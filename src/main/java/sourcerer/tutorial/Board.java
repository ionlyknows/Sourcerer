package sourcerer.tutorial;

import javax.swing.JPanel;

/**
 * One tutorial round. Every mechanic (match, sort, sequence, choose, fill) is a Board.
 * A Board never knows about scoring or screens - it only talks to the Ctx it was given.
 */
abstract class Board extends JPanel {

    /** Callbacks to the tutorial screen. */
    interface Ctx {
        void solved(String message);
        void mistake(String message);
        void say(String message);
    }

    protected final Ctx ctx;

    protected Board(Ctx ctx) {
        this.ctx = ctx;
        setOpaque(false);
    }

    /** Short instruction shown above the board. */
    abstract String prompt();

    /** Text of the big button under the board. */
    String actionLabel() {
        return "SUBMIT";
    }

    /** Called when the big button is pressed. */
    abstract void action();

    static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
