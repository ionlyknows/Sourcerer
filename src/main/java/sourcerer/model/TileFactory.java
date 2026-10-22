package sourcerer.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Turns a plain Python token such as "print" or "+" into the right Tile subclass. */
public final class TileFactory {

    private static final Set<String> KEYWORDS =
            new HashSet<>(Arrays.asList("if", "elif", "else", "for", "while", "in", "and", "or", "not",
                    "def", "return"));
    private static final Set<String> FUNCTIONS =
            new HashSet<>(Arrays.asList("print", "input", "int", "float", "str", "bool", "len", "range",
                    "type", "append"));
    private static final Set<String> OPERATORS =
            new HashSet<>(Arrays.asList("=", "+", "-", "*", "/", "%", "//", "**", "+=", "-=",
                    "==", "!=", "<", ">", "<=", ">="));
    private static final Set<String> SYNTAX =
            new HashSet<>(Arrays.asList(";", "(", ")", ".", ",", "{", "}", ":", "[", "]"));

    private TileFactory() {
    }

    public static Tile of(String token) {
        if (KEYWORDS.contains(token)) {
            return new KeywordTile(token);
        }
        if (FUNCTIONS.contains(token)) {
            return new FunctionTile(token);
        }
        if (OPERATORS.contains(token)) {
            return new OperatorTile(token);
        }
        if (SYNTAX.contains(token)) {
            return new SyntaxTile(token);
        }
        return new ValueTile(token);
    }
}
