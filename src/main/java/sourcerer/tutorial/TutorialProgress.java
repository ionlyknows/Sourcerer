package sourcerer.tutorial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Stars per tutorial stage, saved in its own file (separate from Play-mode progress). */
final class TutorialProgress {

    private static final File FILE = new File("sourcerer_tutorial.properties");
    private final int[] stars = new int[StageBank.count()];

    TutorialProgress() {
        if (!FILE.exists()) {
            return;
        }
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(FILE)) {
            p.load(in);
            for (int i = 0; i < stars.length; i++) {
                stars[i] = Integer.parseInt(p.getProperty("stars." + i, "0"));
            }
        } catch (IOException | NumberFormatException ignored) {
            // corrupt save: start fresh
        }
    }

    int getStars(int idx) {
        return (idx >= 0 && idx < stars.length) ? stars[idx] : 0;
    }

    boolean isUnlocked(int idx) {
        return idx == 0 || getStars(idx - 1) > 0;
    }

    int total() {
        int t = 0;
        for (int s : stars) {
            t += s;
        }
        return t;
    }

    void complete(int idx, int earned) {
        stars[idx] = Math.max(stars[idx], earned);
        Properties p = new Properties();
        for (int i = 0; i < stars.length; i++) {
            p.setProperty("stars." + i, String.valueOf(stars[i]));
        }
        try (FileOutputStream out = new FileOutputStream(FILE)) {
            p.store(out, "Sourcerer Crusade tutorial progress");
        } catch (IOException ignored) {
            // progress simply isn't saved
        }
    }
}
