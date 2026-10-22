package sourcerer.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/** A rounded, framed panel used for every card/frame in the game. */
public class RoundPanel extends JPanel {

    private final Color fill;
    private final Color border;
    private final int arc;

    public RoundPanel(Color fill, Color border, int arc) {
        this.fill = fill;
        this.border = border;
        this.arc = arc;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}
