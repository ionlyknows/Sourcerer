package sourcerer.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import sourcerer.model.Tile;

/** Parchment tile button. Shows hover, pressed and "already used" (with pick order) states. */
public class TileButton extends JButton {

    private final Tile tile;
    private boolean used;
    private int order;

    public TileButton(Tile tile) {
        super(tile.getSymbol());
        this.tile = tile;
        setToolTipText(tile.getTooltip());
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public boolean isUsed() {
        return used;
    }

    public Tile getTile() {
        return tile;
    }

    public void setUsed(boolean used, int order) {
        this.used = used;
        this.order = order;
        setEnabled(!used);
        repaint();
    }

    private Color textColor() {
        switch (tile.getCategory()) {
            case "DataType": return new Color(0x1E9E5A);
            case "Operator": return new Color(0x7A2E8F);
            case "Syntax":   return new Color(0x2F5FD0);
            case "Keyword":  return new Color(0xC0392B);
            case "Function": return new Color(0x1B7F8C);
            default:         return Theme.INK;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        boolean hover = isEnabled() && getModel().isRollover();
        boolean pressed = isEnabled() && getModel().isPressed();

        Color base = used ? new Color(0x8F8670) : Theme.PARCHMENT;
        if (hover) {
            base = new Color(0xF6EBD0);
        }
        if (pressed) {
            base = Theme.PARCHMENT_DARK;
        }
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(3, 5, w - 6, h - 7, 18, 18);
        g2.setColor(base);
        g2.fillRoundRect(1, 1, w - 6, h - 7, 18, 18);
        g2.setStroke(new BasicStroke(hover ? 3.5f : 2.5f));
        g2.setColor(hover ? Theme.TEAL : (used ? new Color(0x5A5240) : new Color(0x4A3A7A)));
        g2.drawRoundRect(1, 1, w - 6, h - 7, 18, 18);

        int size = 26;
        Font f = Theme.serif(Font.BOLD, size);
        FontMetrics fm = g2.getFontMetrics(f);
        while (fm.stringWidth(getText()) > w - 24 && size > 10) {
            size--;
            f = Theme.serif(Font.BOLD, size);
            fm = g2.getFontMetrics(f);
        }
        g2.setFont(f);
        g2.setColor(used ? new Color(0x4D4636) : textColor());
        int tx = (w - 5 - fm.stringWidth(getText())) / 2;
        int ty = (h - 6 - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), tx, ty);

        if (used && order > 0) {
            g2.setColor(Theme.TEAL_DARK);
            g2.fillOval(w - 30, 4, 22, 22);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.sans(Font.BOLD, 12));
            String n = String.valueOf(order);
            g2.drawString(n, w - 19 - g2.getFontMetrics().stringWidth(n) / 2, 20);
        }
        g2.dispose();
    }
}
