package sourcerer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import sourcerer.model.Battle;
import sourcerer.model.Bug;
import sourcerer.model.CodeAssembly;
import sourcerer.model.Player;
import sourcerer.model.Level;
import sourcerer.model.LevelBank;
import sourcerer.model.Potion;
import sourcerer.model.Progress;
import sourcerer.model.Puzzle;
import sourcerer.model.Tile;

/**
 * RPG Mode: arena (hero / enemy / challenge) on top; actions, puzzle and battle log below.
 * Game rules live in Battle - this class only displays state and forwards clicks.
 */
public class RpgScreen extends ScreenPanel {

    private final GameFrame frame;
    private final Progress progress;
    private final TutorialOverlay overlay;
    private Level currentLevel;
    private boolean tutorialMode;
    private boolean tutorialFirst;
    private boolean submittedOk;
    private final Battle battle = new Battle();
    private final CodeAssembly assembly = new CodeAssembly();
    private final Map<Tile, TileButton> buttons = new IdentityHashMap<>();
    private boolean locked;

    // arena
    private final SpriteComponent heroSprite = new SpriteComponent(SpriteComponent.Kind.HERO);
    private final SpriteComponent enemySprite = new SpriteComponent(SpriteComponent.Kind.SPIDER);
    private final StatBar heroHp = new StatBar("HP", Theme.HP_RED);
    private final StatBar heroMp = new StatBar("MP", Theme.MP_BLUE);
    private final StatBar enemyHp = new StatBar("HP", Theme.HP_RED);
    private final JLabel heroName = Theme.label("", Theme.serif(Font.BOLD, 20), Theme.TEXT);
    private final JLabel enemyName = Theme.label("", Theme.serif(Font.BOLD, 20), Theme.TEXT);
    private final JTextArea promptArea = new JTextArea();
    private final JLabel levelTitle = Theme.label("", Theme.serif(Font.BOLD, 26), Theme.TEAL);
    private final JLabel stepLabel = Theme.label(" ", Theme.sans(Font.BOLD, 14), Theme.CYAN_LIGHT);
    private JPanel challengePanel;
    private JPanel actionsPanel;
    private JPanel assembledPanel;
    private JPanel typeRow;

    // actions
    private final StatBar heroXp = new StatBar("XP", Theme.XP_GOLD);
    private final JLabel levelLabel = Theme.label("", Theme.serif(Font.BOLD, 18), Theme.XP_GOLD);
    private final FantasyButton healBtn = new FantasyButton("Healing Potion", 220, 42);
    private final FantasyButton manaBtn = new FantasyButton("Mana Potion", 220, 42);
    private final FantasyButton skillBtn = new FantasyButton("Syntax Strike", 220, 42);
    private final FantasyButton hintBtn = new FantasyButton("Hint", 220, 42);

    // puzzle
    private final JPanel gridPanel = new JPanel(new GridLayout(3, 4, 10, 10));
    private final JLabel assembledLabel = new JLabel(" ");
    private final JTextField typeField = new JTextField();
    private final JLabel feedbackLabel = new JLabel(" ");
    private final FantasyButton undoBtn = new FantasyButton("Undo", 90, 40);
    private final FantasyButton clearBtn = new FantasyButton("Clear", 90, 40);
    private final FantasyButton submitBtn = new FantasyButton("Submit", 120, 40);

    // log
    private final JTextPane logPane = new JTextPane();

    public RpgScreen(GameFrame frame, Progress progress, TutorialOverlay overlay) {
        super(new Color(0x2A0F2E), new Color(0x0C0814));
        this.frame = frame;
        this.progress = progress;
        this.overlay = overlay;
        progress.restorePlayer(battle.getPlayer());
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        add(buildArena(), BorderLayout.NORTH);
        JPanel bottom = new JPanel(new BorderLayout(12, 0));
        bottom.setOpaque(false);
        bottom.add(buildActions(), BorderLayout.WEST);
        bottom.add(buildPuzzlePanel(), BorderLayout.CENTER);
        bottom.add(buildLogPanel(), BorderLayout.EAST);
        add(bottom, BorderLayout.CENTER);

        wireActions();
        currentLevel = LevelBank.tutorial();
        beginEncounter();
    }

    // ------------------------------------------------------------------ layout

    private JPanel buildArena() {
        JPanel arena = new JPanel(new BorderLayout(12, 0));
        arena.setOpaque(false);
        arena.setPreferredSize(new Dimension(100, 270));
        arena.add(fighterPanel(heroSprite, heroName, heroHp, heroMp), BorderLayout.WEST);
        arena.add(buildChallengePanel(), BorderLayout.CENTER);
        arena.add(fighterPanel(enemySprite, enemyName, enemyHp, null), BorderLayout.EAST);
        return arena;
    }

    private JPanel fighterPanel(SpriteComponent sprite, JLabel name, StatBar hp, StatBar mp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(280, 270));
        p.add(sprite, BorderLayout.CENTER);
        JPanel info = new JPanel(new GridLayout(0, 1, 0, 4));
        info.setOpaque(false);
        name.setHorizontalAlignment(SwingConstants.CENTER);
        info.add(name);
        info.add(hp);
        if (mp != null) {
            info.add(mp);
        }
        p.add(info, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildChallengePanel() {
        RoundPanel p = new RoundPanel(new Color(20, 14, 40, 205), Theme.TEAL, 26);
        p.setLayout(new BorderLayout(0, 8));
        challengePanel = p;
        levelTitle.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(levelTitle, BorderLayout.NORTH);

        promptArea.setEditable(false);
        promptArea.setFocusable(false);
        promptArea.setOpaque(false);
        promptArea.setLineWrap(true);
        promptArea.setWrapStyleWord(true);
        promptArea.setFont(Theme.serif(Font.PLAIN, 21));
        promptArea.setForeground(Theme.TEXT);
        JPanel mid = new JPanel(new BorderLayout(0, 4));
        mid.setOpaque(false);
        mid.add(Theme.label("Python Challenge", Theme.sans(Font.BOLD, 13), Theme.TEXT_MUTED), BorderLayout.NORTH);
        mid.add(promptArea, BorderLayout.CENTER);
        p.add(mid, BorderLayout.CENTER);

        JLabel reward = Theme.label("Reward: +" + Battle.PUZZLE_XP + " XP per solve, +"
                + Battle.VICTORY_XP + " XP for the win", Theme.sans(Font.BOLD, 13), Theme.XP_GOLD);
        reward.setHorizontalAlignment(SwingConstants.CENTER);
        stepLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel south = new JPanel(new GridLayout(0, 1, 0, 2));
        south.setOpaque(false);
        south.add(stepLabel);
        south.add(reward);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildActions() {
        RoundPanel p = new RoundPanel(new Color(20, 14, 40, 215), Theme.PARCHMENT_DARK, 24);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(250, 100));
        actionsPanel = p;

        levelLabel.setAlignmentX(CENTER_ALIGNMENT);
        p.add(levelLabel);
        p.add(Box.createVerticalStrut(4));
        heroXp.setAlignmentX(CENTER_ALIGNMENT);
        heroXp.setMaximumSize(new Dimension(220, 22));
        p.add(heroXp);
        p.add(Box.createVerticalStrut(14));
        JLabel title = Theme.label("Actions", Theme.serif(Font.BOLD, 20), Theme.TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(8));

        FantasyButton dictBtn = new FantasyButton("Python Grimoire", 220, 42);
        dictBtn.addActionListener(e -> frame.showDictionary());
        FantasyButton menuBtn = new FantasyButton("Level Map", 220, 42);
        menuBtn.addActionListener(e -> frame.showMap());

        FantasyButton[] all = {healBtn, manaBtn, skillBtn, hintBtn, dictBtn, menuBtn};
        for (FantasyButton b : all) {
            b.setAlignmentX(CENTER_ALIGNMENT);
            p.add(b);
            p.add(Box.createVerticalStrut(8));
        }
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildPuzzlePanel() {
        RoundPanel p = new RoundPanel(new Color(24, 15, 51, 220), Theme.TEAL, 26);
        p.setLayout(new BorderLayout(0, 10));
        p.add(Theme.label("Python Tiles  -  click tiles in the order they belong",
                Theme.serif(Font.BOLD, 18), Theme.CYAN_LIGHT), BorderLayout.NORTH);

        gridPanel.setOpaque(false);
        p.add(gridPanel, BorderLayout.CENTER);

        RoundPanel assembled = new RoundPanel(new Color(0x120E24), Theme.TEAL_DARK, 16);
        assembled.setLayout(new BorderLayout());
        assembled.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        assembledPanel = assembled;
        assembledLabel.setFont(Theme.mono(Font.BOLD, 20));
        assembled.add(assembledLabel, BorderLayout.CENTER);

        typeField.setFont(Theme.mono(Font.BOLD, 18));
        typeField.setBackground(new Color(0x1C1738));
        typeField.setForeground(Theme.TEXT);
        typeField.setCaretColor(Theme.TEAL);
        typeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.TEAL_DARK, 2),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        typeRow = new JPanel(new BorderLayout(8, 0));
        typeRow.setOpaque(false);
        typeRow.add(Theme.label("Type it:", Theme.serif(Font.BOLD, 18), Theme.TEXT), BorderLayout.WEST);
        typeRow.add(typeField, BorderLayout.CENTER);

        feedbackLabel.setFont(Theme.serif(Font.BOLD, 15));
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);
        actionRow.add(undoBtn);
        actionRow.add(clearBtn);
        actionRow.add(submitBtn);

        JPanel south = new JPanel(new GridLayout(0, 1, 0, 8));
        south.setOpaque(false);
        south.add(assembled);
        south.add(typeRow);
        south.add(feedbackLabel);
        south.add(actionRow);
        south.setPreferredSize(new Dimension(100, 200));
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildLogPanel() {
        RoundPanel p = new RoundPanel(new Color(20, 14, 40, 215), Theme.PARCHMENT_DARK, 24);
        p.setLayout(new BorderLayout(0, 8));
        p.setPreferredSize(new Dimension(290, 100));
        p.add(Theme.label("Battle Log", Theme.serif(Font.BOLD, 20), Theme.TEXT), BorderLayout.NORTH);
        logPane.setEditable(false);
        logPane.setBackground(new Color(0x0F0B1F));
        logPane.setFont(Theme.sans(Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(logPane);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.TEAL_DARK, 2));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private void wireActions() {
        healBtn.addActionListener(e -> usePotion(Potion.HEALING));
        manaBtn.addActionListener(e -> usePotion(Potion.MANA));
        skillBtn.addActionListener(e -> {
            if (locked) return;
            battle.useSkill();
            afterAction();
        });
        hintBtn.addActionListener(e -> {
            if (locked) return;
            appendLog('*', "Hint: " + battle.getPuzzle().getHint());
        });
        undoBtn.addActionListener(e -> undoPick());
        clearBtn.addActionListener(e -> {
            while (assembly.size() > 0) {
                undoPick();
            }
        });
        submitBtn.addActionListener(e -> submit());
        typeField.addActionListener(e -> submit());
    }

    // ------------------------------------------------------------------ game flow

    /** Starts a normal level (shows the short Python lesson card first). */
    public void startLevel(Level lv) {
        overlay.stop();
        tutorialMode = false;
        currentLevel = lv;
        battle.startLevel(lv);
        beginEncounter();
        TutorialOverlay.Step intro = new TutorialOverlay.Step("Level " + lv.getId() + ": " + lv.getTitle(), lv.getBlurb())
                .badge("New Python concept").code(lv.getSampleCode(), lv.getSampleOutput()).button("Let's go!");
        overlay.start(Collections.singletonList(intro), () -> typeField.requestFocusInWindow(), false);
    }

    /** Starts the guided tutorial: a one-puzzle practice fight with spotlights and arrows. */
    public void startTutorial(boolean firstTime) {
        overlay.stop();
        tutorialMode = true;
        tutorialFirst = firstTime;
        submittedOk = false;
        currentLevel = LevelBank.tutorial();
        battle.startLevel(currentLevel);
        beginEncounter();
        overlay.start(buildTutorialSteps(), () -> {
            tutorialMode = false;
            frame.tutorialFinished(tutorialFirst);
        }, true);
    }

    private void beginEncounter() {
        locked = false;
        logPane.setText("");
        loadPuzzle();
        Bug bug = battle.getBug();
        enemySprite.setKind(SpriteComponent.Kind.fromId(bug.getSpriteId()));
        levelTitle.setText(currentLevel.getId() == 0 ? "TRAINING"
                : "LEVEL " + currentLevel.getId() + ": " + currentLevel.getTitle().toUpperCase());
        flushBattleLog();
        refreshStats();
    }

    // ------------------------------------------------------------------ tutorial script

    private List<TutorialOverlay.Step> buildTutorialSteps() {
        List<TutorialOverlay.Step> s = new ArrayList<>();
        s.add(new TutorialOverlay.Step("Welcome, Sourcerer!",
                "Bugs are swarming the forest.\nYour spells are lines of Python code!"));
        s.add(new TutorialOverlay.Step("Read the goal",
                "Every fight starts with a challenge.\nThis one: make a variable x that holds 5.")
                .target(() -> challengePanel));
        s.add(new TutorialOverlay.Step("Pick the tiles",
                "Click the glowing tile each time:\nx, then =, then 5.")
                .target(this::nextNeededTile)
                .waitFor(() -> assembly.size() >= battle.getPuzzle().getSolutionTokens().size()
                        && battle.getPuzzle().matches(assembly.getText())));
        s.add(new TutorialOverlay.Step("Your Python code",
                "Your tiles build real Python here.\nx = 5 stores 5 inside the variable x.")
                .target(() -> assembledPanel).code("x = 5", "x holds 5"));
        s.add(new TutorialOverlay.Step("Type it yourself",
                "Now type the same code in this box.\nTyping builds muscle memory!")
                .target(() -> typeRow)
                .onEnter(() -> typeField.requestFocusInWindow())
                .waitFor(() -> battle.getPuzzle().matches(typeField.getText())));
        s.add(new TutorialOverlay.Step("Cast the spell!",
                "Press Submit (or hit Enter).")
                .target(() -> submitBtn).waitFor(() -> submittedOk));
        s.add(new TutorialOverlay.Step("Direct hit!",
                "Right answers hurt the bug.\nWrong answers hurt YOU, so read carefully.")
                .target(() -> enemyHp));
        s.add(new TutorialOverlay.Step("Earn XP and stars",
                "Every solved puzzle gives XP.\nFewer mistakes on a level means more stars on the map.")
                .target(() -> heroXp));
        s.add(new TutorialOverlay.Step("Stuck? Use your tools",
                "Hint shows the next tile.\nPotions heal you.\nThe Python Grimoire explains syntax.")
                .target(() -> actionsPanel));
        s.add(new TutorialOverlay.Step("You're ready!",
                "Beat each level on the map to unlock the next one.\nLevel 1: Variables awaits!")
                .button("Start Level 1"));
        return s;
    }

    /** The tile the player should click next during the tutorial (null if none). */
    private Component nextNeededTile() {
        List<String> sol = battle.getPuzzle().getSolutionTokens();
        int n = assembly.size();
        if (n >= sol.size()) {
            return null;
        }
        String want = sol.get(n);
        for (Map.Entry<Tile, TileButton> e : buttons.entrySet()) {
            if (!e.getValue().isUsed() && e.getKey().getSymbol().equals(want)) {
                return e.getValue();
            }
        }
        return null;
    }

    private void loadPuzzle() {
        assembly.clear();
        buttons.clear();
        gridPanel.removeAll();
        for (Tile tile : battle.getPuzzle().buildTiles()) {
            TileButton b = new TileButton(tile);
            b.addActionListener(e -> pickTile(b));
            buttons.put(tile, b);
            gridPanel.add(b);
        }
        gridPanel.revalidate();
        gridPanel.repaint();
        typeField.setText("");
        promptArea.setText(battle.getPuzzle().getPrompt());
        stepLabel.setText("Spell " + battle.getPuzzleNumber() + " / " + battle.getPuzzleCount());
        updateAssembled();
        setFeedback(" ", Theme.TEXT);
    }

    private void pickTile(TileButton b) {
        if (locked) return;
        String rejection = assembly.pick(b.getTile());
        if (rejection != null) {
            setFeedback(rejection, Theme.BAD);
            return;
        }
        b.setUsed(true, assembly.size());
        updateAssembled();
        setFeedback(" ", Theme.TEXT);
    }

    private void undoPick() {
        Tile t = assembly.undo();
        if (t != null) {
            buttons.get(t).setUsed(false, 0);
            updateAssembled();
        }
    }

    private void submit() {
        if (locked) return;
        String assembled = assembly.getText();
        String typed = typeField.getText();
        if (assembled.isEmpty()) {
            setFeedback("Pick some tiles first!", Theme.BAD);
            return;
        }
        if (typed.trim().isEmpty()) {
            setFeedback("Now type the code into the text field.", Theme.BAD);
            return;
        }

        Puzzle solved = battle.getPuzzle();
        Battle.Outcome outcome = battle.submit(assembled, typed);
        try {
            progress.savePlayer(battle.getPlayer());
        } catch (IllegalStateException failure) {
            locked = true;
            JOptionPane.showMessageDialog(frame, failure.getMessage(),
                    "Progress could not be saved", JOptionPane.ERROR_MESSAGE);
            frame.showMap();
            return;
        }
        boolean correct = battle.wasLastCorrect();
        if (correct) {
            enemySprite.hit("-" + battle.getLastDamageDealt(), Theme.XP_GOLD);
        } else {
            heroSprite.hit("-" + battle.getLastDamageTaken(), Theme.BAD);
        }

        if (outcome == Battle.Outcome.VICTORY) {
            locked = true;
            if (tutorialMode) {
                flushBattleLog();
                refreshStats();
                setFeedback(html(solved.getExplanation()), Theme.GOOD);
                submittedOk = true;
                return;
            }
            flushBattleLog();
            refreshStats();
            int stars = battle.starsEarned();
            boolean boss = currentLevel.isBoss();
            boolean firstClear;
            try {
                firstClear = progress.settleStage(currentLevel.getId(), stars, battle.getPlayer(), boss);
            } catch (IllegalStateException failure) {
                JOptionPane.showMessageDialog(frame, failure.getMessage(),
                        "Stage result could not be saved", JOptionPane.ERROR_MESSAGE);
                frame.showMap();
                return;
            }
            boolean last = currentLevel.getId() >= LevelBank.count();
            Player p = battle.getPlayer();
            setFeedback(html(solved.getExplanation()), Theme.GOOD);
            endAfterDelay(true, stars, last, "You squashed the " + battle.getBug().getName() + "!<br>"
                    + "Python skill learned: " + currentLevel.getTitle() + "<br>Hero Lv " + p.getLevel()
                    + "  -  XP " + p.getXp() + " / " + p.getXpToNext() + "<br>"
                    + (firstClear ? "First-clear reward: +" + (boss ? 60 : 25) + " coins and +1 Healing Potion"
                    : "Replay complete: first-clear rewards already claimed")
                    + (boss ? "<br>Boss defeated! Total: " + progress.getBossesDefeated() : ""));
            return;
        }
        flushBattleLog();
        refreshStats();
        if (outcome == Battle.Outcome.DEFEAT) {
            if (tutorialMode) {
                battle.startLevel(currentLevel);
                beginEncounter();
                setFeedback("Try again - the practice bug reset!", Theme.BAD);
                return;
            }
            locked = true;
            endAfterDelay(false, 0, false, "The " + battle.getBug().getName()
                    + " overwhelmed you.<br>Review the lesson and try again!");
        } else if (correct) {
            loadPuzzle();
            setFeedback(html(solved.getExplanation()), Theme.GOOD);
        } else {
            setFeedback("Not quite - fix the tiles or your typing and try again.", Theme.BAD);
        }
    }

    private static String html(String text) {
        return "<html>" + text + "</html>";
    }

    private void endAfterDelay(boolean victory, int stars, boolean last, String message) {
        if (!victory) {
            setFeedback("Defeated...", Theme.BAD);
        }
        Timer t = new Timer(victory ? 1800 : 1100,
                e -> frame.showResult(victory, currentLevel.getId(), last, stars, message));
        t.setRepeats(false);
        t.start();
    }

    private void usePotion(Potion type) {
        if (locked) return;
        Player p = battle.getPlayer();
        int hpBefore = p.getHp();
        int mpBefore = p.getMp();
        battle.usePotion(type);
        if (p.getHp() > hpBefore) {
            heroSprite.hit("+" + (p.getHp() - hpBefore), Theme.GOOD);
        } else if (p.getMp() > mpBefore) {
            heroSprite.hit("+" + (p.getMp() - mpBefore), Theme.MP_BLUE.brighter());
        }
        afterAction();
    }

    private void afterAction() {
        try {
            progress.savePlayer(battle.getPlayer());
        } catch (IllegalStateException failure) {
            locked = true;
            JOptionPane.showMessageDialog(frame, failure.getMessage(),
                    "Progress could not be saved", JOptionPane.ERROR_MESSAGE);
            frame.showMap();
            return;
        }
        flushBattleLog();
        refreshStats();
    }

    public Player getPlayer() { return battle.getPlayer(); }

    // ------------------------------------------------------------------ display helpers

    private void refreshStats() {
        Player p = battle.getPlayer();
        Bug bug = battle.getBug();
        heroHp.setValues(p.getHp(), p.getMaxHp());
        heroMp.setValues(p.getMp(), p.getMaxMp());
        heroXp.setValues(p.getXp(), p.getXpToNext());
        enemyHp.setValues(bug.getHp(), bug.getMaxHp());
        heroName.setText(p.getName() + "  Lv " + p.getLevel());
        enemyName.setText(bug.getName());
        levelLabel.setText("Hero Lv " + p.getLevel() + "  |  Coins: " + p.getCoins());

        int heals = p.getPotionCount(Potion.HEALING);
        int manas = p.getPotionCount(Potion.MANA);
        healBtn.setText("Healing Potion x" + heals);
        healBtn.setEnabled(heals > 0);
        manaBtn.setText("Mana Potion x" + manas);
        manaBtn.setEnabled(manas > 0);
        if (battle.isStrikeArmed()) {
            skillBtn.setText("Strike Ready!");
            skillBtn.setEnabled(false);
        } else {
            skillBtn.setText("Syntax Strike (" + Battle.SKILL_COST + " MP)");
            skillBtn.setEnabled(p.getMp() >= Battle.SKILL_COST);
        }
    }

    private void updateAssembled() {
        String code = assembly.getText();
        if (code.isEmpty()) {
            assembledLabel.setForeground(Theme.TEXT_MUTED);
            assembledLabel.setText("assembled code appears here...");
        } else {
            assembledLabel.setForeground(Theme.CYAN_LIGHT);
            assembledLabel.setText(code);
        }
    }

    private void setFeedback(String text, Color color) {
        feedbackLabel.setText(text);
        feedbackLabel.setForeground(color);
    }

    private void flushBattleLog() {
        for (String entry : battle.drainLog()) {
            appendLog(entry.charAt(0), entry.substring(1));
        }
    }

    private void appendLog(char tag, String text) {
        Color color = tag == '+' ? Theme.GOOD : tag == '-' ? Theme.BAD : Theme.CYAN_LIGHT;
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs, color);
        StyleConstants.setBold(attrs, true);
        StyledDocument doc = logPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), text + "\n", attrs);
        } catch (BadLocationException ignored) {
            // cannot happen: we always insert at the end
        }
        logPane.setCaretPosition(doc.getLength());
    }
}
