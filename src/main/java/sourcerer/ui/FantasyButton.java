package sourcerer.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import javax.swing.ButtonModel;
import javax.swing.JButton;

/** Hexagon-shaped menu button with normal / hover / pressed / disabled states. */
public class FantasyButton extends JButton {

    public FantasyButton(String text) {
        this(text, 280, 56);
    }

    public FantasyButton(String text, int width, int height) {
        super(text);
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setFont(Theme.serif(java.awt.Font.BOLD, height >= 50 ? 22 : 17));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int c = Math.min(16, h / 3);
        Polygon shape = new Polygon(
                new int[]{c, w - c - 2, w - 2, w - c - 2, c, 1},
                new int[]{1, 1, h / 2, h - 2, h - 2, h / 2}, 6);

        ButtonModel m = getModel();
        Color top;
        Color bottom;
        Color border;
        Color text;
        float stroke = 2.5f;
        if (!isEnabled()) {
            top = new Color(0x5A5575);
            bottom = new Color(0x474363);
            border = new Color(0x6E6890);
            text = new Color(0x9A94B5);
        } else if (m.isPressed()) {
            top = new Color(0xB6E3EC);
            bottom = new Color(0x8FC9D8);
            border = Theme.TEAL_DARK;
            text = new Color(0x1F2F7A);
        } else if (m.isRollover()) {
            top = new Color(0xF4FEFF);
            bottom = new Color(0xBDEFF5);
            border = Theme.TEAL;
            text = new Color(0x14205E);
            stroke = 3.5f;
        } else {
            top = new Color(0xE6F7FB);
            bottom = new Color(0xBFE3F0);
            border = new Color(0x2F4FA0);
            text = new Color(0x1F2F7A);
        }

        g2.setColor(new Color(0, 0, 0, 80));
        g2.translate(0, 3);
        g2.fillPolygon(shape);
        g2.translate(0, -3);
        g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
        g2.fillPolygon(shape);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(stroke));
        g2.drawPolygon(shape);

        java.awt.Font font = getFont();
        FontMetrics fm = g2.getFontMetrics(font);
        int maxText = w - 2 * c - 8;
        while (fm.stringWidth(getText()) > maxText && font.getSize() > 10) {
            font = font.deriveFont((float) (font.getSize() - 1));
            fm = g2.getFontMetrics(font);
        }
        g2.setFont(font);
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(text);
        g2.drawString(getText(), tx, ty);
        g2.dispose();
    }
}
