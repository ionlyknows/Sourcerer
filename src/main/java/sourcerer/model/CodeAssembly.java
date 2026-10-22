package sourcerer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The "assembled code" area: remembers the picked tiles and builds the code text from them. */
public class CodeAssembly {

    private final StringBuilder text = new StringBuilder();
    private final List<Tile> picks = new ArrayList<>();

    /** @return null when accepted, otherwise the rejection message from the tile. */
    public String pick(Tile tile) {
        String rejection = tile.onSelect(this);
        if (rejection == null) {
            picks.add(tile);
        }
        return rejection;
    }

    /** Removes the last picked tile. @return that tile, or null if nothing was picked. */
    public Tile undo() {
        if (picks.isEmpty()) {
            return null;
        }
        Tile last = picks.remove(picks.size() - 1);
        text.setLength(0);
        for (Tile t : picks) {
            t.onSelect(this);
        }
        return last;
    }

    public void clear() {
        picks.clear();
        text.setLength(0);
    }

    // ---- helpers used by the Tile subclasses ----

    public void append(String s) {
        text.append(s);
    }

    public void trimEnd() {
        while (text.length() > 0 && text.charAt(text.length() - 1) == ' ') {
            text.setLength(text.length() - 1);
        }
    }

    public boolean isEmpty() {
        return text.length() == 0;
    }

    public boolean endsWithOperator() {
        int i = text.length() - 1;
        while (i >= 0 && text.charAt(i) == ' ') {
            i--;
        }
        return i >= 0 && "=+-*/%<>!".indexOf(text.charAt(i)) >= 0;
    }

    // ---- read access ----

    public String getText() {
        return text.toString().trim();
    }

    public int size() {
        return picks.size();
    }

    public List<Tile> getPicks() {
        return Collections.unmodifiableList(picks);
    }
}
