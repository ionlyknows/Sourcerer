package sourcerer.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class MainMenuScreen extends ScreenPanel {

    public MainMenuScreen(GameFrame frame) {
        super(new Color(0x2B1B63), new Color(0x0D0920));
        setLayout(new GridBagLayout());

        SpriteComponent hero = new SpriteComponent(SpriteComponent.Kind.HERO);
        hero.setPreferredSize(new Dimension(380, 380));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        TitleArt title = new TitleArt();
        title.setAlignmentX(CENTER_ALIGNMENT);
        right.add(title);
        right.add(Box.createVerticalStrut(24));

        right.add(menuButton("Play", frame::showMap));
        right.add(Box.createVerticalStrut(14));
        right.add(menuButton("Tutorial", frame::startTutorial));
        right.add(Box.createVerticalStrut(14));
        right.add(menuButton("Syntax Dictionary", frame::showDictionary));
        right.add(Box.createVerticalStrut(14));
        right.add(menuButton("Exit", frame::exitGame));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 30);
        add(hero, c);
        c.gridx = 1;
        c.insets = new Insets(0, 0, 0, 0);
        add(right, c);
    }

    private FantasyButton menuButton(String text, Runnable action) {
        FantasyButton b = new FantasyButton(text, 300, 58);
        b.setAlignmentX(CENTER_ALIGNMENT);
        b.addActionListener(e -> action.run());
        return b;
    }

    /** Glowing two-line game title. */
    private static class TitleArt extends JComponent {

        TitleArt() {
            setPreferredSize(new Dimension(560, 230));
            setMaximumSize(new Dimension(560, 230));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font f = Theme.serif(Font.BOLD, 70);
            drawGlowText(g2, "SOURCERER", f, 82);
            drawGlowText(g2, "CRUSADE", f, 160);
            g2.setFont(Theme.serif(Font.ITALIC, 20));
            g2.setColor(Theme.TEXT_MUTED);
            String tag = "Learn Python by Playing";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(tag, (getWidth() - fm.stringWidth(tag)) / 2, 205);
            g2.dispose();
        }

        private void drawGlowText(Graphics2D g2, String text, Font font, int baseline) {
            FontRenderContext frc = g2.getFontRenderContext();
            GlyphVector gv = font.createGlyphVector(frc, text);
            int w = (int) gv.getVisualBounds().getWidth();
            Shape shape = gv.getOutline((getWidth() - w) / 2f, baseline);
            for (int r = 12; r >= 2; r -= 2) {
                g2.setStroke(new BasicStroke(r, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(60, 220, 230, 22));
                g2.draw(shape);
            }
            g2.setPaint(new GradientPaint(0, baseline - 60, Color.WHITE, 0, baseline, Theme.CYAN_LIGHT));
            g2.fill(shape);
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(new Color(0x14205E));
            g2.draw(shape);
        }
    }
}
