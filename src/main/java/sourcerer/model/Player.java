package sourcerer.model;

import java.util.EnumMap;

/**
 * ENCAPSULATION: HP and MP are private. Everything else (enemies, potions, skills)
 * has to go through the public methods below, which enforce the rules.
 * ABSTRACTION: the UI just calls usePotion(); the details stay in here.
 */
public class Player {

    private final String name;
    private int hp;
    private int maxHp = 100;
    private int mp;
    private int maxMp = 50;
    private int level = 1;
    private int xp = 0;
    private int xpToNext = 50;
    private int coins = 0;
    private final int armor = 2; // placeholder for the "Mechanical Keyboard" from the presentation
    private final EnumMap<Potion, Integer> potions = new EnumMap<>(Potion.class);

    public Player(String name) {
        this.name = name;
        this.hp = maxHp;
        this.mp = maxMp;
        potions.put(Potion.HEALING, 3);
        potions.put(Potion.MANA, 2);
    }

    // ---- read access ----
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getMp() { return mp; }
    public int getMaxMp() { return maxMp; }
    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getXpToNext() { return xpToNext; }
    public int getCoins() { return coins; }
    public int getPotionCount(Potion type) { return potions.get(type); }
    public boolean isDefeated() { return hp <= 0; }

    /** Base damage dealt by a correct solution. */
    public int getAttackPower() {
        return 12 + level * 2;
    }

    // ---- controlled updates ----

    /** Full heal at the start of each level. */
    public void restore() {
        hp = maxHp;
        mp = maxMp;
    }

    public void addPotion(Potion type) {
        potions.put(type, potions.get(type) + 1);
    }

    /** Removes one owned potion, returning false when inventory is empty. */
    public boolean spendPotion(Potion type) {
        int count = potions.get(type);
        if (count <= 0) return false;
        potions.put(type, count - 1);
        return true;
    }

    /** Adds earned currency. Invalid negative rewards are ignored. */
    public void addCoins(int amount) {
        if (amount > 0 && coins <= Integer.MAX_VALUE - amount) coins += amount;
    }

    /** Purchases are charged only when the balance is sufficient. */
    public boolean spendCoins(int amount) {
        if (amount <= 0 || coins < amount) return false;
        coins -= amount;
        return true;
    }

    /** Loads a previously saved game state after validating every value. */
    public void restoreSavedState(int savedLevel, int savedXp, int savedXpToNext,
            int savedCoins, int healing, int mana) {
        if (savedLevel < 1 || savedXp < 0 || savedXpToNext < 1 || savedXp >= savedXpToNext
                || savedCoins < 0 || healing < 0 || mana < 0) {
            throw new IllegalArgumentException("Invalid saved player state");
        }
        level = savedLevel;
        xp = savedXp;
        xpToNext = savedXpToNext;
        maxHp = 100 + (savedLevel - 1) * 10;
        maxMp = 50 + (savedLevel - 1) * 5;
        hp = maxHp;
        mp = maxMp;
        coins = savedCoins;
        potions.put(Potion.HEALING, healing);
        potions.put(Potion.MANA, mana);
    }


    /** Armor is checked here before HP is reduced. @return the damage actually taken. */
    public int takeDamage(int amount) {
        int actual = Math.max(1, amount - armor);
        hp = Math.max(0, hp - actual);
        return actual;
    }

    public void drainMana(int amount) {
        mp = Math.max(0, mp - amount);
    }

    public boolean spendMana(int cost) {
        if (mp < cost) {
            return false;
        }
        mp -= cost;
        return true;
    }

    /** @return false if there is no potion left or it would have no effect (nothing is consumed). */
    public boolean usePotion(Potion type) {
        if (potions.get(type) <= 0) {
            return false;
        }
        if (type == Potion.HEALING && hp >= maxHp) {
            return false;
        }
        if (type == Potion.MANA && mp >= maxMp) {
            return false;
        }
        potions.put(type, potions.get(type) - 1);
        if (type == Potion.HEALING) {
            hp = Math.min(maxHp, hp + type.getAmount());
        } else {
            mp = Math.min(maxMp, mp + type.getAmount());
        }
        return true;
    }

    /** @return true if the player levelled up. */
    public boolean gainXp(int amount) {
        xp += amount;
        boolean levelledUp = false;
        while (xp >= xpToNext) {
            xp -= xpToNext;
            level++;
            xpToNext += 25;
            maxHp += 10;
            maxMp += 5;
            hp = Math.min(maxHp, hp + 10);
            mp = Math.min(maxMp, mp + 5);
            levelledUp = true;
        }
        return levelledUp;
    }
}
