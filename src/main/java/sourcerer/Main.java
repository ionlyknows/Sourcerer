package sourcerer;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import sourcerer.ui.GameFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new GameFrame().setVisible(true);
            } catch (IllegalStateException failure) {
                JOptionPane.showMessageDialog(null, failure.getMessage(),
                        "Sourcerer could not load saved progress", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
