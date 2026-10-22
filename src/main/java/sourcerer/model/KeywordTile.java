package sourcerer.model;

/** Python keywords: if, for, while, in, and, or, not, def, return ... Appended with a trailing space. */
public class KeywordTile extends Tile {

    public KeywordTile(String symbol) {
        super(symbol);
    }

    @Override
    public String getCategory() {
        return "Keyword";
    }

    @Override
    public String getTooltip() {
        return "Python keyword: " + getSymbol();
    }

    @Override
    public String onSelect(CodeAssembly code) {
        code.append(getSymbol() + " ");
        return null;
    }
}
