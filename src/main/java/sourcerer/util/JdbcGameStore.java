package sourcerer.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import sourcerer.model.Potion;
import sourcerer.model.Progress;
import sourcerer.model.LevelBank;

/** JDBC persistence for the single local campaign profile used by the current game. */
public final class JdbcGameStore {
    private static final String PLAYER_NAME = "Sourcerer";

    private JdbcGameStore() { }

    /** Loads the profile into Progress, creating a new local profile when needed. */
    public static boolean load(Progress progress) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long playerId;
                boolean created;
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT IGNORE INTO players (player_name, xp, character_level, coins) VALUES (?, 0, 1, 0)")) {
                    insert.setString(1, PLAYER_NAME);
                    created = insert.executeUpdate() == 1;
                }
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT player_id, xp, character_level, coins, tutorial_done FROM players WHERE player_name = ? FOR UPDATE")) {
                    select.setString(1, PLAYER_NAME);
                    try (ResultSet rows = select.executeQuery()) {
                        if (!rows.next()) throw new SQLException("Could not create or load the local player profile");
                        playerId = rows.getLong("player_id");
                        if (created) {
                            // Validate/import any local save before creating the DB profile.
                            connection.rollback();
                            return false;
                        }
                        int level = rows.getInt("character_level");
                        int xp = rows.getInt("xp");
                        int xpToNext = 50 + 25 * (level - 1);
                        int coins = rows.getInt("coins");
                        boolean tutorialDone = rows.getBoolean("tutorial_done");

                        int[] inventory = loadInventory(connection, playerId, created);
                        int[] stars = loadProgress(connection, playerId, created);
                        int[] bosses = loadBosses(connection, playerId);
                        progress.restoreDatabaseState(level, xp, xpToNext, coins, tutorialDone,
                                inventory[0], inventory[1], stars, bosses);
                    }
                }
                connection.commit();
                return !created;
            } catch (SQLException | RuntimeException failure) {
                connection.rollback();
                throw failure;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private static int[] loadInventory(Connection connection, long playerId, boolean created) throws SQLException {
        int healing = created ? 3 : 0;
        int mana = created ? 2 : 0;
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT IGNORE INTO inventory (player_id, potion_code, quantity) VALUES (?, ?, ?)")) {
            insert.setLong(1, playerId);
            insert.setString(2, Potion.HEALING.name());
            insert.setInt(3, healing);
            insert.executeUpdate();
            insert.setLong(1, playerId);
            insert.setString(2, Potion.MANA.name());
            insert.setInt(3, mana);
            insert.executeUpdate();
        }
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT potion_code, quantity FROM inventory WHERE player_id = ?")) {
            query.setLong(1, playerId);
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    if (Potion.HEALING.name().equals(rows.getString(1))) healing = rows.getInt(2);
                    if (Potion.MANA.name().equals(rows.getString(1))) mana = rows.getInt(2);
                }
            }
        }
        return new int[]{healing, mana};
    }

    private static int[] loadProgress(Connection connection, long playerId, boolean created) throws SQLException {
        int[] stars = new int[LevelBank.count() + 2];
        if (created) {
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT IGNORE INTO player_progress (player_id, stage_id, unlocked, completed, stars) VALUES (?, 1, TRUE, FALSE, 0)")) {
                insert.setLong(1, playerId);
                insert.executeUpdate();
            }
        }
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT stage_id, stars FROM player_progress WHERE player_id = ?")) {
            query.setLong(1, playerId);
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    int id = rows.getInt(1);
                    if (id > 0 && id < stars.length) stars[id] = rows.getInt(2);
                }
            }
        }
        return stars;
    }

    private static int[] loadBosses(Connection connection, long playerId) throws SQLException {
        int[] result = new int[10];
        int count = 0;
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT stage_id FROM boss_defeats WHERE player_id = ?")) {
            query.setLong(1, playerId);
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    int id = rows.getInt(1);
                    if (id >= 10 && id <= 100 && id % 10 == 0) result[count++] = id;
                }
            }
        }
        int[] trimmed = new int[count];
        System.arraycopy(result, 0, trimmed, 0, count);
        return trimmed;
    }

    /** Persists player, inventory, stage progress and boss records in one transaction. */
    public static void save(Progress progress) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long playerId;
                try (PreparedStatement upsert = connection.prepareStatement(
                        "INSERT INTO players (player_name, xp, character_level, coins, bosses_defeated, tutorial_done) "
                        + "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE xp=VALUES(xp), "
                        + "character_level=VALUES(character_level), coins=VALUES(coins), "
                        + "bosses_defeated=VALUES(bosses_defeated), tutorial_done=VALUES(tutorial_done)")) {
                    upsert.setString(1, PLAYER_NAME);
                    upsert.setInt(2, progress.getSavedXp());
                    upsert.setInt(3, progress.getSavedLevel());
                    upsert.setInt(4, progress.getSavedCoins());
                    upsert.setInt(5, progress.getBossesDefeated());
                    upsert.setBoolean(6, progress.isTutorialDone());
                    upsert.executeUpdate();
                }
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT player_id FROM players WHERE player_name = ?")) {
                    query.setString(1, PLAYER_NAME);
                    try (ResultSet rows = query.executeQuery()) {
                        if (!rows.next()) throw new SQLException("Player record disappeared during save");
                        playerId = rows.getLong(1);
                    }
                }
                saveInventory(connection, playerId, Potion.HEALING, progress.getSavedHealing());
                saveInventory(connection, playerId, Potion.MANA, progress.getSavedMana());
                saveStageProgress(connection, playerId, progress);
                for (int boss : progress.getBossStagesSnapshot()) {
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT IGNORE INTO boss_defeats (player_id, stage_id, stage_type) VALUES (?, ?, 'BOSS')")) {
                        insert.setLong(1, playerId);
                        insert.setInt(2, boss);
                        insert.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException failure) {
                connection.rollback();
                throw failure;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private static void saveInventory(Connection connection, long playerId, Potion potion, int quantity)
            throws SQLException {
        try (PreparedStatement upsert = connection.prepareStatement(
                "INSERT INTO inventory (player_id, potion_code, quantity) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE quantity=VALUES(quantity)")) {
            upsert.setLong(1, playerId);
            upsert.setString(2, potion.name());
            upsert.setInt(3, quantity);
            upsert.executeUpdate();
        }
    }

    private static void saveStageProgress(Connection connection, long playerId, Progress progress)
            throws SQLException {
        try (PreparedStatement upsert = connection.prepareStatement(
                "INSERT INTO player_progress (player_id, stage_id, unlocked, completed, stars, completed_at) "
                + "VALUES (?, ?, ?, ?, ?, IF(?, CURRENT_TIMESTAMP, NULL)) ON DUPLICATE KEY UPDATE "
                + "unlocked=VALUES(unlocked), completed=VALUES(completed), stars=VALUES(stars), "
                + "completed_at=IF(VALUES(completed), COALESCE(completed_at, CURRENT_TIMESTAMP), NULL)")) {
            for (int id = 1; id <= 100; id++) {
                int stars = progress.getStars(id);
                boolean completed = stars > 0;
                upsert.setLong(1, playerId);
                upsert.setInt(2, id);
                upsert.setBoolean(3, id == 1 || progress.isCompleted(id - 1));
                upsert.setBoolean(4, completed);
                upsert.setInt(5, stars);
                upsert.setBoolean(6, completed);
                upsert.addBatch();
            }
            upsert.executeBatch();
        }
    }
}
