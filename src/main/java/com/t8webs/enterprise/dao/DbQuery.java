package com.t8webs.enterprise.dao;

import com.t8webs.enterprise.T8WebsApplication;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Class containing all functionality for database communication
 */
public class DbQuery {

    private String tableName = "";
    private StringBuffer whereCondition = new StringBuffer();
    private HashMap<String, Object> columnValues = new HashMap<>();

    /**
     * Method for Subclasses to set the table name for their corresponding table
     *
     * @param tableName Name of the database table
     */
    public void setTableName(String tableName) {
        this.tableName = tableName != null ? tableName : "";
    }

    /**
     * This is a method for setting constraints on sql statements
     *
     * @param column column name
     * @param value String value required in the column
     */
    public void addWhere(String column, String value) {
        if (column == null || value == null) {
            return;
        }

        if (this.whereCondition.isEmpty()) {
            this.whereCondition.append(" WHERE ");
        } else {
            this.whereCondition.append(" AND ");
        }

        this.whereCondition.append(column);

        if (value.equalsIgnoreCase("NULL")) {
            this.whereCondition.append(" IS NULL");
        } else {
            this.whereCondition.append(" = '").append(value).append("'");
        }
    }

    /**
     * This is a method for setting constraints on sql statements
     *
     * @param column column name
     * @param value integer value required in the column
     */
    public void addWhere(String column, int value) {
        if (column == null) {
            return;
        }

        if (this.whereCondition.isEmpty()) {
            this.whereCondition.append(" WHERE ");
        } else {
            this.whereCondition.append(" AND ");
        }

        this.whereCondition.append(column).append(" = ").append(value);
    }

    /**
     * This method is used for running select statements against the database
     *
     * @return ArrayList containing key value pairs
     */
    public ArrayList<HashMap<String, Object>> select() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT * FROM ").append(tableName).append(whereCondition);

        return execute(sql.toString());
    }

    /**
     * This method is for creating column value pairs for a SQL statement
     *
     * @param column name of column to be updated
     * @param value value to be populated into the given column
     */
    public void setColumnValue(String column, Object value) {
        if (column != null && value != null) {
            columnValues.put(column, value);
        }
    }

    /**
     * This method is used for running update statements against the database
     *
     * @return boolean indicating whether update was successful
     */
    public boolean update() {
        if (whereCondition.isEmpty()) {
            return false;
        }

        StringBuffer sql = new StringBuffer();
        sql.append("UPDATE ").append(tableName).append(" SET ");

        int index = 0;
        for(Map.Entry entry: columnValues.entrySet()) {
            index++;
            String column = (String) entry.getKey();
            Object value = entry.getValue();

            sql.append(column).append(" = ");

            if (value instanceof String || value instanceof Timestamp) {
                sql.append("'").append(value.toString()).append("'");
            } else {
                sql.append(value);
            }

            if (index < columnValues.size()) {
                sql.append(", ");
            }
        }

        sql.append(whereCondition);

        try {
            return executeUpdate(sql.toString());
        } catch (IntegrityConstraintViolationException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This method is used for running insert statements against the database
     *
     * @return boolean indicating whether insert was successful
     */
    public boolean insert() throws IntegrityConstraintViolationException {
        StringBuffer sql = new StringBuffer();
        StringBuffer colStr = new StringBuffer();
        StringBuffer valStr = new StringBuffer();

        int index = 0;
        for(Map.Entry entry: columnValues.entrySet()) {
            index++;
            String column = (String) entry.getKey();
            Object value = entry.getValue();

            colStr.append(column);

            if (value instanceof String || value instanceof Timestamp) {
                valStr.append("'").append(value.toString()).append("'");
            } else {
                valStr.append(value);
            }

            if(index < columnValues.entrySet().size()) {
                colStr.append(",");
                valStr.append(",");
            }
        }
        sql.append("INSERT INTO ").append(tableName).append(" (").append(colStr).append(") VALUES (").append(valStr).append(")");

        return executeUpdate(sql.toString());
    }

    /**
     * This method is used for running delete statements against the database
     *
     * @return boolean indicating whether delete was successful
     */
    public boolean delete() {
        if (whereCondition.isEmpty()) {
            return false;
        }

        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM ").append(tableName).append(whereCondition);

        try {
            return executeUpdate(sql.toString());
        } catch (IntegrityConstraintViolationException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param query String to execute a select statement
     * @return ArrayList of SQL results represented as key value pairs
     */
    private ArrayList<HashMap<String, Object>> execute(String query) {
        try (Connection conn = getConnection();
             Statement statement = conn.createStatement())
        {
            statement.execute(query);
            return getResultList(statement.getResultSet());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList();
        }
    }

    /**
     * @param query String to execute an UPDATE, DELETE, or INSERT statement
     * @return boolean indicating a successful query
     * @throws IntegrityConstraintViolationException indicating a race condition
     */
    private boolean executeUpdate(String query) throws IntegrityConstraintViolationException {
        try (Connection conn = getConnection();
             Statement statement = conn.createStatement())
        {
            try {
                return statement.executeUpdate(query) > 0;
            } catch (SQLIntegrityConstraintViolationException e) {
                e.printStackTrace();
                throw new IntegrityConstraintViolationException();
            }
        } catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method for getting a connection to the database
     *
     * @return Connection to database
     */
    private Connection getConnection() throws ClassNotFoundException, SQLException, IOException {
        Class.forName("org.mariadb.jdbc.Driver");

        Properties properties = new Properties();
        properties.load(T8WebsApplication.class.getClassLoader().getResourceAsStream("application.properties"));

        return DriverManager.getConnection(properties.getProperty("databaseURL"), properties.getProperty("databaseUser"), properties.getProperty("databasePass"));
    }

    /**
     * Method for parsing SQL Result sets into an ArrayList
     *
     * @param resultSet returned from the execution of a SQL statement
     * @return ArrayList of key value pairs
     */
    private ArrayList<HashMap<String, Object>> getResultList(ResultSet resultSet) throws SQLException {
        ArrayList<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> valuesMap;

        ResultSetMetaData metaData = resultSet.getMetaData();

        while (resultSet.next()) {
            valuesMap = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                int columnType = metaData.getColumnType(i);
                Object value;
                if (columnType == 93) {
                    value = resultSet.getTimestamp(i);
                } else {
                    value = resultSet.getObject(i);
                }

                valuesMap.put(metaData.getColumnName(i), value);
            }
            results.add(valuesMap);
        }

        return results;
    }

    public class IntegrityConstraintViolationException extends Exception {
        public IntegrityConstraintViolationException() {
            super("Duplicate Primary Key.");
        }
    }
}
