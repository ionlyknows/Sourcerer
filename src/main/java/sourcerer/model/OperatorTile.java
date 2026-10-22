package sourcerer.model;

/** Operators: = + - * / % == <= >= ... Needs something on its left and glues itself between two tokens. */
public class OperatorTile extends Tile {

    public OperatorTile(String symbol) {
        super(symbol);
    }

    @Override
    public String getCategory() {
        return "Operator";
    }

    @Override
    public String getTooltip() {
        return "Operator: " + getSymbol();
    }

    @Override
    public String onSelect(CodeAssembly code) {
        if (code.isEmpty()) {
            return "An operator needs something on its left first.";
        }
        if (code.endsWithOperator()) {
            return "Two operators in a row won't compile.";
        }
        code.trimEnd();
        code.append(" " + getSymbol() + " ");
        return null;
    }
}
