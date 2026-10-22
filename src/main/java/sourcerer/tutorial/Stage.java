package sourcerer.tutorial;

import java.util.List;
import java.util.function.Function;

/** One tutorial: a Python concept taught by a short series of puzzle rounds. */
final class Stage {
    final String title;
    final String mechanic;
    final String tip;
    final String summary;
    final List<Function<Board.Ctx, Board>> rounds;

    Stage(String title, String mechanic, String tip, String summary, List<Function<Board.Ctx, Board>> rounds) {
        this.title = title;
        this.mechanic = mechanic;
        this.tip = tip;
        this.summary = summary;
        this.rounds = rounds;
    }
}
