package sourcerer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Battle logic for one level. The UI calls these methods and reads the results.
 * Log entries start with a tag character: '+' good, '-' bad, '*' info.
 * A level is won by solving all of its puzzles; the bug's HP bar is kept in sync with that.
 */
public class Battle {

    public enum Outcome { CONTINUE, VICTORY, DEFEAT }

    public static final int SKILL_COST = 15;
    public static final int PUZZLE_XP = 10;
    public static final int VICTORY_XP = 25;

    private Player player;
    private Level level;
    private Bug bug;
    private Puzzle puzzle;
    private int puzzleIndex;
    private int mistakes;
    private boolean strikeArmed;
    private boolean lastCorrect;
    private int lastDamageDealt;
    private int lastDamageTaken;
    private final List<String> log = new ArrayList<>();

    public Battle() {
        startNewGame();
    }

    /** Fresh player (used when the game starts). */
    public void startNewGame() {
        player = new Player("Sourcerer");
        startLevel(LevelBank.tutorial());
        log.clear();
    }

    /** Begins (or restarts) a level. The player keeps XP and potions but is fully healed. */
    public void startLevel(Level lv) {
        level = lv;
        player.restore();
        puzzleIndex = 0;
        mistakes = 0;
        strikeArmed = false;
        bug = lv.createBug();
        puzzle = lv.getPuzzles().get(0);
        log('*', "A wild " + bug.getName() + " appears!");
    }

    // ---- player actions ----

    /** Both the arranged tiles and the typed text must match the puzzle's sample answer. */
    public Outcome submit(String assembled, String typed) {
        lastCorrect = false;
        lastDamageDealt = 0;
        lastDamageTaken = 0;

        if (!puzzle.matches(assembled)) {
            log('-', "Those tiles don't form the right statement!");
            return retaliate();
        }
        if (!puzzle.matches(typed)) {
            log('-', "Tiles are right, but your typed code doesn't match!");
            return retaliate();
        }

        lastCorrect = true;
        boolean last = puzzleIndex + 1 >= level.getPuzzles().size();
        int damage = player.getAttackPower();
        if (strikeArmed) {
            damage *= 2;
            strikeArmed = false;
            log('*', "Syntax Strike doubles your damage!");
        }
        damage = last ? bug.getHp() : Math.min(damage, Math.max(1, bug.getHp() - 1));
        bug.takeDamage(damage);
        lastDamageDealt = damage;
        log('+', "Correct! You hit " + bug.getName() + " for " + damage + " damage.");

        boolean levelUp = player.gainXp(PUZZLE_XP);
        log('*', "+" + PUZZLE_XP + " XP");
        if (last) {
            log('+', bug.getName() + " is squashed!");
            levelUp |= player.gainXp(VICTORY_XP);
            log('*', "+" + VICTORY_XP + " XP victory bonus");
            if (levelUp) {
                log('*', "Level up! You are now level " + player.getLevel() + ".");
            }
            return Outcome.VICTORY;
        }
        if (levelUp) {
            log('*', "Level up! You are now level " + player.getLevel() + ".");
        }
        puzzleIndex++;
        puzzle = level.getPuzzles().get(puzzleIndex);
        return Outcome.CONTINUE;
    }

    private Outcome retaliate() {
        mistakes++;
        int before = player.getHp();
        log('-', bug.attack(player));
        lastDamageTaken = before - player.getHp();
        if (player.isDefeated()) {
            log('-', "You have been defeated...");
            return Outcome.DEFEAT;
        }
        return Outcome.CONTINUE;
    }

    public void usePotion(Potion type) {
        if (player.getPotionCount(type) <= 0) {
            log('-', "No " + type.getLabel() + "s left!");
        } else if (player.usePotion(type)) {
            log('+', "Used a " + type.getLabel() + " (+" + type.getAmount() + ").");
        } else {
            log('-', "That would have no effect right now.");
        }
    }

    /** Placeholder skill: arms "Syntax Strike" so the next correct answer deals double damage. */
    public void useSkill() {
        if (strikeArmed) {
            log('-', "Syntax Strike is already ready.");
        } else if (!player.spendMana(SKILL_COST)) {
            log('-', "Not enough MP for Syntax Strike.");
        } else {
            strikeArmed = true;
            log('+', "Syntax Strike ready! Your next correct answer hits twice as hard.");
        }
    }

    /** Called by the UI after a level victory: potion reward. */
    public void grantLevelReward() {
        player.addPotion(Potion.HEALING);
        log('+', "Reward: +1 Healing Potion");
    }

    /** 3 stars for a clean run, 2 for up to two mistakes, otherwise 1. */
    public int starsEarned() {
        return mistakes == 0 ? 3 : (mistakes <= 2 ? 2 : 1);
    }

    // ---- read access for the UI ----
    public Player getPlayer() { return player; }
    public Level getLevel() { return level; }
    public Bug getBug() { return bug; }
    public Puzzle getPuzzle() { return puzzle; }
    public int getPuzzleNumber() { return puzzleIndex + 1; }
    public int getPuzzleCount() { return level.getPuzzles().size(); }
    public int getMistakes() { return mistakes; }
    public boolean isStrikeArmed() { return strikeArmed; }
    public boolean wasLastCorrect() { return lastCorrect; }
    public int getLastDamageDealt() { return lastDamageDealt; }
    public int getLastDamageTaken() { return lastDamageTaken; }

    public List<String> drainLog() {
        List<String> copy = new ArrayList<>(log);
        log.clear();
        return copy;
    }

    private void log(char tag, String message) {
        log.add(tag + message);
    }
}
