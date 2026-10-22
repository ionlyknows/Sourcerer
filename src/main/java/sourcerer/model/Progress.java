package sourcerer.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import sourcerer.util.JdbcGameStore;
import java.util.Properties;

/** Saved progress: stars per level (0 = not completed) and whether the tutorial was seen. */
public class Progress {

    private final File file;
    private final boolean databaseEnabled;
    private final int[] stars = new int[LevelBank.count() + 2];
    private final Set<Integer> bossesDefeated = new HashSet<>();
    private boolean tutorialDone;
    private int savedLevel = 1;
    private int savedXp;
    private int savedXpToNext = 50;
    private int savedCoins;
    private int savedHealing = 3;
    private int savedMana = 2;

    public Progress() {
        this(new File("sourcerer_progress.properties"), isDatabaseConfigured());
    }

    /** Creates file-backed progress at the supplied path, useful for isolated saves. */
    public Progress(File saveFile) {
        this(saveFile, false);
    }

    private Progress(File saveFile, boolean useDatabase) {
        file = java.util.Objects.requireNonNull(saveFile, "saveFile");
        databaseEnabled = useDatabase;
        if (useDatabase) {
            try {
                if (!JdbcGameStore.load(this)) {
                    // Import an existing local save the first time this database profile is created.
                    load();
                    save();
                }
            } catch (java.sql.SQLException failure) {
                throw new IllegalStateException("Database progress could not be loaded. Check the database setup; "
                        + "the game will not replace it with default data.", failure);
            }
        } else {
            load();
        }
    }

    public boolean isUnlocked(int id) {
        return id == 1 || getStars(id - 1) > 0;
    }

    public boolean isCompleted(int id) {
        return getStars(id) > 0;
    }

    public int getStars(int id) {
        return (id >= 1 && id < stars.length) ? stars[id] : 0;
    }

    public int totalStars() {
        int t = 0;
        for (int s : stars) {
            t += s;
        }
        return t;
    }

    /** First level that is unlocked but not completed (the "you are here" level). */
    public int currentLevel() {
        for (int i = 1; i <= LevelBank.count(); i++) {
            if (!isCompleted(i)) {
                return i;
            }
        }
        return LevelBank.count();
    }

    public void complete(int id, int earned) {
        if (id >= 1 && id < stars.length) {
            stars[id] = Math.max(stars[id], earned);
            save();
        }
    }

    /** Settles a won stage once, retaining the original star-based unlock progression. */
    public boolean settleStage(int id, int earned, Player player, boolean boss) {
        if (id < 1 || id >= stars.length) throw new IllegalArgumentException("Unknown stage: " + id);
        int priorStars = stars[id];
        boolean firstClear = stars[id] == 0;
        boolean newlyDefeatedBoss = boss && !bossesDefeated.contains(id);
        stars[id] = Math.max(stars[id], Math.max(1, Math.min(3, earned)));
        if (firstClear) {
            player.addCoins(boss ? 60 : 25);
            player.addPotion(Potion.HEALING);
        }
        if (boss) bossesDefeated.add(id);
        try {
            capturePlayer(player);
            save();
        } catch (RuntimeException failure) {
            stars[id] = priorStars;
            if (firstClear) {
                player.spendCoins(boss ? 60 : 25);
                player.spendPotion(Potion.HEALING);
            }
            if (newlyDefeatedBoss) bossesDefeated.remove(id);
            capturePlayer(player);
            throw failure;
        }
        return firstClear;
    }

    public boolean isBossDefeated(int stage) { return bossesDefeated.contains(stage); }
    public int getBossesDefeated() { return bossesDefeated.size(); }

    public int getSavedLevel() { return savedLevel; }
    public int getSavedXp() { return savedXp; }
    public int getSavedCoins() { return savedCoins; }
    public int getSavedHealing() { return savedHealing; }
    public int getSavedMana() { return savedMana; }
    public int[] getBossStagesSnapshot() { return bossesDefeated.stream().mapToInt(Integer::intValue).sorted().toArray(); }

    /** Replaces the in-memory state with a verified record loaded from JDBC. */
    public void restoreDatabaseState(int level, int xp, int xpToNext, int coins, boolean tutorialComplete,
            int healing, int mana, int[] databaseStars, int[] databaseBosses) {
        if (databaseStars.length != stars.length) throw new IllegalArgumentException("Unexpected stage snapshot size");
        System.arraycopy(databaseStars, 0, stars, 0, stars.length);
        bossesDefeated.clear();
        Arrays.stream(databaseBosses).forEach(bossesDefeated::add);
        savedLevel = level;
        savedXp = xp;
        savedXpToNext = xpToNext;
        savedCoins = coins;
        tutorialDone = tutorialComplete;
        savedHealing = healing;
        savedMana = mana;
        if (savedLevel < 1 || savedXp < 0 || savedXpToNext < 1 || savedXp >= savedXpToNext
                || savedCoins < 0 || savedHealing < 0 || savedMana < 0) {
            throw new IllegalStateException("Database player values are invalid");
        }
    }

    public void restorePlayer(Player player) {
        player.restoreSavedState(savedLevel, savedXp, savedXpToNext, savedCoins, savedHealing, savedMana);
    }

    /** Saves all persistent player fields after successful gameplay actions. */
    public void savePlayer(Player player) {
        capturePlayer(player);
        save();
    }

    private void capturePlayer(Player player) {
        savedLevel = player.getLevel();
        savedXp = player.getXp();
        savedXpToNext = player.getXpToNext();
        savedCoins = player.getCoins();
        savedHealing = player.getPotionCount(Potion.HEALING);
        savedMana = player.getPotionCount(Potion.MANA);
    }

    /** Charges and grants one potion, then persists; insufficient funds leave state unchanged. */
    public boolean purchasePotion(Player player, Potion type) {
        if (!player.spendCoins(type.getPrice())) return false;
        player.addPotion(type);
        try {
            savePlayer(player);
            return true;
        } catch (RuntimeException failure) {
            // Restore the pre-purchase balance and quantity if local persistence failed.
            player.spendPotion(type);
            player.addCoins(type.getPrice());
            capturePlayer(player);
            throw failure;
        }
    }

    public boolean isTutorialDone() {
        return tutorialDone;
    }

    public void setTutorialDone() {
        tutorialDone = true;
        save();
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            p.load(in);
            tutorialDone = Boolean.parseBoolean(p.getProperty("tutorialDone", "false"));
            for (int i = 1; i <= LevelBank.count(); i++) {
                stars[i] = Integer.parseInt(p.getProperty("stars." + i, "0"));
                if (stars[i] < 0 || stars[i] > 3) {
                    throw new IllegalArgumentException("Invalid stars at stage " + i);
                }
            }
            savedLevel = Integer.parseInt(p.getProperty("player.level", "1"));
            savedXp = Integer.parseInt(p.getProperty("player.xp", "0"));
            savedXpToNext = Integer.parseInt(p.getProperty("player.xpToNext", "50"));
            savedCoins = Integer.parseInt(p.getProperty("player.coins", "0"));
            savedHealing = Integer.parseInt(p.getProperty("inventory.HEALING", "3"));
            savedMana = Integer.parseInt(p.getProperty("inventory.MANA", "2"));
            for (int i = 10; i <= LevelBank.count(); i += 10) {
                if (Boolean.parseBoolean(p.getProperty("boss." + i, "false"))) bossesDefeated.add(i);
            }
            if (savedLevel < 1 || savedXp < 0 || savedXpToNext < 1 || savedXp >= savedXpToNext
                    || savedCoins < 0 || savedHealing < 0 || savedMana < 0) {
                throw new IllegalArgumentException("Saved player values are out of range");
            }
        } catch (IOException | IllegalArgumentException failure) {
            throw new IllegalStateException("Cannot load saved Sourcerer progress from "
                    + file.getAbsolutePath() + "; the save was left untouched.", failure);
        }
    }

    private void save() {
        if (databaseEnabled) {
            try {
                JdbcGameStore.save(this);
                return;
            } catch (java.sql.SQLException failure) {
                throw new IllegalStateException("Database progress could not be saved; no local fallback was used.", failure);
            }
        }
        Properties p = new Properties();
        p.setProperty("tutorialDone", String.valueOf(tutorialDone));
        for (int i = 1; i <= LevelBank.count(); i++) {
            p.setProperty("stars." + i, String.valueOf(stars[i]));
        }
        p.setProperty("player.level", String.valueOf(savedLevel));
        p.setProperty("player.xp", String.valueOf(savedXp));
        p.setProperty("player.xpToNext", String.valueOf(savedXpToNext));
        p.setProperty("player.coins", String.valueOf(savedCoins));
        p.setProperty("inventory.HEALING", String.valueOf(savedHealing));
        p.setProperty("inventory.MANA", String.valueOf(savedMana));
        for (int i = 10; i <= LevelBank.count(); i += 10) {
            p.setProperty("boss." + i, String.valueOf(bossesDefeated.contains(i)));
        }
        File temp = new File(file.getPath() + ".tmp");
        try (OutputStream out = Files.newOutputStream(temp.toPath())) {
            p.store(out, "Sourcerer Crusade progress");
            out.flush();
            try {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException failure) {
            try { Files.deleteIfExists(temp.toPath()); } catch (IOException ignored) { }
            throw new IllegalStateException("Cannot save Sourcerer progress to " + file.getAbsolutePath(), failure);
        }
    }

    private static boolean isDatabaseConfigured() {
        String url = System.getenv("SOURCERER_DB_URL");
        return url != null && !url.trim().isEmpty();
    }
}
