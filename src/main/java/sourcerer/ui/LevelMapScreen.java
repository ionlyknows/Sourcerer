package sourcerer.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import sourcerer.model.Level;
import sourcerer.model.LevelBank;
import sourcerer.model.Progress;

/**
 * World map: a winding path of numbered level nodes (Candy-Crush style).
 * Locked levels are greyed out until the previous level is completed.
 * Temporary art: everything is drawn with Java2D, so it can be swapped for images later.
 */
public class LevelMapScreen extends ScreenPanel {

    private static final int PAGE_SIZE = 10;
    private static final int R = 34;

    private final GameFrame frame;
    private final Progress progress;
    private final MapCanvas canvas = new MapCanvas();
    private final JLabel starsLabel = Theme.label("", Theme.serif(Font.BOLD, 26), Theme.XP_GOLD);
    private final JLabel info = Theme.label(" ", Theme.serif(Font.BOLD, 20), Color.WHITE);
    private final JLabel pageLabel = Theme.label("", Theme.sans(Font.BOLD, 16), Color.WHITE);
    private int page;

    public LevelMapScreen(GameFrame frame, Progress progress) {
        super(new Color(0x56C2B0), new Color(0x1D4E5F));
        this.frame = frame;
        this.progress = progress;
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        FantasyButton back = new FantasyButton("Main Menu", 180, 44);
        back.addActionListener(e -> frame.showMenu());
        JPanel leftButtons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        leftButtons.setOpaque(false);
        leftButtons.add(back);
        FantasyButton store = new FantasyButton("Potion Store", 180, 44);
        store.addActionListener(e -> frame.showStore());
        leftButtons.add(store);
        header.add(leftButtons, BorderLayout.WEST);
        JLabel title = Theme.label("Python Quest Map", Theme.serif(Font.BOLD, 40), Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(title, BorderLayout.CENTER);
        header.add(starsLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        JButton previous = new JButton("‹ Previous");
        JButton next = new JButton("Next ›");
        previous.addActionListener(e -> changePage(-1));
        next.addActionListener(e -> changePage(1));
        footer.add(previous, BorderLayout.WEST);
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        info.setHorizontalAlignment(SwingConstants.CENTER);
        pageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        center.add(pageLabel, BorderLayout.NORTH);
        center.add(info, BorderLayout.CENTER);
        footer.add(center, BorderLayout.CENTER);
        footer.add(next, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refresh();
            }
        });
        refresh();
    }

    public void refresh() {
        starsLabel.setText("Stars: " + progress.totalStars() + " / " + (3 * LevelBank.count()));
        page = Math.max(0, Math.min(page, (LevelBank.count() - 1) / PAGE_SIZE));
        pageLabel.setText("Region " + (page + 1) + " / " + ((LevelBank.count() + PAGE_SIZE - 1) / PAGE_SIZE));
        info.setText("Click the glowing level to play!");
        canvas.repaint();
    }

    private void changePage(int amount) {
        int pages = (LevelBank.count() + PAGE_SIZE - 1) / PAGE_SIZE;
        page = Math.max(0, Math.min(pages - 1, page + amount));
        refresh();
    }

    private class MapCanvas extends JComponent {

        private double phase;
        private int hover = -1;
        private int shakeId = -1;
        private int shakeTick;
        private final Timer timer = new Timer(40, e -> {
            phase += 0.15;
            if (shakeTick > 0) {
                shakeTick--;
            }
            repaint();
        });

        MapCanvas() {
            setOpaque(false);
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int h = hit(e.getX(), e.getY());
                    if (h != hover) {
                        hover = h;
                        if (h > 0) {
                            Level lv = LevelBank.get(h);
                            info.setText("Level " + h + ": " + lv.getTitle()
                                    + (progress.isUnlocked(h) ? "" : "  (locked)"));
                        } else {
                            info.setText("Click the glowing level to play!");
                        }
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int h = hit(e.getX(), e.getY());
                    if (h < 0) {
                        return;
                    }
                    if (progress.isUnlocked(h)) {
                        frame.startLevel(h);
                    } else {
                        shakeId = h;
                        shakeTick = 12;
                        info.setText("Level " + h + " is locked - finish Level " + (h - 1) + " first!");
                    }
                }
            });
        }

        @Override
        public void addNotify() {
            super.addNotify();
            timer.start();
        }

        @Override
        public void removeNotify() {
            timer.stop();
            super.removeNotify();
        }

        private int firstStage() { return page * PAGE_SIZE + 1; }
        private int visibleCount() { return Math.min(PAGE_SIZE, LevelBank.count() - page * PAGE_SIZE); }

        private int px(int i) {
            int col = i % 5;
            if (i / 5 == 1) col = 4 - col;
            return (int) ((0.12 + col * 0.19) * getWidth());
        }

        private int py(int i) {
            return (int) ((i < 5 ? 0.38 : 0.72) * getHeight());
        }

        /** @return level id (1-based) under the mouse, or -1. */
        private int hit(int x, int y) {
            for (int i = 0; i < visibleCount(); i++) {
                int dx = x - px(i);
                int dy = y - py(i);
                if (dx * dx + dy * dy <= (R + 4) * (R + 4)) {
                    return firstStage() + i;
                }
            }
            return -1;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int n = visibleCount();

            drawRibbon(g2, "Python Town", (int) (getWidth() * 0.20), (int) (getHeight() * 0.94), new Color(0xF48FB1));
            drawRibbon(g2, "Loop & Function Forest", (int) (getWidth() * 0.62), (int) (getHeight() * 0.07),
                    new Color(0x9FD88F));

            // path
            for (int i = 0; i < n - 1; i++) {
                int id = firstStage() + i;
                boolean open = progress.isCompleted(id);
                Path2D seg = new Path2D.Float();
                seg.moveTo(px(i), py(i));
                seg.quadTo((px(i) + px(i + 1)) / 2.0, Math.min(py(i), py(i + 1)) - 30, px(i + 1), py(i + 1));
                g2.setStroke(new BasicStroke(26, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(255, 250, 235, open ? 235 : 120));
                g2.draw(seg);
                g2.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1f, new float[]{14f, 14f}, 0f));
                g2.setColor(open ? new Color(0xF06292) : new Color(0xB0A8B9));
                g2.draw(seg);
            }

            int current = progress.currentLevel();
            for (int i = 0; i < n; i++) {
                int id = firstStage() + i;
                drawNode(g2, i, id, id == current);
            }
            g2.dispose();
        }

        private void drawNode(Graphics2D g2, int i, int id, boolean current) {
            boolean unlocked = progress.isUnlocked(id);
            boolean done = progress.isCompleted(id);
            int cx = px(i);
            int cy = py(i);
            if (id == shakeId && shakeTick > 0) {
                cx += (shakeTick % 2 == 0) ? 6 : -6;
            }
            int r = R + (hover == id && unlocked ? 4 : 0);
            if (current) {
                r += (int) (Math.sin(phase * 1.5) * 3);
                g2.setColor(new Color(255, 230, 120, 90));
                g2.fill(new Ellipse2D.Double(cx - r - 12, cy - r - 12, 2 * r + 24, 2 * r + 24));
            }
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fill(new Ellipse2D.Double(cx - r + 2, cy - r + 6, 2 * r, 2 * r));
            boolean boss = LevelBank.get(id).isBoss();
            Color fill = !unlocked ? new Color(0x8A8398) : done ? new Color(0x3DBE7A)
                    : boss ? new Color(0xE87545) : new Color(0xF2C14E);
            g2.setColor(fill);
            g2.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
            g2.setStroke(new BasicStroke(4f));
            g2.setColor(!unlocked ? new Color(0x5C566B) : Color.WHITE);
            g2.draw(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));

            if (unlocked) {
                g2.setFont(Theme.serif(Font.BOLD, 34));
                g2.setColor(new Color(0x2B2238));
                String s = String.valueOf(id);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(s, cx - fm.stringWidth(s) / 2, cy + fm.getAscent() / 2 - 2);
            } else {
                drawLock(g2, cx, cy);
            }

            // stars above
            for (int s = 0; s < 3; s++) {
                boolean on = s < progress.getStars(id);
                drawStar(g2, cx + (s - 1) * 24, cy - r - 14 - (s == 1 ? 6 : 0), 11,
                        on ? Theme.XP_GOLD : new Color(0, 0, 0, 90));
            }

            // title under node
            g2.setFont(Theme.serif(Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            String t = LevelBank.get(id).getTitle();
            g2.setColor(new Color(0, 0, 0, 120));
            g2.drawString(t, cx - fm.stringWidth(t) / 2 + 1, cy + r + 21);
            g2.setColor(Color.WHITE);
            g2.drawString(t, cx - fm.stringWidth(t) / 2, cy + r + 20);

            if (current) {
                int bob = (int) (Math.sin(phase * 2) * 6);
                int ty = cy - r - 62 + bob;
                g2.setColor(Theme.BAD);
                g2.fillPolygon(new Polygon(new int[]{cx - 14, cx + 14, cx}, new int[]{ty, ty, ty + 18}, 3));
                g2.setFont(Theme.sans(Font.BOLD, 14));
                String p = "PLAY";
                g2.drawString(p, cx - g2.getFontMetrics().stringWidth(p) / 2, ty - 4);
            }
        }

        private void drawLock(Graphics2D g2, int cx, int cy) {
            g2.setColor(new Color(0x3F3A4C));
            g2.fill(new RoundRectangle2D.Double(cx - 14, cy - 2, 28, 22, 6, 6));
            g2.setStroke(new BasicStroke(4f));
            g2.drawArc(cx - 9, cy - 18, 18, 22, 0, 180);
        }

        private void drawStar(Graphics2D g2, int cx, int cy, int r, Color c) {
            Polygon p = new Polygon();
            for (int k = 0; k < 10; k++) {
                double ang = -Math.PI / 2 + k * Math.PI / 5;
                double rad = (k % 2 == 0) ? r : r * 0.45;
                p.addPoint((int) (cx + Math.cos(ang) * rad), (int) (cy + Math.sin(ang) * rad));
            }
            g2.setColor(c);
            g2.fillPolygon(p);
        }

        private void drawRibbon(Graphics2D g2, String text, int cx, int cy, Color c) {
            g2.setFont(Theme.serif(Font.BOLD, 22));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(text) + 50;
            int h = 36;
            g2.setColor(c);
            g2.fill(new RoundRectangle2D.Double(cx - w / 2.0, cy - h / 2.0, w, h, 14, 14));
            g2.setColor(new Color(255, 255, 255, 200));
            g2.setStroke(new BasicStroke(2.5f));
            g2.draw(new RoundRectangle2D.Double(cx - w / 2.0, cy - h / 2.0, w, h, 14, 14));
            g2.setColor(new Color(0x2B2238));
            g2.drawString(text, cx - fm.stringWidth(text) / 2, cy + fm.getAscent() / 2 - 2);
        }
    }
}
