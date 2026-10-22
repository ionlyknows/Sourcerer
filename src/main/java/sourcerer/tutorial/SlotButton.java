package sourcerer.tutorial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import sourcerer.ui.Theme;

/** A drop slot: dashed outline when empty, a tile when filled, green border once confirmed correct. */
class SlotButton extends JButton {

    private final boolean mono;
    private String value;
    private boolean good;
    private int number;

    SlotButton(boolean mono) {
        this.mono = mono;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    String getValue() { return value; }
    void setValue(String v) { value = v; repaint(); }
    boolean isGood() { return good; }
    void setGood(boolean g) { good = g; repaint(); }
    void setNumber(int n) { number = n; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        boolean hover = getModel().isRollover() && !good;
        if (value == null) {
            g2.setColor(new Color(255, 255, 255, hover ? 45 : 22));
            g2.fillRoundRect(1, 1, w - 4, h - 4, 18, 18);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                    new float[]{8f, 6f}, 0f));
            g2.setColor(hover ? Theme.TEAL : new Color(0x9BE7EE));
            g2.drawRoundRect(1, 1, w - 4, h - 4, 18, 18);
        } else {
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRoundRect(3, 5, w - 6, h - 7, 18, 18);
            g2.setColor(good ? new Color(0xDDF5E3) : hover ? new Color(0xF6EBD0) : Theme.PARCHMENT);
            g2.fillRoundRect(1, 1, w - 6, h - 7, 18, 18);
            g2.setStroke(new BasicStroke(good ? 3.5f : 2.5f));
            g2.setColor(good ? Theme.GOOD : new Color(0x4A3A7A));
            g2.drawRoundRect(1, 1, w - 6, h - 7, 18, 18);

            int left = number > 0 ? 44 : 16;
            int size = mono ? 20 : 26;
            Font f = mono ? Theme.mono(Font.BOLD, size) : Theme.serif(Font.BOLD, size);
            FontMetrics fm = g2.getFontMetrics(f);
            while (fm.stringWidth(value) > w - left - 12 && size > 10) {
                size--;
                f = mono ? Theme.mono(Font.BOLD, size) : Theme.serif(Font.BOLD, size);
                fm = g2.getFontMetrics(f);
            }
            g2.setFont(f);
            g2.setColor(mono ? Theme.INK : PieceButton.inkFor(value));
            int tx = mono ? left : (w - 5 - fm.stringWidth(value)) / 2;
            int ty = (h - 6 - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(value, tx, ty);
        }
        if (number > 0) {
            g2.setColor(Theme.TEAL_DARK);
            g2.fillOval(8, h / 2 - 14, 26, 26);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.sans(Font.BOLD, 14));
            String n = String.valueOf(number);
            g2.drawString(n, 21 - g2.getFontMetrics().stringWidth(n) / 2, h / 2 + 5);
        }
        g2.dispose();
    }
}
