package sourcerer.model;

/** Represents Java syntax mistakes. Its web hits a little harder than a normal bite. */
public class SyntaxSpider extends Bug {

    public SyntaxSpider() {
        super("Syntax Spider", 40, 8, "spider");
    }

    @Override
    public String attack(Player target) {
        int dealt = target.takeDamage(getAttackPower() + 2);
        return "Syntax Spider spins a Web of Typos! You take " + dealt + " damage.";
    }
}
