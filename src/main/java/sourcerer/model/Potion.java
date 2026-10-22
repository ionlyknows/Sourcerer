package sourcerer.model;

public enum Potion {
    HEALING("Healing Potion", 30, "Restores up to 30 HP", 25),
    MANA("Mana Potion", 25, "Restores up to 25 MP", 20);

    private final String label;
    private final int amount;
    private final String description;
    private final int price;

    Potion(String label, int amount, String description, int price) {
        this.label = label;
        this.amount = amount;
        this.description = description;
        this.price = price;
    }

    public String getLabel() {
        return label;
    }

    public int getAmount() {
        return amount;
    }

    public String getDescription() { return description; }
    public int getPrice() { return price; }
}
