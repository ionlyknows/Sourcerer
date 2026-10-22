package sourcerer.tutorial;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import sourcerer.ui.Theme;

/** VARIABLES mechanic: click a value, then click the variable box it belongs in. */
class MatchBoard extends Board {

    static final class Row {
        final String left;
        final String answer;
        final String clue;

        Row(String left, String answer, String clue) {
            this.left = left;
            this.answer = answer;
            this.clue = clue;
        }
    }

    private final String prompt;
    private final List<Row> rows;
    private final List<SlotButton> slots = new ArrayList<>();
    private final Map<SlotButton, PieceButton> placed = new HashMap<>();
    private final JPanel pool = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
    private PieceButton selected;

    MatchBoard(Ctx ctx, String prompt, List<Row> rows, String... decoys) {
        super(ctx);
        this.prompt = prompt;
        this.rows = rows;
        setLayout(new BorderLayout(0, 12));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            c.gridy = i;
            c.gridx = 0;
            c.weightx = 0;
            c.anchor = GridBagConstraints.EAST;
            grid.add(Theme.label(r.left, Theme.mono(Font.BOLD, 19), Theme.TEXT), c);
            SlotButton s = new SlotButton(false);
            s.setPreferredSize(new Dimension(130, 54));
            s.addActionListener(e -> clickSlot(s));
            slots.add(s);
            c.gridx = 1;
            c.anchor = GridBagConstraints.CENTER;
            grid.add(s, c);
            c.gridx = 2;
            c.weightx = 1;
            c.anchor = GridBagConstraints.WEST;
            grid.add(Theme.label(r.clue, Theme.sans(Font.ITALIC, 15), Theme.TEXT_MUTED), c);
        }
        add(grid, BorderLayout.CENTER);

        List<String> values = new ArrayList<>();
        for (Row r : rows) {
            values.add(r.answer);
        }
        Collections.addAll(values, decoys);
        Collections.shuffle(values);
        pool.setOpaque(false);
        for (String v : values) {
            PieceButton p = new PieceButton(v);
            p.setPreferredSize(new Dimension(112, 58));
            p.addActionListener(e -> clickPiece(p));
            pool.add(p);
        }
        add(pool, BorderLayout.SOUTH);
    }

    private void clickPiece(PieceButton p) {
        if (selected == p) {
            p.setSelected(false);
            selected = null;
            return;
        }
        if (selected != null) {
            selected.setSelected(false);
        }
        selected = p;
        p.setSelected(true);
    }

    private void clickSlot(SlotButton s) {
        if (s.isGood()) {
            return;
        }
        PieceButton old = placed.remove(s);
        if (old != null) {
            old.setGhost(false);
            s.setValue(null);
        }
        if (selected != null) {
            s.setValue(selected.getText());
            PieceButton p = selected;
            selected = null;
            p.setGhost(true);
            placed.put(s, p);
        }
    }

    @Override
    String prompt() {
        return prompt;
    }

    @Override
    void action() {
        for (SlotButton s : slots) {
            if (s.getValue() == null) {
                ctx.say("Fill every box first! Click a value, then click a box.");
                return;
            }
        }
        int wrong = 0;
        for (int i = 0; i < slots.size(); i++) {
            SlotButton s = slots.get(i);
            if (s.isGood()) {
                continue;
            }
            if (s.getValue().equals(rows.get(i).answer)) {
                s.setGood(true);
            } else {
                wrong++;
                PieceButton p = placed.remove(s);
                p.setGhost(false);
                s.setValue(null);
            }
        }
        if (wrong == 0) {
            ctx.solved("Every variable holds the right value!");
        } else {
            ctx.mistake(wrong + (wrong > 1 ? " boxes were" : " box was")
                    + " wrong and went back to the pool. Try again!");
        }
    }
}
