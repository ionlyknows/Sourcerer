package sourcerer.model;

/** Literals and names: 5, "Hero", x, println ... Simply appended with a trailing space. */
public class ValueTile extends Tile {

    public ValueTile(String symbol) {
        super(symbol);
    }

    @Override
    public String getCategory() {
        return "Value";
    }

    @Override
    public String getTooltip() {
        return "Value / name: " + getSymbol();
    }

    @Override
    public String onSelect(CodeAssembly code) {
        code.append(getSymbol() + " ");
        return null;
    }
}
