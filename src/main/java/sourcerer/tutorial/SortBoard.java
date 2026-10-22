package sourcerer.tutorial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

/** DATA TYPES mechanic: sort value tiles into the int / float / str / bool boxes. Judged instantly. */
class SortBoard extends Board {

    private static final String[][] CATS = {
            {"int", "whole numbers"}, {"float", "decimal numbers"},
            {"str", "text in quotes"}, {"bool", "True or False"}};

    private final String prompt;
    private final Map<String, String> typeOf = new HashMap<>();
    private final JPanel pool = new JPanel(new GridLayout(0, 3, 10, 10));
    private PieceButton selected;
    private int remaining;

    /** items: {token, type}. Tokens must be unique inside one round. */
    SortBoard(Ctx ctx, String prompt, String[][] items) {
        super(ctx);
        this.prompt = prompt;
        setLayout(new BorderLayout(0, 14));

        JPanel buckets = new JPanel(new GridLayout(1, 4, 10, 0));
        buckets.setOpaque(false);
        for (String[] c : CATS) {
            buckets.add(new Bucket(c[0], c[1]));
        }
        add(buckets, BorderLayout.CENTER);

        List<String[]> shuffled = new ArrayList<>(java.util.Arrays.asList(items));
        Collections.shuffle(shuffled);
        pool.setOpaque(false);
        for (String[] it : shuffled) {
            typeOf.put(it[0], it[1]);
            PieceButton p = new PieceButton(it[0]);
            p.setPreferredSize(new Dimension(100, 58));
            p.addActionListener(e -> clickPiece(p));
            pool.add(p);
            remaining++;
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

    private void drop(Bucket b) {
        if (selected == null) {
            ctx.say("Pick a tile first, then click the box it belongs in.");
            return;
        }
        PieceButton p = selected;
        String token = p.getText();
        String type = typeOf.get(token);
        if (type.equals(b.key)) {
            selected = null;
            pool.remove(p);
            b.put(token);
            remaining--;
            pool.revalidate();
            pool.repaint();
            if (remaining == 0) {
                ctx.solved("Every value is sorted into the right type!");
            } else {
                ctx.say("Yes! " + token + " is type " + type + ".");
            }
        } else {
            p.setSelected(false);
            selected = null;
            ctx.mistake(token + " is not type " + b.key + ". Look at it again!");
        }
    }

    @Override
    String prompt() {
        return prompt;
    }

    @Override
    String actionLabel() {
        return "HINT";
    }

    @Override
    void action() {
        if (selected != null) {
            String type = typeOf.get(selected.getText());
            for (String[] c : CATS) {
                if (c[0].equals(type)) {
                    ctx.say("Hint: " + selected.getText() + " is " + c[1] + " - the " + type + " box.");
                }
            }
        } else {
            ctx.say("int = whole number, float = has a decimal point, str = text in quotes, bool = True/False.");
        }
    }

    private class Bucket extends RoundPanel {
        final String key;
        final List<String> items = new ArrayList<>();
        final JLabel list = new JLabel(" ");

        Bucket(String key, String sub) {
            super(new Color(0x2D2352), Theme.TEAL, 22);
            this.key = key;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            JLabel t = Theme.label(key, Theme.mono(Font.BOLD, 26), Theme.CYAN_LIGHT);
            JLabel s = Theme.label(sub, Theme.sans(Font.PLAIN, 13), Theme.TEXT_MUTED);
            list.setFont(Theme.mono(Font.BOLD, 15));
            list.setForeground(Theme.TEXT);
            t.setAlignmentX(CENTER_ALIGNMENT);
            s.setAlignmentX(CENTER_ALIGNMENT);
            list.setAlignmentX(CENTER_ALIGNMENT);
            add(t);
            add(s);
            add(Box.createVerticalStrut(8));
            add(list);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    drop(Bucket.this);
                }
            });
        }

        void put(String token) {
            items.add(token);
            StringBuilder sb = new StringBuilder("<html><div style='text-align:center'>");
            for (String i : items) {
                sb.append(esc(i)).append("<br>");
            }
            list.setText(sb.append("</div></html>").toString());
        }
    }
}
