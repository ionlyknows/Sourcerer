package sourcerer.model;

/** Punctuation: ( ) [ ] . , : ... Sticks to the token on its left (no space before it). */
public class SyntaxTile extends Tile {

    public SyntaxTile(String symbol) {
        super(symbol);
    }

    @Override
    public String getCategory() {
        return "Syntax";
    }

    @Override
    public String getTooltip() {
        return "Syntax symbol: " + getSymbol();
    }

    @Override
    public String onSelect(CodeAssembly code) {
        String s = getSymbol();
        boolean opener = s.equals("(") || s.equals("[");
        if (!(opener && code.endsWithOperator())) {
            code.trimEnd();
        }
        code.append(s);
        if (s.equals(",") || s.equals(":")) {
            code.append(" ");
        }
        return null;
    }
}
