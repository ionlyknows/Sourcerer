package sourcerer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import sourcerer.model.Player;
import sourcerer.model.Potion;
import sourcerer.model.Progress;

/** Small shop panel backed by the same player inventory used in battles. */
public final class PotionStoreScreen extends ScreenPanel {
    private final Player player;
    private final Progress progress;
    private final JLabel balance = Theme.label("", Theme.serif(Font.BOLD, 24), Theme.XP_GOLD);
    private final JLabel status = Theme.label("", Theme.sans(Font.PLAIN, 16), Color.WHITE);
    private final JLabel[] owned = new JLabel[Potion.values().length];
    private boolean purchaseGuard;

    public PotionStoreScreen(Runnable back, Player player, Progress progress) {
        super(new Color(0x3A2357), new Color(0x100B20));
        this.player = player;
        this.progress = progress;
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(28, 50, 28, 50));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        FantasyButton backButton = new FantasyButton("Back to Map", 180, 44);
        backButton.addActionListener(e -> back.run());
        header.add(backButton, BorderLayout.WEST);
        JLabel title = Theme.label("Potion Store", Theme.serif(Font.BOLD, 38), Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(title, BorderLayout.CENTER);
        header.add(balance, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel listings = new JPanel(new GridLayout(Potion.values().length, 1, 0, 14));
        listings.setOpaque(false);
        for (Potion potion : Potion.values()) {
            JPanel row = new RoundPanel(new Color(32, 22, 54, 230), Theme.TEAL, 18);
            row.setLayout(new BorderLayout(18, 0));
            row.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
            JLabel details = Theme.label("<html><b>" + potion.getLabel() + "</b> — "
                    + potion.getDescription() + "<br>Price: " + potion.getPrice() + " coins</html>",
                    Theme.sans(Font.PLAIN, 18), Color.WHITE);
            row.add(details, BorderLayout.CENTER);
            JLabel count = Theme.label("", Theme.sans(Font.BOLD, 16), Theme.CYAN_LIGHT);
            owned[potion.ordinal()] = count;
            row.add(count, BorderLayout.WEST);
            FantasyButton buy = new FantasyButton("Buy", 150, 48);
            buy.addActionListener(e -> buy(potion));
            row.add(buy, BorderLayout.EAST);
            listings.add(row);
        }
        add(listings, BorderLayout.CENTER);
        status.setHorizontalAlignment(SwingConstants.CENTER);
        add(status, BorderLayout.SOUTH);
        refresh();
    }

    public void refresh() {
        balance.setText("Coins: " + player.getCoins());
        for (Potion potion : Potion.values()) {
            owned[potion.ordinal()].setText("Owned: " + player.getPotionCount(potion));
        }
    }

    private void buy(Potion potion) {
        if (purchaseGuard) return;
        purchaseGuard = true;
        try {
            if (!progress.purchasePotion(player, potion)) {
                status.setText("Not enough coins for " + potion.getLabel() + ".");
                return;
            }
            status.setText("Purchased " + potion.getLabel() + ".");
            refresh();
        } catch (IllegalStateException failure) {
            status.setText("Purchase wasn't saved: " + failure.getMessage());
        } finally {
            javax.swing.Timer rearm = new javax.swing.Timer(350, e -> purchaseGuard = false);
            rearm.setRepeats(false);
            rearm.start();
        }
    }
}
