package sourcerer.model;

/** One topic in the Syntax Dictionary. */
public class DictionaryEntry {

    private final String category;
    private final String title;
    private final String formula;
    private final String description;
    private final String code;
    private final String[] tokens;
    private final String output;

    public DictionaryEntry(String category, String title, String formula, String description,
                           String code, String[] tokens, String output) {
        this.category = category;
        this.title = title;
        this.formula = formula;
        this.description = description;
        this.code = code;
        this.tokens = tokens;
        this.output = output;
    }

    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getFormula() { return formula; }
    public String getDescription() { return description; }
    public String getCode() { return code; }
    public String[] getTokens() { return tokens; }
    public String getOutput() { return output; }

    /** Basic search: matches title, category or formula (query must already be lower case). */
    public boolean matches(String query) {
        return title.toLowerCase().contains(query)
                || category.toLowerCase().contains(query)
                || formula.toLowerCase().contains(query);
    }

    @Override
    public String toString() {
        return title;
    }
}
