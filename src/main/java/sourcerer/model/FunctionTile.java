package sourcerer.model;

/** Python built-in functions and methods: print, input, int, str, len, range, append ... */
public class FunctionTile extends Tile {

    public FunctionTile(String symbol) {
        super(symbol);
    }

    @Override
    public String getCategory() {
        return "Function";
    }

    @Override
    public String getTooltip() {
        return "Python function: " + getSymbol();
    }

    @Override
    public String onSelect(CodeAssembly code) {
        code.append(getSymbol() + " ");
        return null;
    }
}
