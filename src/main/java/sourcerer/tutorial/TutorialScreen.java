package sourcerer.tutorial;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import sourcerer.ui.FantasyButton;
import sourcerer.ui.RoundPanel;
import sourcerer.ui.ScreenPanel;
import sourcerer.ui.SpriteComponent;
import sourcerer.ui.Theme;

/**
 * TUTORIAL MODE - a calm puzzle game with no monsters, HP or combat.
 * Stage list -> six lessons, each made of short puzzle rounds (match, sort, connect, choose, fill).
 * Layout follows the reference: score board + guide on the left, framed puzzle board on the right.
 */
public class TutorialScreen extends ScreenPanel {

    /** Set to true while testing to unlock every tutorial. */
    private static final boolean UNLOCK_ALL = false;
    private static final String[] PRAISE = {"EXCELLENT!", "GREAT JOB!", "NICE!", "PERFECT!", "WELL DONE!"};

    private final Runnable onExit;
    private final TutorialProgress progress = new TutorialProgress();
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    // stage list
    private final JPanel selectGrid = new JPanel(new GridLayout(2, 3, 24, 24));
    private final JLabel selectInfo = Theme.label(" ", Theme.serif(Font.BOLD, 20), Color.WHITE);
    private final JLabel totalStars = Theme.label("", Theme.serif(Font.BOLD, 26), Theme.XP_GOLD);

    // play view
    private final JLabel titleLabel = Theme.label("", Theme.serif(Font.BOLD, 34), Color.WHITE);
    private final JLabel roundLabel = Theme.label("", Theme.serif(Font.BOLD, 22), Theme.XP_GOLD);
    private final JLabel scoreLabel = Theme.label("0", Theme.serif(Font.BOLD, 56), new Color(0x1F2F7A));
    private final JLabel bubbleLabel = new JLabel();
    private final JLabel promptLabel = new JLabel();
    private final JPanel boardHolder = new JPanel(new BorderLayout());
    private final FantasyButton actionBtn = new FantasyButton("SUBMIT", 240, 56);
    private final SpriteComponent hero = new SpriteComponent(SpriteComponent.Kind.HERO);

    private Stage stage;
    private int stageIndex;
    private int round;
    private int score;
    private int mistakes;
    private int roundMistakes;
    private boolean busy;
    private Board board;
    private Timer nextTimer;

    private final Board.Ctx ctx = new Board.Ctx() {
        @Override
        public void solved(String message) {
            if (busy) {
                return;
            }
            busy = true;
            score += 50 + (roundMistakes == 0 ? 25 : 0);
            scoreLabel.setText(String.valueOf(score));
            say(PRAISE[(int) (Math.random() * PRAISE.length)] + " " + message);
            hero.hit(roundMistakes == 0 ? "+75" : "+50", Theme.GOOD);
            actionBtn.setEnabled(false);
            nextTimer = new Timer(1500, e -> {
                round++;
                loadRound();
            });
            nextTimer.setRepeats(false);
            nextTimer.start();
        }

        @Override
        public void mistake(String message) {
            if (busy) {
                return;
            }
            mistakes++;
            roundMistakes++;
            say(message);
            hero.hit("Oops!", Theme.BAD);
        }

        @Override
        public void say(String message) {
            TutorialScreen.this.say(message);
        }
    };

    public TutorialScreen(Runnable onExit) {
        super(new Color(0x3FA7B8), new Color(0x1D2A5E));
        this.onExit = onExit;
        setLayout(new BorderLayout());
        root.setOpaque(false);
        root.add(buildSelect(), "select");
        root.add(buildPlay(), "play");
        add(root, BorderLayout.CENTER);
        refreshSelect();
    }

    /** Called by the frame every time the Tutorial button is pressed. */
    public void open() {
        cancelTimer();
        refreshSelect();
        cards.show(root, "select");
    }

    // ------------------------------------------------------------------ stage list

    private JPanel buildSelect() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        FantasyButton back = new FantasyButton("Main Menu", 180, 44);
        back.addActionListener(e -> {
            cancelTimer();
            onExit.run();
        });
        header.add(back, BorderLayout.WEST);
        JLabel title = Theme.label("Python Puzzle Lessons", Theme.serif(Font.BOLD, 40), Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(title, BorderLayout.CENTER);
        header.add(totalStars, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);

        selectGrid.setOpaque(false);
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(selectGrid);
        p.add(center, BorderLayout.CENTER);

        selectInfo.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(selectInfo, BorderLayout.SOUTH);
        return p;
    }

    private void refreshSelect() {
        selectGrid.removeAll();
        for (int i = 0; i < StageBank.count(); i++) {
            selectGrid.add(new StageCard(i));
        }
        totalStars.setText("Stars: " + progress.total() + " / " + (3 * StageBank.count()));
        selectInfo.setText("Pick a lesson and start puzzling!");
        selectGrid.revalidate();
        selectGrid.repaint();
    }

    private boolean unlocked(int idx) {
        return UNLOCK_ALL || progress.isUnlocked(idx);
    }

    // ------------------------------------------------------------------ play view

    private JPanel buildPlay() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(14, 22, 16, 22));

        // header: Restart / Stages, title, round counter
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        FantasyButton restart = new FantasyButton("Restart", 150, 46);
        restart.addActionListener(e -> startStage(stageIndex));
        FantasyButton stages = new FantasyButton("Stages", 150, 46);
        stages.addActionListener(e -> open());
        buttons.add(restart);
        buttons.add(stages);
        header.add(buttons, BorderLayout.WEST);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(titleLabel, BorderLayout.CENTER);
        roundLabel.setPreferredSize(new Dimension(190, 30));
        roundLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(roundLabel, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);

        // left: score board, speech bubble, guide
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(330, 100));
        RoundPanel score = new RoundPanel(new Color(0xE6F7FB), new Color(0x2F4FA0), 30);
        score.setLayout(new BorderLayout());
        JLabel sb = Theme.label("Score Board", Theme.serif(Font.PLAIN, 22), new Color(0x4A5FA8));
        sb.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        score.add(sb, BorderLayout.NORTH);
        score.add(scoreLabel, BorderLayout.CENTER);
        left.add(score, BorderLayout.NORTH);

        JPanel guide = new JPanel(new BorderLayout(0, 4));
        guide.setOpaque(false);
        RoundPanel bubble = new RoundPanel(new Color(0xF6EBD0), new Color(0x2F4FA0), 26);
        bubble.setLayout(new BorderLayout());
        bubbleLabel.setFont(Theme.serif(Font.BOLD, 17));
        bubbleLabel.setForeground(Theme.INK);
        bubble.add(bubbleLabel, BorderLayout.CENTER);
        guide.add(bubble, BorderLayout.NORTH);
        guide.add(hero, BorderLayout.CENTER);
        left.add(guide, BorderLayout.CENTER);
        p.add(left, BorderLayout.WEST);

        // right: framed board
        RoundPanel frame = new RoundPanel(new Color(0x1C1738), Theme.TEAL, 34);
        frame.setLayout(new BorderLayout(0, 10));
        frame.setBorder(BorderFactory.createEmptyBorder(16, 20, 14, 20));
        promptLabel.setFont(Theme.serif(Font.BOLD, 22));
        promptLabel.setForeground(Theme.TEXT);
        frame.add(promptLabel, BorderLayout.NORTH);
        boardHolder.setOpaque(false);
        frame.add(boardHolder, BorderLayout.CENTER);
        JPanel south = new JPanel(new GridBagLayout());
        south.setOpaque(false);
        south.add(actionBtn);
        frame.add(south, BorderLayout.SOUTH);
        JPanel rightWrap = new JPanel(new BorderLayout());
        rightWrap.setOpaque(false);
        rightWrap.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        rightWrap.add(frame, BorderLayout.CENTER);
        p.add(rightWrap, BorderLayout.CENTER);

        actionBtn.addActionListener(e -> {
            if (!busy && board != null) {
                board.action();
            }
        });
        return p;
    }

    private void startStage(int idx) {
        cancelTimer();
        stageIndex = idx;
        stage = StageBank.all().get(idx);
        round = 0;
        score = 0;
        mistakes = 0;
        scoreLabel.setText("0");
        titleLabel.setText("Tutorial " + (idx + 1) + ": " + stage.title);
        cards.show(root, "play");
        loadRound();
        say(stage.tip);
    }

    private void loadRound() {
        if (round >= stage.rounds.size()) {
            showClear();
            return;
        }
        roundMistakes = 0;
        busy = false;
        board = stage.rounds.get(round).apply(ctx);
        boardHolder.removeAll();
        boardHolder.add(board, BorderLayout.CENTER);
        promptLabel.setText("<html><div style='width:520px'>" + Board.esc(board.prompt()) + "</div></html>");
        actionBtn.setText(board.actionLabel());
        actionBtn.setVisible(true);
        actionBtn.setEnabled(true);
        roundLabel.setText("Round " + (round + 1) + " / " + stage.rounds.size());
        boardHolder.revalidate();
        boardHolder.repaint();
        if (round > 0) {
            say("Next puzzle! You've got this.");
        }
    }

    private void showClear() {
        board = null;
        int stars = mistakes == 0 ? 3 : (mistakes <= 2 ? 2 : 1);
        progress.complete(stageIndex, stars);
        boolean hasNext = stageIndex + 1 < StageBank.count();

        JPanel box = new JPanel(new GridBagLayout());
        box.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new java.awt.Insets(6, 0, 6, 0);
        JLabel t = Theme.label("STAGE CLEAR!", Theme.serif(Font.BOLD, 52), Theme.XP_GOLD);
        box.add(t, c);
        c.gridy++;
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            s.append(i < stars ? "\u2605 " : "\u2606 ");
        }
        box.add(Theme.label(s.toString(), Theme.serif(Font.BOLD, 44), Theme.XP_GOLD), c);
        c.gridy++;
        box.add(Theme.label("Score " + score + "   |   Mistakes " + mistakes, Theme.sans(Font.BOLD, 18),
                Theme.TEXT), c);
        c.gridy++;
        box.add(Theme.label("<html><div style='width:460px;text-align:center'>" + Board.esc(stage.summary)
                + (hasNext ? "" : "<br><br>You finished every tutorial - try Play mode next!") + "</div></html>",
                Theme.serif(Font.PLAIN, 19), Theme.CYAN_LIGHT), c);
        c.gridy++;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        btns.setOpaque(false);
        if (hasNext) {
            FantasyButton next = new FantasyButton("Next Tutorial", 220, 52);
            next.addActionListener(e -> startStage(stageIndex + 1));
            btns.add(next);
        }
        FantasyButton again = new FantasyButton("Play Again", 190, 52);
        again.addActionListener(e -> startStage(stageIndex));
        btns.add(again);
        FantasyButton list = new FantasyButton("Stage List", 190, 52);
        list.addActionListener(e -> open());
        btns.add(list);
        box.add(btns, c);

        boardHolder.removeAll();
        boardHolder.add(box, BorderLayout.CENTER);
        promptLabel.setText("All rounds complete!");
        roundLabel.setText("Done");
        actionBtn.setVisible(false);
        boardHolder.revalidate();
        boardHolder.repaint();
        hero.hit("Clear!", Theme.XP_GOLD);
        say("EXCELLENT! You mastered " + stage.title + "!");
    }

    private void say(String msg) {
        bubbleLabel.setText("<html><div style='width:230px'>" + Board.esc(msg) + "</div></html>");
    }

    private void cancelTimer() {
        if (nextTimer != null) {
            nextTimer.stop();
            nextTimer = null;
        }
    }

    // ------------------------------------------------------------------ stage card

    private class StageCard extends JComponent {
        private final int idx;
        private boolean hover;

        StageCard(int idx) {
            this.idx = idx;
            setPreferredSize(new Dimension(260, 170));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (unlocked(idx)) {
                        startStage(idx);
                    } else {
                        selectInfo.setText("Clear Tutorial " + idx + " first to unlock this one!");
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            boolean open = unlocked(idx);
            int st = progress.getStars(idx);
            Stage s = StageBank.all().get(idx);

            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRoundRect(4, 8, w - 8, h - 10, 30, 30);
            g2.setColor(!open ? new Color(0x8A8398) : st > 0 ? new Color(0xD8F5E4) : new Color(0xFFF3C4));
            g2.fillRoundRect(2, 2, w - 8, h - 12, 30, 30);
            g2.setStroke(new BasicStroke(hover && open ? 5f : 3.5f));
            g2.setColor(!open ? new Color(0x5C566B) : hover ? Theme.TEAL : new Color(0x2F4FA0));
            g2.drawRoundRect(2, 2, w - 8, h - 12, 30, 30);

            g2.setColor(open ? Theme.TEAL_DARK : new Color(0x5C566B));
            g2.fillOval(16, 16, 44, 44);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.serif(Font.BOLD, 26));
            String n = String.valueOf(idx + 1);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(n, 38 - fm.stringWidth(n) / 2, 47);

            g2.setColor(Theme.INK);
            g2.setFont(Theme.serif(Font.BOLD, 22));
            g2.drawString(s.title, 72, 42);
            g2.setFont(Theme.sans(Font.PLAIN, 14));
            g2.setColor(new Color(0x4A3A7A));
            g2.drawString(s.mechanic, 72, 62);

            if (open) {
                for (int k = 0; k < 3; k++) {
                    star(g2, w / 2 - 36 + k * 36, 118, 14, k < st ? Theme.XP_GOLD : new Color(0, 0, 0, 70));
                }
                if (st == 0) {
                    g2.setFont(Theme.sans(Font.BOLD, 13));
                    g2.setColor(new Color(0xC0392B));
                    String p = "CLICK TO PLAY";
                    g2.drawString(p, (w - g2.getFontMetrics().stringWidth(p)) / 2 - 3, 152);
                }
            } else {
                g2.setFont(Theme.sans(Font.BOLD, 15));
                g2.setColor(new Color(0x3F3A4C));
                String l = "LOCKED";
                g2.drawString(l, (w - g2.getFontMetrics().stringWidth(l)) / 2 - 3, 125);
            }
            g2.dispose();
        }

        private void star(Graphics2D g2, int cx, int cy, int r, Color c) {
            Polygon p = new Polygon();
            for (int k = 0; k < 10; k++) {
                double ang = -Math.PI / 2 + k * Math.PI / 5;
                double rad = (k % 2 == 0) ? r : r * 0.45;
                p.addPoint((int) (cx + Math.cos(ang) * rad), (int) (cy + Math.sin(ang) * rad));
            }
            g2.setColor(c);
            g2.fillPolygon(p);
        }
    }
}
