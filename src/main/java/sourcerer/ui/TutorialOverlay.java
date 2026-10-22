package sourcerer.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Guided-tour layer drawn over the game. Dims the screen, spotlights one component, points an arrow
 * at it and shows a speech bubble. Clicks are only allowed inside the spotlight (and on the bubble).
 * Steps either wait for a button press ("Next") or for a condition (done) such as "tiles assembled".
 * Also used for the short lesson card shown before every level.
 */
public class TutorialOverlay extends JComponent {

    /** One tutorial step. Build with the fluent setters. */
    public static class Step {
        String title = "";
        String text = "";
        String code;
        String output;
        String badge = "Tutorial";
        String button = "Next";
        Supplier<Component> target;
        BooleanSupplier done;
        Runnable onEnter;

        public Step(String title, String text) {
            this.title = title;
            this.text = text;
        }

        public Step target(Supplier<Component> t) { this.target = t; return this; }
        public Step waitFor(BooleanSupplier d) { this.done = d; return this; }
        public Step code(String c, String o) { this.code = c; this.output = o; return this; }
        public Step badge(String b) { this.badge = b; return this; }
        public Step button(String b) { this.button = b; return this; }
        public Step onEnter(Runnable r) { this.onEnter = r; return this; }
    }

    private final List<Step> steps = new ArrayList<>();
    private int index;
    private Runnable onFinish;
    private boolean active;
    private boolean suspended;
    private double phase;
    private Rectangle spot;

    private final RoundPanel bubble = new RoundPanel(new Color(0xF6EBD0), new Color(0x2F4FA0), 26);
    private final JLabel badgeLabel = Theme.label("", Theme.sans(Font.BOLD, 12), new Color(0x1B7F8C));
    private final JLabel titleLabel = Theme.label("", Theme.serif(Font.BOLD, 24), Theme.INK);
    private final JLabel textLabel = new JLabel();
    private final JLabel codeLabel = new JLabel();
    private final JLabel outLabel = new JLabel();
    private final JLabel progressLabel = Theme.label("", Theme.sans(Font.BOLD, 12), new Color(0x7A5C8A));
    private final JLabel waitLabel = Theme.label("", Theme.sans(Font.BOLD, 13), new Color(0x1E9E5A));
    private final FantasyButton nextBtn = new FantasyButton("Next", 150, 40);
    private final FantasyButton skipBtn = new FantasyButton("Skip", 90, 34);
    private final RoundPanel codeBox = new RoundPanel(new Color(0x1C2530), new Color(0x1C2530), 12);
    private final Timer timer = new Timer(40, e -> tick());

    public TutorialOverlay() {
        setOpaque(false);
        setVisible(false);
        addMouseListener(new MouseAdapter() { });
        addMouseMotionListener(new MouseAdapter() { });
        setLayout(null);

        bubble.setLayout(new BorderLayout(8, 0));
        SpriteComponent hero = new SpriteComponent(SpriteComponent.Kind.HERO);
        hero.setPreferredSize(new Dimension(110, 130));
        bubble.add(hero, BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        textLabel.setFont(Theme.serif(Font.PLAIN, 18));
        textLabel.setForeground(Theme.INK);
        codeLabel.setFont(Theme.mono(Font.BOLD, 17));
        codeLabel.setForeground(Theme.CYAN_LIGHT);
        outLabel.setFont(Theme.mono(Font.PLAIN, 13));
        outLabel.setForeground(new Color(0xB9C4D0));
        codeBox.setLayout(new BoxLayout(codeBox, BoxLayout.Y_AXIS));
        codeBox.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        codeBox.add(codeLabel);
        codeBox.add(outLabel);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(progressLabel);
        left.add(waitLabel);
        bottom.add(left, BorderLayout.WEST);
        JPanel btns = new JPanel();
        btns.setOpaque(false);
        btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));
        btns.add(skipBtn);
        btns.add(Box.createHorizontalStrut(8));
        btns.add(nextBtn);
        bottom.add(btns, BorderLayout.EAST);

        for (JComponent c : new JComponent[]{badgeLabel, titleLabel, textLabel, codeBox, bottom}) {
            c.setAlignmentX(LEFT_ALIGNMENT);
        }
        content.add(badgeLabel);
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(textLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(codeBox);
        content.add(Box.createVerticalStrut(10));
        content.add(bottom);
        bubble.add(content, BorderLayout.CENTER);
        add(bubble);

        nextBtn.addActionListener(e -> advance());
        skipBtn.addActionListener(e -> finish());
    }

    // ------------------------------------------------------------------ control

    public boolean isRunning() {
        return active;
    }

    public void start(List<Step> list, Runnable onFinish, boolean skippable) {
        steps.clear();
        steps.addAll(list);
        this.onFinish = onFinish;
        skipBtn.setVisible(skippable);
        active = true;
        index = 0;
        fitToParent();
        showStep();
        timer.start();
        updateVisibility();
    }

    /** Stops the overlay without calling the finish callback. */
    public void stop() {
        active = false;
        timer.stop();
        updateVisibility();
    }

    public void setSuspended(boolean s) {
        suspended = s;
        updateVisibility();
    }

    private void updateVisibility() {
        setVisible(active && !suspended);
        repaint();
    }

    private void fitToParent() {
        if (getParent() != null) {
            setBounds(0, 0, getParent().getWidth(), getParent().getHeight());
        }
    }

    private void advance() {
        if (index + 1 >= steps.size()) {
            finish();
        } else {
            index++;
            showStep();
        }
    }

    private void finish() {
        Runnable r = onFinish;
        stop();
        if (r != null) {
            r.run();
        }
    }

    private void showStep() {
        Step s = steps.get(index);
        badgeLabel.setText(s.badge.toUpperCase());
        titleLabel.setText(s.title);
        textLabel.setText("<html><div style='width:330px'>" + s.text.replace("\n", "<br>") + "</div></html>");
        codeBox.setVisible(s.code != null);
        codeLabel.setText(s.code == null ? "" : s.code);
        outLabel.setText(s.output == null ? "" : "-> " + s.output);
        progressLabel.setText("Step " + (index + 1) + " / " + steps.size());
        boolean waiting = s.done != null;
        nextBtn.setVisible(!waiting);
        nextBtn.setText(s.button);
        waitLabel.setText(waiting ? "Do it to continue!" : " ");
        if (s.onEnter != null) {
            s.onEnter.run();
        }
        bubble.setSize(bubble.getPreferredSize());
        bubble.revalidate();
        bubble.setSize(bubble.getPreferredSize());
    }

    // ------------------------------------------------------------------ per-frame update

    private void tick() {
        if (!active || suspended) {
            return;
        }
        phase += 0.2;
        fitToParent();
        Step s = steps.get(index);
        if (s.done != null && s.done.getAsBoolean()) {
            advance();
            if (!active) {
                return;
            }
            s = steps.get(index);
        }
        spot = computeSpot(s);
        placeBubble();
        repaint();
    }

    private Rectangle computeSpot(Step s) {
        if (s.target == null) {
            return null;
        }
        Component c = s.target.get();
        if (c == null || !c.isShowing() || c.getParent() == null) {
            return null;
        }
        Rectangle r = SwingUtilities.convertRectangle(c.getParent(), c.getBounds(), this);
        r.grow(8, 8);
        return r;
    }

    private void placeBubble() {
        Dimension d = bubble.getPreferredSize();
        int w = d.width;
        int h = d.height;
        int W = getWidth();
        int H = getHeight();
        List<Rectangle> cands = new ArrayList<>();
        if (spot == null) {
            cands.add(new Rectangle((W - w) / 2, (H - h) / 2, w, h));
        } else {
            int gap = 40;
            cands.add(new Rectangle(spot.x + spot.width / 2 - w / 2, spot.y + spot.height + gap, w, h));
            cands.add(new Rectangle(spot.x + spot.width / 2 - w / 2, spot.y - h - gap, w, h));
            cands.add(new Rectangle(spot.x + spot.width + gap, spot.y + spot.height / 2 - h / 2, w, h));
            cands.add(new Rectangle(spot.x - w - gap, spot.y + spot.height / 2 - h / 2, w, h));
            cands.add(new Rectangle(W - w - 20, H - h - 20, w, h));
            cands.add(new Rectangle(20, H - h - 20, w, h));
            cands.add(new Rectangle(W - w - 20, 20, w, h));
            cands.add(new Rectangle(20, 20, w, h));
        }
        Rectangle best = null;
        long bestScore = Long.MAX_VALUE;
        Rectangle screen = new Rectangle(0, 0, W, H);
        for (Rectangle c : cands) {
            Rectangle clamped = new Rectangle(Math.max(10, Math.min(c.x, W - w - 10)),
                    Math.max(10, Math.min(c.y, H - h - 10)), w, h);
            long score = 0;
            if (spot != null) {
                Rectangle inter = clamped.intersection(spot);
                if (!inter.isEmpty()) {
                    score += (long) inter.width * inter.height + 1_000_000L;
                }
            }
            if (!screen.contains(clamped)) {
                score += 5_000_000L;
            }
            if (score < bestScore) {
                bestScore = score;
                best = clamped;
            }
        }
        bubble.setBounds(best);
    }

    /** Clicks inside the spotlight fall through to the game; everything else is blocked. */
    @Override
    public boolean contains(int x, int y) {
        if (!active || suspended) {
            return false;
        }
        if (spot != null && spot.contains(x, y) && !bubble.getBounds().contains(x, y)) {
            return false;
        }
        return super.contains(x, y);
    }

    // ------------------------------------------------------------------ painting

    @Override
    protected void paintComponent(Graphics g) {
        if (!active || suspended) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        Area dim = new Area(new Rectangle(0, 0, w, h));
        if (spot != null) {
            dim.subtract(new Area(new RoundRectangle2D.Float(spot.x, spot.y, spot.width, spot.height, 24, 24)));
        }
        g2.setColor(new Color(5, 3, 15, 175));
        g2.fill(dim);

        if (spot != null) {
            float pulse = (float) (0.5 + 0.5 * Math.sin(phase));
            g2.setStroke(new BasicStroke(3f + 3f * pulse));
            g2.setColor(new Color(46, 196, 182, 140 + (int) (100 * pulse)));
            g2.draw(new RoundRectangle2D.Float(spot.x, spot.y, spot.width, spot.height, 24, 24));
            drawArrow(g2);
        }
        g2.dispose();
    }

    private void drawArrow(Graphics2D g2) {
        Rectangle b = bubble.getBounds();
        double dx = b.getCenterX() - spot.getCenterX();
        double dy = b.getCenterY() - spot.getCenterY();
        int bob = (int) (Math.sin(phase * 1.5) * 6);
        int len = 34;
        int cx = (int) spot.getCenterX();
        int cy = (int) spot.getCenterY();
        Polygon arrow = new Polygon();
        boolean horizontal = Math.abs(dx) * spot.height > Math.abs(dy) * spot.width;
        if (horizontal && dx > 0) {
            int tipX = spot.x + spot.width + 4 + bob;
            arrow.addPoint(tipX, cy);
            arrow.addPoint(tipX + len, cy - 16);
            arrow.addPoint(tipX + len, cy + 16);
        } else if (horizontal) {
            int tipX = spot.x - 4 - bob;
            arrow.addPoint(tipX, cy);
            arrow.addPoint(tipX - len, cy - 16);
            arrow.addPoint(tipX - len, cy + 16);
        } else if (dy > 0) {
            int tipY = spot.y + spot.height + 4 + bob;
            arrow.addPoint(cx, tipY);
            arrow.addPoint(cx - 16, tipY + len);
            arrow.addPoint(cx + 16, tipY + len);
        } else {
            int tipY = spot.y - 4 - bob;
            arrow.addPoint(cx, tipY);
            arrow.addPoint(cx - 16, tipY - len);
            arrow.addPoint(cx + 16, tipY - len);
        }
        g2.setColor(Theme.XP_GOLD);
        g2.fillPolygon(arrow);
        g2.setColor(new Color(0x7A5A10));
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(arrow);
    }
}
