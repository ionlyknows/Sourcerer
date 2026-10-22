package sourcerer.ui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;

/** Colours and fonts in one place. Change them here and every screen follows. */
public final class Theme {

    public static final Color BG_DARK = new Color(0x120E24);
    public static final Color PANEL_DARK = new Color(0x1C1738);
    public static final Color PURPLE = new Color(0x3B2A70);
    public static final Color TEAL = new Color(0x2EC4B6);
    public static final Color TEAL_DARK = new Color(0x1B7F8C);
    public static final Color CYAN_LIGHT = new Color(0x9BE7EE);
    public static final Color PARCHMENT = new Color(0xE8DCC0);
    public static final Color PARCHMENT_DARK = new Color(0xC9B88F);
    public static final Color INK = new Color(0x2B2238);
    public static final Color HP_RED = new Color(0xD64545);
    public static final Color MP_BLUE = new Color(0x3A7BD5);
    public static final Color XP_GOLD = new Color(0xF2C14E);
    public static final Color GOOD = new Color(0x57D68D);
    public static final Color BAD = new Color(0xFF6B6B);
    public static final Color TEXT = new Color(0xF3EEFF);
    public static final Color TEXT_MUTED = new Color(0xA9A0CC);

    private Theme() {
    }

    public static Font serif(int style, int size) {
        return new Font(Font.SERIF, style, size);
    }

    public static Font sans(int style, int size) {
        return new Font(Font.SANS_SERIF, style, size);
    }

    public static Font mono(int style, int size) {
        return new Font(Font.MONOSPACED, style, size);
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }
}
