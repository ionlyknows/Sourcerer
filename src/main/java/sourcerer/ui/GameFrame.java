package sourcerer.ui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import sourcerer.model.LevelBank;
import sourcerer.model.Progress;
import sourcerer.tutorial.TutorialScreen;

/** The single window. Screens are cards; call the show/start methods to navigate. */
public class GameFrame extends JFrame {

    public static final String MENU = "menu";
    public static final String MAP = "map";
    public static final String RPG = "rpg";
    public static final String DICTIONARY = "dictionary";
    public static final String RESULT = "result";
    public static final String TUTORIAL = "tutorial";
    public static final String STORE = "store";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final Progress progress = new Progress();
    private final TutorialOverlay overlay = new TutorialOverlay();
    private final TutorialScreen tutorial = new TutorialScreen(this::showMenu);
    private final RpgScreen rpg;
    private final ResultScreen result;
    private final LevelMapScreen map;
    private final PotionStoreScreen store;
    private String current = MENU;
    private String previous = MENU;

    public GameFrame() {
        super("Sourcerer Crusade - Learn Python");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        rpg = new RpgScreen(this, progress, overlay);
        result = new ResultScreen(this);
        map = new LevelMapScreen(this, progress);
        root.add(new MainMenuScreen(this), MENU);
        root.add(map, MAP);
        root.add(rpg, RPG);
        root.add(new DictionaryScreen(this), DICTIONARY);
        root.add(result, RESULT);
        root.add(tutorial, TUTORIAL);
        store = new PotionStoreScreen(this::showMap, rpg.getPlayer(), progress);
        root.add(store, STORE);

        setContentPane(root);
        getLayeredPane().add(overlay, JLayeredPane.MODAL_LAYER);
        getLayeredPane().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                overlay.setBounds(0, 0, getLayeredPane().getWidth(), getLayeredPane().getHeight());
            }
        });
        setMinimumSize(new Dimension(1050, 720));
        setSize(1200, 800);
        setLocationRelativeTo(null);
        cards.show(root, MENU);
    }

    private void show(String name) {
        if (!name.equals(current)) {
            previous = current;
            current = name;
        }
        overlay.setSuspended(!RPG.equals(name));
        cards.show(root, name);
    }

    public void showMenu() {
        overlay.stop();
        show(MENU);
    }

    public void showMap() {
        overlay.stop();
        map.refresh();
        show(MAP);
    }

    public void showDictionary() { show(DICTIONARY); }
    public void showStore() { store.refresh(); show(STORE); }

    /** Starts a Play-mode level from the map. */
    public void startLevel(int id) {
        rpg.startLevel(LevelBank.get(id));
        show(RPG);
    }

    /** Tutorial mode is its own puzzle screen (no battle, no monsters). */
    public void startTutorial() {
        overlay.stop();
        tutorial.open();
        show(TUTORIAL);
    }

    /** Called when the guided tutorial ends (finished or skipped). */
    public void tutorialFinished(boolean firstTime) {
        progress.setTutorialDone();
        if (firstTime) {
            startLevel(1);
        } else {
            showMap();
        }
    }

    public void showResult(boolean victory, int levelId, boolean lastLevel, int stars, String message) {
        overlay.stop();
        result.showResult(victory, levelId, lastLevel, stars, message);
        show(RESULT);
    }

    /** The dictionary can be opened from the menu or from inside a battle. */
    public void closeDictionary() {
        show(RPG.equals(previous) ? RPG : MENU);
    }

    public void exitGame() {
        dispose();
        System.exit(0);
    }
}
