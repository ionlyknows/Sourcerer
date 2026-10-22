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
import sourcerer.model.TileFactory;
import sourcerer.ui.Theme;

/** A parchment puzzle piece. States: normal, hover, selected (AbstractButton selection), ghost (used), locked (correct). */
class PieceButton extends JButton {

    private final boolean mono;
    private final Color ink;
    private boolean ghost;
    private boolean locked;

    PieceButton(String text) {
        this(text, false);
    }

    PieceButton(String text, boolean mono) {
        super(text);
        this.mono = mono;
        this.ink = mono ? Theme.INK : inkFor(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    void setGhost(boolean g) {
        ghost = g;
        setEnabled(!g);
        setSelected(false);
        repaint();
    }

    void setLocked(boolean l) {
        locked = l;
        repaint();
    }

    static Color inkFor(String token) {
        switch (TileFactory.of(token.trim()).getCategory()) {
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
        boolean sel = isSelected();

        Color base = ghost ? new Color(0x8F8670) : Theme.PARCHMENT;
        if (hover) {
            base = new Color(0xF6EBD0);
        }
        if (pressed) {
            base = Theme.PARCHMENT_DARK;
        }
        if (sel) {
            base = new Color(0xFFF2B8);
        }
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(3, 5, w - 6, h - 7, 18, 18);
        g2.setColor(base);
        g2.fillRoundRect(1, 1, w - 6, h - 7, 18, 18);
        Color border = sel ? Theme.XP_GOLD : locked ? Theme.GOOD : hover ? Theme.TEAL
                : ghost ? new Color(0x5A5240) : new Color(0x4A3A7A);
        g2.setStroke(new BasicStroke(sel || hover ? 3.5f : 2.5f));
        g2.setColor(border);
        g2.drawRoundRect(1, 1, w - 6, h - 7, 18, 18);

        int size = mono ? 20 : 26;
        Font f = mono ? Theme.mono(Font.BOLD, size) : Theme.serif(Font.BOLD, size);
        FontMetrics fm = g2.getFontMetrics(f);
        while (fm.stringWidth(getText()) > w - 28 && size > 10) {
            size--;
            f = mono ? Theme.mono(Font.BOLD, size) : Theme.serif(Font.BOLD, size);
            fm = g2.getFontMetrics(f);
        }
        g2.setFont(f);
        g2.setColor(ghost ? new Color(0x4D4636) : ink);
        int tx = mono ? 16 : (w - 5 - fm.stringWidth(getText())) / 2;
        int ty = (h - 6 - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), tx, ty);
        g2.dispose();
    }
}
