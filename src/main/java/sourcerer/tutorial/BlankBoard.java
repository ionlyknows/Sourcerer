package sourcerer.tutorial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import sourcerer.ui.RoundPanel;
import sourcerer.ui.Theme;

/**
 * LOOPS / FUNCTIONS mechanic: a code template with ___ blanks. Fill them so the code produces the goal output.
 */
class BlankBoard extends Board {

    private final String prompt;
    private final String goal;
    private final String[] answers;
    private final List<SlotButton> slots = new ArrayList<>();
    private final Map<SlotButton, PieceButton> placed = new HashMap<>();

    BlankBoard(Ctx ctx, String prompt, String goal, String[] template, String[] answers, String... decoys) {
        super(ctx);
        this.prompt = prompt;
        this.goal = goal;
        this.answers = answers;
        setLayout(new BorderLayout(0, 12));

        RoundPanel goalBox = new RoundPanel(new Color(0x2D2352), Theme.TEAL, 18);
        goalBox.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 2));
        goalBox.add(Theme.label("GOAL OUTPUT:", Theme.sans(Font.BOLD, 14), Theme.TEAL));
        goalBox.add(Theme.label(goal, Theme.mono(Font.BOLD, 22), Theme.XP_GOLD));
        add(goalBox, BorderLayout.NORTH);

        RoundPanel code = new RoundPanel(new Color(0x0F1620), Theme.TEAL_DARK, 18);
        code.setLayout(new BoxLayout(code, BoxLayout.Y_AXIS));
        for (String line : template) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 4));
            row.setOpaque(false);
            row.setAlignmentX(LEFT_ALIGNMENT);
            String[] parts = line.split("___", -1);
            for (int i = 0; i < parts.length; i++) {
                if (!parts[i].isEmpty()) {
                    row.add(Theme.label(parts[i], Theme.mono(Font.BOLD, 22), Theme.CYAN_LIGHT));
                }
                if (i < parts.length - 1) {
                    SlotButton s = new SlotButton(false);
                    s.setPreferredSize(new Dimension(96, 44));
                    s.addActionListener(e -> clickSlot(s));
                    slots.add(s);
                    row.add(s);
                }
            }
            code.add(row);
        }
        JPanel codeWrap = new JPanel(new BorderLayout());
        codeWrap.setOpaque(false);
        codeWrap.add(code, BorderLayout.NORTH);
        add(codeWrap, BorderLayout.CENTER);

        List<String> tokens = new ArrayList<>(java.util.Arrays.asList(answers));
        Collections.addAll(tokens, decoys);
        Collections.shuffle(tokens);
        JPanel pool = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        pool.setOpaque(false);
        for (String t : tokens) {
            PieceButton p = new PieceButton(t);
            p.setPreferredSize(new Dimension(92, 54));
            p.addActionListener(e -> clickPiece(p));
            pool.add(p);
        }
        add(pool, BorderLayout.SOUTH);
    }

    private void clickPiece(PieceButton p) {
        for (SlotButton s : slots) {
            if (s.getValue() == null) {
                s.setValue(p.getText());
                p.setGhost(true);
                placed.put(s, p);
                return;
            }
        }
        ctx.say("All blanks are full. Click a filled blank to take its tile back.");
    }

    private void clickSlot(SlotButton s) {
        if (s.isGood()) {
            return;
        }
        PieceButton p = placed.remove(s);
        if (p != null) {
            p.setGhost(false);
            s.setValue(null);
        }
    }

    @Override
    String prompt() {
        return prompt;
    }

    @Override
    String actionLabel() {
        return "RUN";
    }

    @Override
    void action() {
        for (SlotButton s : slots) {
            if (s.getValue() == null) {
                ctx.say("Fill every blank first!");
                return;
            }
        }
        int wrong = 0;
        for (int i = 0; i < slots.size(); i++) {
            SlotButton s = slots.get(i);
            if (s.isGood()) {
                continue;
            }
            if (s.getValue().equals(answers[i])) {
                s.setGood(true);
            } else {
                wrong++;
                PieceButton p = placed.remove(s);
                p.setGhost(false);
                s.setValue(null);
            }
        }
        if (wrong == 0) {
            ctx.solved("It runs and prints: " + goal);
        } else {
            ctx.mistake("Not quite - " + wrong + " blank" + (wrong > 1 ? "s" : "")
                    + " didn't produce the goal. They went back to the pool.");
        }
    }
}
