package sourcerer.tutorial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import sourcerer.ui.RoundPanel;
import sourcerer.ui.Theme;

/**
 * INPUT/OUTPUT and FUNCTIONS mechanic: put code blocks in running order between an INPUT chip and an
 * OUTPUT chip. The output only appears once the sequence is right.
 */
class SequenceBoard extends Board {

    private final String prompt;
    private final String output;
    private final String[] lines;
    private final SlotButton[] slots;
    private final Map<SlotButton, PieceButton> placed = new HashMap<>();
    private final JLabel outValue = Theme.label("?", Theme.mono(Font.BOLD, 22), Theme.XP_GOLD);

    SequenceBoard(Ctx ctx, String prompt, String input, String output, String... lines) {
        super(ctx);
        this.prompt = prompt;
        this.output = output;
        this.lines = lines;
        setLayout(new BorderLayout(14, 0));

        add(chip("INPUT", Theme.label(input, Theme.mono(Font.BOLD, 22), Theme.GOOD)), BorderLayout.WEST);
        add(chip("OUTPUT", outValue), BorderLayout.EAST);

        int n = lines.length;
        JPanel track = new JPanel(new GridLayout(n, 1, 0, 8));
        track.setOpaque(false);
        slots = new SlotButton[n];
        for (int i = 0; i < n; i++) {
            SlotButton s = new SlotButton(true);
            s.setNumber(i + 1);
            s.setPreferredSize(new Dimension(300, 46));
            s.addActionListener(e -> clickSlot(s));
            slots[i] = s;
            track.add(s);
        }

        List<String> shuffled = new ArrayList<>(java.util.Arrays.asList(lines));
        Collections.shuffle(shuffled);
        JPanel pool = new JPanel(new GridLayout(n, 1, 0, 8));
        pool.setOpaque(false);
        for (String l : shuffled) {
            PieceButton p = new PieceButton(l, true);
            p.setPreferredSize(new Dimension(300, 46));
            p.addActionListener(e -> clickPiece(p));
            pool.add(p);
        }
        JPanel poolWrap = new JPanel(new BorderLayout(0, 4));
        poolWrap.setOpaque(false);
        poolWrap.add(Theme.label("CODE BLOCKS  (click to add)", Theme.sans(Font.BOLD, 12), Theme.TEXT_MUTED),
                BorderLayout.NORTH);
        poolWrap.add(pool, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        top.add(Theme.label("RUNS TOP TO BOTTOM", Theme.sans(Font.BOLD, 12), Theme.TEXT_MUTED), BorderLayout.NORTH);
        top.add(track, BorderLayout.CENTER);
        center.add(top, BorderLayout.NORTH);
        center.add(poolWrap, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel chip(String title, JLabel value) {
        RoundPanel p = new RoundPanel(new Color(0x2D2352), Theme.TEAL, 20);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(120, 100));
        JLabel t = Theme.label(title, Theme.sans(Font.BOLD, 13), Theme.TEAL);
        t.setAlignmentX(CENTER_ALIGNMENT);
        value.setAlignmentX(CENTER_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(10));
        p.add(value);
        JPanel wrap = new JPanel(new java.awt.GridBagLayout());
        wrap.setOpaque(false);
        wrap.add(p);
        return wrap;
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
        ctx.say("All slots are full. Click a slot to take its block back.");
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
                ctx.say("Fill every slot first!");
                return;
            }
        }
        int wrong = 0;
        for (int i = 0; i < slots.length; i++) {
            SlotButton s = slots[i];
            if (s.isGood()) {
                continue;
            }
            if (s.getValue().equals(lines[i])) {
                s.setGood(true);
            } else {
                wrong++;
                PieceButton p = placed.remove(s);
                p.setGhost(false);
                s.setValue(null);
            }
        }
        if (wrong == 0) {
            outValue.setText(output);
            ctx.solved("It runs! The output is " + output + ".");
        } else {
            ctx.mistake("Some blocks are in the wrong order. The correct ones stayed; fix the rest!");
        }
    }
}
