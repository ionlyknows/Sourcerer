package sourcerer.ui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.Random;
import javax.swing.JPanel;

/** Base class for every screen: a vertical gradient with a few twinkling-looking sparkles. */
public class ScreenPanel extends JPanel {

    private final Color top;
    private final Color bottom;
    private final float[] sx = new float[60];
    private final float[] sy = new float[60];
    private final float[] ss = new float[60];

    public ScreenPanel(Color top, Color bottom) {
        this.top = top;
        this.bottom = bottom;
        Random r = new Random(7);
        for (int i = 0; i < sx.length; i++) {
            sx[i] = r.nextFloat();
            sy[i] = r.nextFloat();
            ss[i] = 2 + r.nextFloat() * 5;
        }
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
        g2.fillRect(0, 0, w, h);
        for (int i = 0; i < sx.length; i++) {
            float x = sx[i] * w;
            float y = sy[i] * h;
            float s = ss[i];
            g2.setColor(new Color(190, 240, 255, 60 + (int) (ss[i] * 15)));
            Path2D star = new Path2D.Float();
            star.moveTo(x, y - s);
            star.quadTo(x, y, x + s, y);
            star.quadTo(x, y, x, y + s);
            star.quadTo(x, y, x - s, y);
            star.quadTo(x, y, x, y - s);
            g2.fill(star);
        }
        g2.dispose();
    }
}
