package sourcerer.model;

/**
 * POLYMORPHISM: base class for every puzzle tile.
 * The game only ever calls onSelect(); each subclass decides how it behaves.
 */
public abstract class Tile {

    private final String symbol;

    protected Tile(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /** Short label used for colouring in the UI: "Value", "DataType", "Operator" or "Syntax". */
    public abstract String getCategory();

    public abstract String getTooltip();

    /**
     * Called when the player clicks this tile.
     * @return null if the pick was accepted, otherwise a short message explaining the rejection.
     */
    public abstract String onSelect(CodeAssembly code);
}
