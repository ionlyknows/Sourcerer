package sourcerer.model;

/** A scaled boss variant that keeps the existing spider/null sprite family. */
public final class BossBug extends Bug {
    public BossBug(int stageId) {
        super("Stage " + stageId + " Boss", 65 + stageId / 10 * 8,
                10 + stageId / 20, stageId % 20 == 0 ? "null" : "spider");
    }
}
