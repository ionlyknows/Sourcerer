package sourcerer.model;

/** Represents null-related errors. Its exception also drains the player's mana. */
public class NullPointer extends Bug {

    public NullPointer() {
        super("Null Pointer", 50, 10, "null");
    }

    @Override
    public String attack(Player target) {
        int dealt = target.takeDamage(getAttackPower());
        target.drainMana(5);
        return "NullPointerException! You take " + dealt + " damage and lose 5 MP.";
    }
}
