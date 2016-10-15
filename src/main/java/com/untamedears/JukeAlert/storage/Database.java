package com.untamedears.JukeAlert.storage;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.avaje.ebeaninternal.server.lib.sql.DataSourceException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MySql database
 *
 * @author Dylan
 */
public class Database {

    private String host;
    private int port;
    private String db;
    private String user;
    private String password;
    private String prefix;
    private Logger logger;
    private Connection connection;

    public Database(String host, int port, String db, String user, String password, String prefix, Logger logger) {
        this.host = host;
        this.port = port;
        this.db = db;
        this.user = user;
        this.password = password;
        this.prefix = prefix;
        this.logger = logger;
    }

    public String getDb() {
        return db;
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getUser() {
        return user;
    }

    /**
     * Connects to the database.
     *
     * @since 0.1
     */
    public boolean connect() {
        String jdbc = "jdbc:mysql://" + host + ":" + port + "/" + db + "?user=" + user + "&password=" + password + "&autoReconnect=true";
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            throw new DataSourceException("Failed to initialize JDBC driver.");
        }
        try {
            connection = DriverManager.getConnection(jdbc);
            this.logger.log(Level.INFO, "Connected to database!");
            return true;
        } catch (SQLException ex) { //Error handling below:
            this.logger.log(Level.SEVERE, "Could not connnect to the database!", ex);
            return false;
        }
    }

    /**
     * Closes the database connection.
     *
     * @since 0.1
     */
    public void close() {
        try {
            connection.close();
        } catch (SQLException ex) {
            this.logger.log(Level.SEVERE, "An error occured while closing the connection.", ex);
        }
    }

    /**
     * Are we connected to the database?
     *
     * @return Connected
     * @throws SQLException
     */
    public boolean isConnected() {
        try {
            return connection.isValid(5);
        } catch (SQLException ex) {
            this.logger.log(Level.SEVERE, "isConnected error!", ex);
        }
        return false;
    }

    /**
     * Prepare the SQL statements
     *
     * @return PreparedStatement
     * @throws SQLException
     */
    public PreparedStatement prepareStatement(String sqlStatement) {
        try {
            return connection.prepareStatement(sqlStatement);
        } catch (SQLException ex) {
            this.logger.log(Level.SEVERE, "Failed to prepare statement! " + sqlStatement, ex);
        }
        return null;
    }

    /**
     * Executes an SQL query. (No output)
     *
     * @param sql The SQL query as a string.
     */
    public void execute(String sql) {
        try {
            connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException ex) {
            try {
                connect();
                connection.prepareStatement(sql).executeUpdate();
            } catch (SQLException ex2) {
                this.logger.log(Level.SEVERE, "Could not execute SQL statement!", ex2);
            }
        }
    }

    /**
     * Executes an SQL query. (No output, pass Exception along)
     *
     * @param sql The SQL query as a string.
     */
    public void executeLoud(String sql) throws java.sql.SQLException {
        try {
            connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException ex) { // one quiet retry, scream on failure.
            connect();
            connection.prepareStatement(sql).executeUpdate();
        }
    }

    /**
     * Executes a SQL query. Notes an exception w/o stack and does not retry
     *
     * @param sql The SQL query as a strign.
     */
    public void silentExecute(String sql) {
        try {
            connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException ex) { // one quiet retry, scream on failure.
            this.logger.log(Level.WARNING, "Quiet execution of SQL failed, no retry attempted.");
        }
    }

    /**
     * Gets a result set returned from an SQL query.
     *
     * @param sql
     * @return ResultSet
     */
    public ResultSet getResultSet(String sql) {
        try {
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException ex) {
            try {
                connect();
                return connection.createStatement().executeQuery(sql);
            } catch (SQLException ex2) {
                this.logger.log(Level.SEVERE, "Could not get result set from SQL statement!", ex2);
            }
        }
        return null;
    }

    /**
     * Gets a string from the database.
     *
     * @param sql
     * @return String
     *
     * @since 0.1
     */
    public String getString(String sql) {
        try {
            ResultSet result = getResultSet(sql);
            if (result == null) {
                this.logger.log(Level.SEVERE, "Result Set Retrieval failed, null found instead");
                return null;
            }
            result.next();
            return result.getString(1);
        } catch (SQLException ex) {
            this.logger.log(Level.SEVERE, "Could not execute SQL statement!", ex);
        }
        return null;
    }

    /**
     * Gets an integer from the database.
     *
     * @param sql
     * @return int
     */
    public int getInteger(String sql) {
        try {
            ResultSet result = getResultSet(sql);
            if (result == null) {
                this.logger.log(Level.SEVERE, "Result Set Retrieval failed, null found instead");
                return 0;
            }
            result.next();
            return result.getInt(1);
        } catch (SQLException ex) {
            this.logger.log(Level.SEVERE, "Could not execute SQL statement!", ex);
        }
        return 0;
    }

    /**
     * Gets a double from the database.
     *
     * @param sql
     * @return double
     */
    public double getDouble(String sql) {
        try {
            ResultSet result = getResultSet(sql);
            if (result == null) {
                this.logger.log(Level.SEVERE, "Result Set Retrieval failed, null found instead");
                return 0;
            }
            result.next();
            return result.getDouble(1);
        } catch (SQLException ex) {
            this.logger.log(Level.SEVERE, "Could not execute SQL statement!", ex);
        }
        return 0;
    }

    /**
     * Gets a boolean from the database.
     *
     * @param sql
     * @return boolean
     */
    public boolean getBoolean(String sql) {
        try {
            ResultSet result = getResultSet(sql);
            if (result == null) {
                this.logger.log(Level.SEVERE, "Result Set Retrieval failed, null found instead");
                return false;
            }
            result.next();
            boolean returnValue = result.getBoolean(1);
            return returnValue;
        } catch (SQLException ex) {
            this.logger.log(Level.SEVERE, "Could not execute SQL statement!", ex);
        }
        return false;
    }

    /**
     * Gets all of the values in a specified column as a list.
     *
     * @param sql
     * @return List of column values
     */
    public List<String> getColumn(String sql) {
        List<String> coldata = new ArrayList<String>();
        try {
            ResultSet result = getResultSet(sql);
            if (result == null) {
                this.logger.log(Level.SEVERE, "Result Set Retrieval failed, null found instead");
                return null;
            }
            while (result.next()) {
                coldata.add(result.getString(1));
            }
            return coldata;
        } catch (SQLException ex) {
            this.logger.log(Level.SEVERE, "Could not execute SQL statement!", ex);
        }
        return null;
    }
}
