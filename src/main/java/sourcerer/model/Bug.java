package sourcerer.model;

/**
 * INHERITANCE: parent class holding the traits every enemy shares
 * (HP, attack power, sprite id). Subclasses add their own abilities.
 */
public abstract class Bug {

    private final String name;
    private final int maxHp;
    private int hp;
    private final int attackPower;
    private final String spriteId;

    protected Bug(String name, int maxHp, int attackPower, String spriteId) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attackPower = attackPower;
        this.spriteId = spriteId;
    }

    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttackPower() { return attackPower; }
    public String getSpriteId() { return spriteId; }
    public boolean isDefeated() { return hp <= 0; }

    public void takeDamage(int amount) {
        hp = Math.max(0, hp - amount);
    }

    /** Attacks the player. Subclasses override this with their special ability. @return a log message. */
    public String attack(Player target) {
        int dealt = target.takeDamage(attackPower);
        return name + " bites you for " + dealt + " damage.";
    }
}
