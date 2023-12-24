import java.sql.*;

public class SqlConnector {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/vol_sys";
    private static final String USER = "root";
    private static final String PASSWORD = "lin_dcyy123";
    private static final SqlConnector sqlConn;

    static {
        try {
            sqlConn = new SqlConnector();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final Connection conn;
    private final Statement stmt;


    private SqlConnector() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection(URL, USER, PASSWORD);
        stmt = conn.createStatement();
    }

    public static SqlConnector getInstance() {
        return sqlConn;
    }

    /**
     * 关闭sql连接
     *
     * @throws SQLException
     */
    public void shutdownSql() throws SQLException {
        stmt.close();
        conn.close();
    }

    public Connection getConnection() {
        return conn;
    }

    /**
     * 执行sql语句
     *
     * @param text sql语句
     * @return 执行结果ResultSet
     */
    public ResultSet execSql(String text) throws SQLException {
        return stmt.executeQuery(text);
    }

}
