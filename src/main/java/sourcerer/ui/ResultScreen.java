package sourcerer.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Victory / defeat screen: next level or retry, plus a button back to the level map. */
public class ResultScreen extends ScreenPanel {

    private final GameFrame frame;
    private final JLabel titleLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel starsLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel messageLabel = new JLabel("", SwingConstants.CENTER);
    private final FantasyButton primary = new FantasyButton("Next Level", 300, 56);
    private boolean victory;
    private int levelId;
    private boolean lastLevel;

    public ResultScreen(GameFrame frame) {
        super(new Color(0x1B1240), new Color(0x07050F));
        this.frame = frame;
        setLayout(new GridBagLayout());

        RoundPanel card = new RoundPanel(new Color(20, 14, 40, 235), Theme.TEAL, 30);
        card.setLayout(new GridLayout(0, 1, 0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(26, 60, 26, 60));
        card.setPreferredSize(new Dimension(680, 520));

        titleLabel.setFont(Theme.serif(Font.BOLD, 60));
        starsLabel.setFont(Theme.serif(Font.BOLD, 44));
        starsLabel.setForeground(Theme.XP_GOLD);
        messageLabel.setFont(Theme.serif(Font.PLAIN, 21));
        messageLabel.setForeground(Theme.TEXT);
        card.add(titleLabel);
        card.add(starsLabel);
        card.add(messageLabel);

        primary.addActionListener(e -> {
            if (this.victory && !lastLevel) {
                frame.startLevel(levelId + 1);
            } else if (this.victory) {
                frame.showMap();
            } else {
                frame.startLevel(levelId);
            }
        });
        FantasyButton map = new FantasyButton("Level Map", 300, 56);
        map.addActionListener(e -> frame.showMap());
        card.add(centered(primary));
        card.add(centered(map));

        add(card, new GridBagConstraints());
    }

    private JPanel centered(FantasyButton b) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(b);
        return p;
    }

    public void showResult(boolean victory, int levelId, boolean lastLevel, int stars, String message) {
        this.victory = victory;
        this.levelId = levelId;
        this.lastLevel = lastLevel;
        titleLabel.setText(victory ? "LEVEL CLEAR!" : "DEFEAT");
        titleLabel.setForeground(victory ? Theme.XP_GOLD : Theme.BAD);
        StringBuilder s = new StringBuilder();
        if (victory) {
            for (int i = 0; i < 3; i++) {
                s.append(i < stars ? "★ " : "☆ ");
            }
        }
        starsLabel.setText(s.toString());
        messageLabel.setText("<html><div style='text-align:center'>" + message + "</div></html>");
        primary.setText(victory ? (lastLevel ? "Back to Map" : "Next Level") : "Try Again");
    }
}
