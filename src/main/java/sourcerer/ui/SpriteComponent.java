package sourcerer.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Placeholder sprites drawn with Java2D (hero, spider, null pointer).
 * To use real art later, replace the draw methods with g.drawImage(...).
 * Call hit("-12", color) for a shake plus floating number.
 */
public class SpriteComponent extends JComponent {

    public enum Kind {
        HERO, SPIDER, NULL_POINTER;

        public static Kind fromId(String id) {
            if ("null".equals(id)) {
                return NULL_POINTER;
            }
            return SPIDER;
        }
    }

    private Kind kind;
    private double phase;
    private int hitTick = -1;
    private String popText = "";
    private Color popColor = Color.WHITE;
    private final Timer timer = new Timer(40, e -> tick());

    public SpriteComponent(Kind kind) {
        this.kind = kind;
        setOpaque(false);
        setPreferredSize(new Dimension(200, 200));
        setMinimumSize(new Dimension(120, 120));
    }

    public void setKind(Kind kind) {
        this.kind = kind;
        repaint();
    }

    public void hit(String text, Color color) {
        popText = text;
        popColor = color;
        hitTick = 0;
    }

    private void tick() {
        phase += 0.15;
        if (hitTick >= 0) {
            hitTick++;
            if (hitTick > 24) {
                hitTick = -1;
            }
        }
        repaint();
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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        double shake = (hitTick >= 0 && hitTick < 10) ? ((hitTick % 2 == 0) ? 9 : -9) * (1 - hitTick / 10.0) : 0;
        double bob = Math.sin(phase) * 4;

        g2.setColor(new Color(0, 0, 0, 70));
        g2.fill(new Ellipse2D.Double(w * 0.25, h * 0.88, w * 0.5, h * 0.07));

        Graphics2D s = (Graphics2D) g2.create();
        double sc = Math.min(w, h) / 200.0;
        s.translate(w / 2.0 + shake, h / 2.0 + bob);
        s.scale(sc, sc);
        s.translate(-100, -100);
        switch (kind) {
            case HERO: drawHero(s); break;
            case SPIDER: drawSpider(s); break;
            default: drawNullPointer(s); break;
        }
        s.dispose();

        if (hitTick >= 0) {
            float a = Math.max(0f, 1f - hitTick / 24f);
            g2.setComposite(AlphaComposite.SrcOver.derive(a));
            g2.setFont(Theme.serif(Font.BOLD, 30));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(popText)) / 2;
            int ty = (int) (h * 0.3) - hitTick * 2;
            g2.setColor(Color.BLACK);
            g2.drawString(popText, tx + 2, ty + 2);
            g2.setColor(popColor);
            g2.drawString(popText, tx, ty);
        }
        g2.dispose();
    }

    // ---- drawing in a virtual 200 x 200 box ----

    private void drawHero(Graphics2D g) {
        for (int side = -1; side <= 1; side += 2) {
            Path2D wing = new Path2D.Float();
            wing.moveTo(100 + side * 30, 104);
            wing.curveTo(100 + side * 70, 90, 100 + side * 92, 50, 100 + side * 88, 36);
            wing.curveTo(100 + side * 100, 80, 100 + side * 96, 120, 100 + side * 70, 150);
            wing.curveTo(100 + side * 56, 140, 100 + side * 40, 132, 100 + side * 30, 124);
            wing.closePath();
            g.setColor(new Color(240, 168, 80, 200));
            g.fill(wing);
            g.setColor(new Color(150, 90, 30));
            g.setStroke(new BasicStroke(2f));
            g.draw(wing);
            g.setColor(new Color(255, 230, 160, 190));
            g.drawLine(100 + side * 34, 112, 100 + side * 86, 56);
            g.drawLine(100 + side * 34, 120, 100 + side * 90, 96);
        }
        g.setColor(new Color(0x3B4EA8));
        g.fillPolygon(new Polygon(new int[]{70, 130, 152, 48}, new int[]{98, 98, 176, 176}, 4));
        g.setColor(Theme.XP_GOLD);
        g.fillRect(95, 102, 10, 74);
        g.setColor(new Color(0x1F2A6B));
        g.fillOval(78, 172, 18, 10);
        g.fillOval(104, 172, 18, 10);

        g.setColor(new Color(0xFFF3C4));
        g.fill(new Ellipse2D.Double(68, 52, 64, 58));
        g.setColor(Color.BLACK);
        g.fill(new Ellipse2D.Double(80, 70, 14, 18));
        g.fill(new Ellipse2D.Double(106, 70, 14, 18));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(84, 74, 5, 5));
        g.fill(new Ellipse2D.Double(110, 74, 5, 5));

        g.setColor(new Color(0x2C3E8F));
        g.fill(new Ellipse2D.Double(52, 46, 96, 22));
        g.fillPolygon(new Polygon(new int[]{74, 126, 116, 128, 100, 88},
                new int[]{52, 52, 24, 6, 18, 34}, 6));
        g.setColor(Theme.XP_GOLD);
        g.fillRect(78, 44, 44, 8);
    }

    private void drawSpider(Graphics2D g) {
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int[] kneeY = {55, 78, 100, 122};
        int[] footY = {120, 140, 158, 172};
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 4; i++) {
                Path2D leg = new Path2D.Float();
                leg.moveTo(100 + side * 30, 112 + i * 6);
                leg.lineTo(100 + side * (64 + i * 4), kneeY[i]);
                leg.lineTo(100 + side * (90 - i * 2), footY[i]);
                g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(0x2A2438));
                g.draw(leg);
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(0xD9D2EE));
                g.draw(leg);
            }
        }
        g.setColor(new Color(0xFFF1E0));
        g.fill(new Ellipse2D.Double(42, 62, 116, 98));
        g.setColor(new Color(192, 57, 43, 200));
        g.fill(new Ellipse2D.Double(58, 108, 84, 52));

        g.setColor(new Color(0x3A2F4F));
        g.fillPolygon(new Polygon(new int[]{58, 74, 84}, new int[]{82, 52, 78}, 3));
        g.fillPolygon(new Polygon(new int[]{142, 126, 116}, new int[]{82, 52, 78}, 3));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(70, 78, 60, 62));
        g.setColor(new Color(0x15121C));
        g.fill(new Ellipse2D.Double(76, 90, 20, 26));
        g.fill(new Ellipse2D.Double(104, 90, 20, 26));
        g.setColor(new Color(0xE02A2A));
        g.fillPolygon(new Polygon(new int[]{86, 91, 86, 81}, new int[]{94, 103, 112, 103}, 4));
        g.fillPolygon(new Polygon(new int[]{114, 119, 114, 109}, new int[]{94, 103, 112, 103}, 4));
        g.fillPolygon(new Polygon(new int[]{100, 104, 100, 96}, new int[]{118, 124, 130, 124}, 4));
    }

    private void drawNullPointer(Graphics2D g) {
        g.setColor(new Color(60, 220, 230, 60));
        g.fill(new RoundRectangle2D.Double(36, 36, 128, 138, 60, 60));
        g.setColor(new Color(0x2D1B4E));
        g.fill(new RoundRectangle2D.Double(46, 44, 108, 122, 52, 52));
        g.setColor(new Color(60, 220, 230, 120));
        g.fillRect(46, 70, 108, 5);
        g.fillRect(60, 120, 80, 4);
        g.fillRect(46, 140, 108, 6);
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(68, 80, 24, 28));
        g.fill(new Ellipse2D.Double(108, 80, 24, 28));
        g.setColor(new Color(0xE02A2A));
        g.fill(new Ellipse2D.Double(76, 90, 10, 12));
        g.fill(new Ellipse2D.Double(116, 90, 10, 12));
        g.setColor(Theme.CYAN_LIGHT);
        g.setFont(Theme.mono(Font.BOLD, 26));
        g.drawString("null", 72, 134);
        g.setColor(new Color(0x2D1B4E));
        for (int i = 0; i < 4; i++) {
            g.fill(new Ellipse2D.Double(50 + i * 26, 158, 24, 20));
        }
    }
}
