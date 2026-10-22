package sourcerer.model;

import java.util.List;

/** One level on the map: a Python concept, a short lesson card and a list of puzzles. */
public class Level {

    private final int id;
    private final String title;
    private final String blurb;
    private final String sampleCode;
    private final String sampleOutput;
    private final List<Puzzle> puzzles;
    private final boolean boss;

    public Level(int id, String title, String blurb, String sampleCode, String sampleOutput, List<Puzzle> puzzles) {
        this(id, title, blurb, sampleCode, sampleOutput, puzzles, false);
    }

    public Level(int id, String title, String blurb, String sampleCode, String sampleOutput,
                 List<Puzzle> puzzles, boolean boss) {
        this.id = id;
        this.title = title;
        this.blurb = blurb;
        this.sampleCode = sampleCode;
        this.sampleOutput = sampleOutput;
        this.puzzles = puzzles;
        this.boss = boss;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getBlurb() { return blurb; }
    public String getSampleCode() { return sampleCode; }
    public String getSampleOutput() { return sampleOutput; }
    public List<Puzzle> getPuzzles() { return puzzles; }
    /** True for every tenth campaign stage, which includes extra challenge puzzles. */
    public boolean isBoss() { return boss; }

    /** Odd levels are guarded by a Syntax Spider, even levels by a Null Pointer. */
    public Bug createBug() {
        if (boss) return new BossBug(id);
        return (id % 2 == 1) ? new SyntaxSpider() : new NullPointer();
    }
}
