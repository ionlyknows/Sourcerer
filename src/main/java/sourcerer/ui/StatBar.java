package sourcerer.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;

/** Horizontal HP / MP / XP bar with a "HP 80/100" caption. */
public class StatBar extends JComponent {

    private final String label;
    private final Color fill;
    private int value = 0;
    private int max = 1;

    public StatBar(String label, Color fill) {
        this.label = label;
        this.fill = fill;
        setPreferredSize(new Dimension(220, 22));
        setMinimumSize(new Dimension(100, 22));
        setOpaque(false);
    }

    public void setValues(int value, int max) {
        this.value = Math.max(0, value);
        this.max = Math.max(1, max);
        repaint();
    }

    public int getValue() {
        return value;
    }

    public int getMax() {
        return max;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        RoundRectangle2D track = new RoundRectangle2D.Float(1, 1, w - 3, h - 3, h, h);
        g2.setColor(new Color(0x120E24));
        g2.fill(track);

        int fw = (int) ((w - 4) * (value / (double) max));
        if (fw > 0) {
            g2.setClip(track);
            g2.setPaint(new GradientPaint(0, 0, fill.brighter(), 0, h, fill.darker()));
            g2.fillRect(2, 2, fw, h - 4);
            g2.setClip(null);
        }
        g2.setColor(Theme.PARCHMENT_DARK);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(track);

        String text = label + " " + value + "/" + max;
        g2.setFont(Theme.sans(java.awt.Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(text)) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(new Color(0, 0, 0, 160));
        g2.drawString(text, tx + 1, ty + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(text, tx, ty);
        g2.dispose();
    }
}
