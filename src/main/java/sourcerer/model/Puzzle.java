package sourcerer.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** One Python code challenge: a prompt, the solution tokens, distractors and a plain-English explanation. */
public class Puzzle {

    public static final int GRID_SIZE = 12;

    private static final String[] FILLERS = {"x", "y", "5", "+", "==", "(", ")", "\"Hi\"", "3", ":", "print", "=",
            "if", "0", "<", "name"};

    private final String prompt;
    private final List<String> solution;
    private final List<String> distractors;
    private final String explanation;

    public Puzzle(String prompt, String[] solution, String[] distractors, String explanation) {
        this.prompt = prompt;
        this.solution = Arrays.asList(solution);
        this.distractors = Arrays.asList(distractors);
        this.explanation = explanation;
    }

    public String getPrompt() {
        return prompt;
    }

    /** Shown after a correct answer: what the Python code actually does. */
    public String getExplanation() {
        return explanation;
    }

    public List<String> getSolutionTokens() {
        return Collections.unmodifiableList(solution);
    }

    public String getSolutionCode() {
        return String.join(" ", solution);
    }

    public String getHint() {
        return "The first tile you need is \"" + solution.get(0) + "\".";
    }

    /** Fresh, shuffled tiles for the grid: solution + distractors, padded/trimmed to 12. */
    public List<Tile> buildTiles() {
        List<String> tokens = new ArrayList<>(solution);
        for (String d : distractors) {
            if (tokens.size() < GRID_SIZE) {
                tokens.add(d);
            }
        }
        for (String f : FILLERS) {
            if (tokens.size() >= GRID_SIZE) {
                break;
            }
            if (!tokens.contains(f)) {
                tokens.add(f);
            }
        }
        List<Tile> tiles = new ArrayList<>();
        for (String token : tokens) {
            tiles.add(TileFactory.of(token));
        }
        Collections.shuffle(tiles);
        return tiles;
    }

    public boolean matches(String code) {
        return normalize(code).equals(normalize(getSolutionCode()));
    }

    /** Collapses whitespace, ignores spaces around punctuation and treats 'single' quotes like "double" ones. */
    public static String normalize(String s) {
        return s.trim()
                .replace('\'', '"')
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*([^A-Za-z0-9_\\s\"])\\s*", "$1");
    }
}
