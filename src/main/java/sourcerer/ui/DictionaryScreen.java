package sourcerer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import sourcerer.model.DictionaryEntry;
import sourcerer.model.SyntaxDictionary;
import sourcerer.model.TileFactory;

public class DictionaryScreen extends ScreenPanel {

    private static final Color BOOK = new Color(0xF3E6E9);
    private static final Color BOOK_EDGE = new Color(0x3A2442);

    private final DefaultListModel<DictionaryEntry> model = new DefaultListModel<>();
    private final JList<DictionaryEntry> list = new JList<>(model);
    private final JTextField search = new JTextField();

    private final JLabel categoryLabel = Theme.label("", Theme.sans(java.awt.Font.BOLD, 13), new Color(0x7A5C8A));
    private final JLabel titleLabel = Theme.label("", Theme.serif(java.awt.Font.BOLD, 34), Theme.INK);
    private final JLabel formulaLabel = Theme.label("", Theme.mono(java.awt.Font.BOLD, 16), new Color(0x4A3A7A));
    private final JTextArea descArea = new JTextArea();
    private final JTextArea codeArea = new JTextArea();
    private final JPanel tilesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    private final JLabel outputLabel = Theme.label("", Theme.mono(java.awt.Font.BOLD, 15), Theme.INK);

    public DictionaryScreen(GameFrame frame) {
        super(new Color(0x1A1030), new Color(0x07050F));
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(Theme.label("Python Grimoire", Theme.serif(java.awt.Font.BOLD, 40), Color.WHITE), BorderLayout.WEST);
        FantasyButton back = new FantasyButton("Back", 160, 46);
        back.addActionListener(e -> frame.closeDictionary());
        header.add(back, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        RoundPanel book = new RoundPanel(BOOK, BOOK_EDGE, 26);
        book.setLayout(new BorderLayout(18, 0));
        book.add(buildLeft(), BorderLayout.WEST);
        book.add(buildDetail(), BorderLayout.CENTER);
        add(book, BorderLayout.CENTER);

        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        filter();
    }

    private JPanel buildLeft() {
        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(330, 100));

        JPanel searchRow = new JPanel(new BorderLayout(0, 4));
        searchRow.setOpaque(false);
        searchRow.add(Theme.label("Search topics", Theme.sans(java.awt.Font.BOLD, 13), Theme.INK), BorderLayout.NORTH);
        search.setFont(Theme.sans(java.awt.Font.PLAIN, 16));
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x7A5C8A), 2),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        searchRow.add(search, BorderLayout.CENTER);
        left.add(searchRow, BorderLayout.NORTH);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(52);
        list.setBackground(new Color(0xE6D3DA));
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object value, int index,
                                                          boolean selected, boolean focus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(l, value, index, selected, focus);
                DictionaryEntry e = (DictionaryEntry) value;
                String sub = selected ? "#D6F5F2" : "#6A5A7A";
                c.setText("<html><b>" + e.getTitle() + "</b><br><font color='" + sub + "'>"
                        + e.getCategory() + "</font></html>");
                c.setFont(Theme.sans(java.awt.Font.PLAIN, 15));
                c.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 8));
                if (selected) {
                    c.setBackground(new Color(0x3A2442));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(new Color(0xF8EDF0));
                    c.setForeground(Theme.INK);
                }
                return c;
            }
        });
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showEntry(list.getSelectedValue());
            }
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0x7A5C8A), 2));
        left.add(scroll, BorderLayout.CENTER);
        return left;
    }

    private JPanel buildDetail() {
        JPanel d = new JPanel(new GridBagLayout());
        d.setOpaque(false);

        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setOpaque(false);
        descArea.setFont(Theme.serif(java.awt.Font.PLAIN, 19));
        descArea.setForeground(Theme.INK);

        codeArea.setEditable(false);
        codeArea.setFocusable(false);
        codeArea.setOpaque(false);
        codeArea.setFont(Theme.mono(java.awt.Font.BOLD, 17));
        codeArea.setForeground(Theme.CYAN_LIGHT);
        RoundPanel codeBox = new RoundPanel(new Color(0x1C2530), new Color(0x1C2530), 14);
        codeBox.setLayout(new BorderLayout());
        codeBox.add(codeArea, BorderLayout.CENTER);

        tilesRow.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(2, 0, 2, 0);
        int row = 0;
        c.gridy = row++; d.add(categoryLabel, c);
        c.gridy = row++; d.add(titleLabel, c);
        c.gridy = row++; d.add(formulaLabel, c);
        c.gridy = row++; c.insets = new Insets(14, 0, 2, 0);
        d.add(Theme.label("FUNCTION & MECHANICS", Theme.sans(java.awt.Font.BOLD, 12), new Color(0x7A5C8A)), c);
        c.gridy = row++; c.insets = new Insets(2, 0, 2, 0); d.add(descArea, c);
        c.gridy = row++; c.insets = new Insets(14, 0, 2, 0);
        d.add(Theme.label("PYTHON CODE", Theme.sans(java.awt.Font.BOLD, 12), new Color(0x7A5C8A)), c);
        c.gridy = row++; c.insets = new Insets(2, 0, 2, 0); d.add(codeBox, c);
        c.gridy = row++; c.insets = new Insets(14, 0, 2, 0);
        d.add(Theme.label("EXAMPLE TILE SEQUENCE", Theme.sans(java.awt.Font.BOLD, 12), new Color(0x7A5C8A)), c);
        c.gridy = row++; c.insets = new Insets(2, 0, 2, 0); d.add(tilesRow, c);
        c.gridy = row++; d.add(outputLabel, c);
        c.gridy = row; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        d.add(new JPanel() {{ setOpaque(false); }}, c);
        return d;
    }

    private void filter() {
        String q = search.getText().trim().toLowerCase();
        model.clear();
        for (DictionaryEntry e : SyntaxDictionary.entries()) {
            if (q.isEmpty() || e.matches(q)) {
                model.addElement(e);
            }
        }
        if (model.isEmpty()) {
            showEntry(null);
        } else {
            list.setSelectedIndex(0);
        }
    }

    private void showEntry(DictionaryEntry e) {
        tilesRow.removeAll();
        if (e == null) {
            categoryLabel.setText("");
            titleLabel.setText("No topics found");
            formulaLabel.setText("");
            descArea.setText("Try a different search word.");
            codeArea.setText("");
            outputLabel.setText("");
        } else {
            categoryLabel.setText(e.getCategory().toUpperCase());
            titleLabel.setText(e.getTitle());
            formulaLabel.setText("Syntax formula:  " + e.getFormula());
            descArea.setText(e.getDescription());
            codeArea.setText(e.getCode());
            for (String token : e.getTokens()) {
                TileButton t = new TileButton(TileFactory.of(token));
                t.setEnabled(false);
                t.setPreferredSize(new Dimension(Math.max(52, 14 * token.length() + 24), 46));
                tilesRow.add(t);
            }
            outputLabel.setText("Evaluated output:  " + e.getOutput());
        }
        tilesRow.revalidate();
        tilesRow.repaint();
    }
}
