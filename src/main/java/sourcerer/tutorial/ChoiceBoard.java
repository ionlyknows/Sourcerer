package sourcerer.tutorial;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import sourcerer.ui.RoundPanel;
import sourcerer.ui.Theme;

/** CONDITIONALS (and a bit of loops/functions) mechanic: read the code, then pick the path that really happens. */
class ChoiceBoard extends Board {

    private final String prompt;
    private final int correct;
    private final String why;
    private final PieceButton[] opts;
    private int sel = -1;

    ChoiceBoard(Ctx ctx, String prompt, String code, String[] options, int correct, String why) {
        super(ctx);
        this.prompt = prompt;
        this.correct = correct;
        this.why = why;
        setLayout(new BorderLayout(0, 6));

        RoundPanel codeBox = new RoundPanel(new Color(0x0F1620), Theme.TEAL_DARK, 18);
        codeBox.setLayout(new BorderLayout());
        JTextArea area = new JTextArea(code);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setFont(Theme.mono(java.awt.Font.BOLD, 20));
        area.setForeground(Theme.CYAN_LIGHT);
        codeBox.add(area, BorderLayout.CENTER);
        add(codeBox, BorderLayout.NORTH);

        JPanel lower = new JPanel(new BorderLayout());
        lower.setOpaque(false);
        lower.add(new Fork(options.length), BorderLayout.NORTH);
        JPanel cells = new JPanel(new GridLayout(1, options.length));
        cells.setOpaque(false);
        opts = new PieceButton[options.length];
        for (int i = 0; i < options.length; i++) {
            final int idx = i;
            PieceButton b = new PieceButton(options[i]);
            b.setPreferredSize(new Dimension(150, 64));
            b.addActionListener(e -> choose(idx));
            opts[i] = b;
            JPanel cell = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            cell.setOpaque(false);
            cell.add(b);
            cells.add(cell);
        }
        lower.add(cells, BorderLayout.CENTER);
        add(lower, BorderLayout.CENTER);
    }

    private void choose(int i) {
        if (sel >= 0) {
            opts[sel].setSelected(false);
        }
        sel = (sel == i) ? -1 : i;
        if (sel >= 0) {
            opts[sel].setSelected(true);
        }
    }

    @Override
    String prompt() {
        return prompt;
    }

    @Override
    String actionLabel() {
        return "GO!";
    }

    @Override
    void action() {
        if (sel < 0) {
            ctx.say("Click one of the paths first!");
            return;
        }
        if (sel == correct) {
            opts[sel].setLocked(true);
            ctx.solved(why);
        } else {
            opts[sel].setSelected(false);
            sel = -1;
            ctx.mistake("Not that path. Work out the condition step by step: is it True or False?");
        }
    }

    /** Lines from the code box down to each possible path. */
    private static class Fork extends JComponent {
        private final int n;

        Fork(int n) {
            this.n = n;
            setPreferredSize(new Dimension(100, 56));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f,
                    new float[]{8f, 6f}, 0f));
            g2.setColor(Theme.TEAL);
            for (int i = 0; i < n; i++) {
                g2.drawLine(w / 2, 4, (int) ((i + 0.5) * w / n), h - 2);
            }
            g2.setColor(Theme.XP_GOLD);
            g2.fillOval(w / 2 - 6, 0, 12, 12);
            g2.dispose();
        }
    }
}
