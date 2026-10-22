package sourcerer.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Creates JDBC connections using environment configuration or local development defaults. */
public final class DatabaseConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/sourcerer_crusade";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private DatabaseConnection() {
    }

    /**
     * Opens a connection. The caller owns the returned connection and should close it,
     * preferably with try-with-resources.
     *
     * <p>Configure {@code SOURCERER_DB_URL}, {@code SOURCERER_DB_USER}, and
     * {@code SOURCERER_DB_PASSWORD} for a local server. The defaults target a typical
     * XAMPP installation; verify its server type and port first.</p>
     */
    public static Connection getConnection() throws SQLException {
        String url = setting("SOURCERER_DB_URL", DEFAULT_URL);
        String user = setting("SOURCERER_DB_USER", DEFAULT_USER);
        String password = setting("SOURCERER_DB_PASSWORD", DEFAULT_PASSWORD);
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException exception) {
            String state = exception.getSQLState();
            String stateText = state == null ? "unknown" : state;
            throw new SQLException("Could not connect to the Sourcerer database (SQLState "
                    + stateText + ", error " + exception.getErrorCode()
                    + "). Check the database service, JDBC driver, URL, and credentials.",
                    state, exception.getErrorCode());
        }
    }

    /** Opens and closes a connection to verify that the configured database is reachable. */
    public static boolean isConnectionValid() throws SQLException {
        try (Connection connection = getConnection()) {
            return connection.isValid(5);
        }
    }

    private static String setting(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
