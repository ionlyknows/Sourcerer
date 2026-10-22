package sourcerer.model;

/** Data types: int, double, String ... A type cannot be assigned, so it is refused right after '='. */
public class DataTypeTile extends Tile {

    public DataTypeTile(String symbol) {
        super(symbol);
    }

    @Override
    public String getCategory() {
        return "DataType";
    }

    @Override
    public String getTooltip() {
        return "Data type: " + getSymbol();
    }

    @Override
    public String onSelect(CodeAssembly code) {
        if (code.endsWithOperator()) {
            return "A data type can't go after an operator.";
        }
        code.append(getSymbol() + " ");
        return null;
    }
}
