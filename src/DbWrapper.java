import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * DbWrapper abstracts all necessary database functionality that we need for Wysper
 */
class DbWrapper
{
    private String protocol = "jdbc:derby:";
    private String dbname;

    private ArrayList<Statement> statements;
    private Connection conn;

    /**
     * Creates a connection to a database
     * @param createTables specifies whether or not we should create new tables for users and messages
     */
    DbWrapper(String name, boolean createTables) {
        dbname = name;
        // try to instantiate database
        try {
            String driver = "org.apache.derby.jdbc.EmbeddedDriver";
            Class.forName(driver).newInstance();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        // try to connect to this database
        try {
            this.conn = DriverManager.getConnection(protocol + dbname + ";create=True");

            System.out.println("Created database " + dbname + " and connected");

            // We want to control transactions manually. Autocommit is on by default in JDBC.
            conn.setAutoCommit(false);
            this.statements = new ArrayList<>();

            // create user and message tables
            if (createTables) {
                this.createUsersTable();
                this.createMessagesTable();
            }
            this.conn.commit();
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    /**
     * Initializes the users table
     */
    private void createUsersTable() {
        try {
            Statement s;
            s = this.conn.createStatement();
            statements.add(s);

            s.execute("create table users(" +
                    "userid int NOT NULL GENERATED ALWAYS AS IDENTITY (Start with 1), " +
                    "username varchar(30) UNIQUE," +
                    "last_read int," +
                    "PRIMARY KEY(userid)" +
                    ")");
            System.out.println("Created table users");
            this.conn.commit();
        }
        catch (SQLException sqle) {
            if (sqle.getErrorCode() == 20000) {
                System.out.println("users table found");
                return;
            }
            printSQLException(sqle);
        }

    }

    /**
     * Initializes the messages table
     */
    private void createMessagesTable() {
        try {
            Statement s;
            s = this.conn.createStatement();
            statements.add(s);

            s.execute("create table messages(" +
                    "mid int NOT NULL GENERATED ALWAYS AS IDENTITY, " +
                    "userid int, " +
                    "username varchar(30), " +
                    "time timestamp, " +
                    "text varchar(8000), " +
                    "PRIMARY KEY(mid)" +
                    ")");
            System.out.println("Created table messages");
            this.conn.commit();
        }
        catch (SQLException sqle) {
            if (sqle.getErrorCode() == 20000) {
                System.out.println("messages table found");
                return;
            }
            printSQLException(sqle);
        }
    }

    /**
     * Finds the message ID of the most recent message a user has seen
     * @param username the user to search for
     * @return the most recent messageid that this user has seen
     */
    int getLastRead(String username) {
        PreparedStatement checkUser;
        ResultSet rs;
        int ret = 0;
        try {
            checkUser = conn.prepareStatement("Select last_read from users where username= ? ");
            statements.add(checkUser);
            checkUser.setString(1, username);
            int last_read = 0;
            rs = checkUser.executeQuery();
            while (rs.next()) {
                last_read = rs.getInt(1);
            }
            rs.close();
            ret = last_read;
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
        return ret;
    }

    /**
     * Updates the last read for the user to the latest message in the messages table
     * @param username, the user to update
     */
    void updateUsersLastRead(String username) {
        PreparedStatement updateusers;
        try {
            updateusers = conn.prepareStatement(
                    "UPDATE users SET last_read = (SELECT max(mid) FROM messages) WHERE username = ?");

            statements.add(updateusers);
            updateusers.setString(1, username);    // Set username
            updateusers.executeUpdate();
            this.conn.commit();
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    /**
     * This function adds a new user to the database
     * @param username the user to add to the database
     */
    void insertUser(String username) {
        PreparedStatement psInsert;

        try {
            psInsert = conn.prepareStatement(
                    "insert into users (username, last_read) values (?, ?)");
            this.statements.add(psInsert);

            psInsert.setString(1, username);       // Set username to corey
            psInsert.setInt(2, 0);              // Set lastRead to 0
            psInsert.executeUpdate();
            System.out.println("Inserted " + username + " into users");
            this.conn.commit();
        }
        catch (SQLException sqle) {
            if (sqle.getErrorCode() == 20000) {
                return;
            }
            printSQLException(sqle);
        }
    }

    /**
     * This function adds a new message to the database
     * @param msg the message to add to the database
     */
    void insertMessage(Message msg) {
        // get message components
        String username = msg.getUsername();
        Timestamp time = msg.getTimestamp();
        String text = msg.getBody();

        PreparedStatement psInsert;
        try {
            psInsert = conn.prepareStatement(
                    "insert into messages (username, time, text) values (?, ?, ?)");
            this.statements.add(psInsert);

            psInsert.setString(1, username);     // Set username
            psInsert.setTimestamp(2, time); // Set timestamp
            psInsert.setString(3, text);    // Set message body

            psInsert.executeUpdate();
            this.conn.commit();
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    /**
     * Get all messages that are unread by the specified user
     * @param username the user to get messages for
     * @return array of messages this user has not seen
     */
    ArrayList<Message> getMessages(String username) {

        ArrayList<Message> ret = new ArrayList<>();
        ResultSet rs;
        PreparedStatement getMessage;
        int last_read;

        try {
            // First this function needs to query the users database and get
            // the last_read messageid of the given user
            last_read = this.getLastRead(username);

            // Next we query the messages table and get the unread messages for this user
            getMessage = conn.prepareStatement("SELECT username, text, time, mid  from messages WHERE mid > ?");
            getMessage.setInt(1,last_read);

            rs = getMessage.executeQuery();

            if (!rs.next()) {
                return ret;
            }
            else {
                // As we receive a message, we put it in a new message object and add it to the ret array
                Message msg = new Message(rs.getString(1), rs.getString(2), rs.getTimestamp(3));
                ret.add(msg);
                while (rs.next()) {
                    msg = new Message(rs.getString(1), rs.getString(2), rs.getTimestamp(3));
                    ret.add(msg);
                }
            }

            if (rs.next())
                rs.close();

            // Finally we should update the user to reflect that they have read up to the most recent message
            updateUsersLastRead(username);
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
        return ret;
    }

    /**
     * Get a specific number of recent messages
     * @param n the number of messages to get
     * @return array of messages of size n
     */
    ArrayList<Message> getMessages(int n) {
        ArrayList<Message> ret = new ArrayList<>();
        ResultSet rs;
        PreparedStatement getmessage;
        try {
            getmessage = conn.prepareStatement("SELECT username, mid, text, time  from messages ORDER BY mid DESC");
            rs = getmessage.executeQuery();
            if (!rs.next()) {
                return ret;
            }
            else{
                do {
                    Message msg = new Message(rs.getString(1), rs.getString(3), rs.getTimestamp(4));
                    ret.add(msg);
                    n--;
                } while (rs.next() && n > 0);
            }
            rs.close();
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
        Collections.reverse(ret);
        return ret;
    }

    /**
     * Removes all data associated with the db and shuts down the derby driver
     * @param dropTables specifies whether or not we should remove the tables
     */
    void cleanup(boolean dropTables) {

        // shut down database
        try {
            // delete the database if user specified
            if (dropTables) {
                this.dropUsersTable();
                this.dropMessagesTable();
            }

            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        }
        catch (SQLException se)
        {
            if (( (se.getErrorCode() == 50000)
                    && ("XJ015".equals(se.getSQLState()) ))) {
                // we got the expected exception
                System.out.println("Derby shut down normally");
                // Note that for single database shutdown, the expected
                // SQL state is "08006", and the error code is 45000.
            } else {
                // if the error code or SQLState is different, we have
                // an unexpected exception (shutdown failed)
                System.err.println("Derby did not shut down normally");
                printSQLException(se);
            }
        }

        // remove connection to database
        try {
            if (this.conn != null) {
                this.conn.close();
                this.conn = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }

        // clear outstanding statements
        int i = 0;
        while (!this.statements.isEmpty()) {
            Statement st = this.statements.remove(i);
            try {
                if (st != null) {
                    st.close();
                }
            } catch (SQLException sqle) {
                printSQLException(sqle);
            }
        }
    }

    /**
     * Drops the user table from our database
     */
    private void dropUsersTable() {
        try {
            Statement s = conn.createStatement();
            statements.add(s);
            // delete the table
            s.execute("drop table users");
            System.out.println("Dropped table users");
            this.conn.commit();
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    /**
     * Drops the messages table from our database
     */
    private void dropMessagesTable() {
        try {
            Statement s = conn.createStatement();
            statements.add(s);
            // delete the table
            s.execute("drop table messages");
            System.out.println("Dropped table messages");
            this.conn.commit();
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    /**
     * Print all messages in the database to the server console
     */
    void displayAllMessages() {
        ResultSet rs;
        PreparedStatement getmessage;
        System.out.println(String.format("%10s %10s %25s %25s", "userid", "messageid", "text", "time"));
        try {
            getmessage = conn.prepareStatement("SELECT username, mid, text, time  from messages");
            //Storing message in result set
            rs = getmessage.executeQuery();
            if (!rs.next()) {
                System.err.println("No rows in ResultSet");
            }
            else {
                do {
                    //fetching messages(Add code here to enter values to message class object)
                    System.out.println(String.format("%10s %10s %25s %25s",
                            rs.getString(1), rs.getInt(2), rs.getString(3), rs.getString(4)));
                } while(rs.next());
            }
            rs.close();
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    void displayAllUsers() {
        ResultSet rs;
        PreparedStatement getuser;
        System.out.println(String.format("%10s %10s", "userid", "last read"));
        try {
            getuser = conn.prepareStatement("SELECT username, last_read  from users");
            //Storing message in result set
            rs = getuser.executeQuery();
            if (!rs.next()) {
                System.err.println("No rows in ResultSet");
            }
            else {
                do {
                    //fetching messages(Add code here to enter values to message class object)
                    System.out.println(String.format("%10s %10s",
                            rs.getString(1), rs.getInt(2)));
                } while(rs.next());
            }
            rs.close();
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    /**
     * Prints details of an SQLException chain
     * @param e the SQLException from which to print details.
     */
    private static void printSQLException(SQLException e) {
        // Unwraps the entire exception chain to unveil the real cause of the
        // Exception.
        while (e != null)
        {
            System.err.println("\n----- SQLException -----");
            System.err.println("  SQL State:  " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  Message:    " + e.getMessage());
            // for stack traces, refer to derby.log or uncomment this:
            //e.printStackTrace(System.err);
            e = e.getNextException();
        }
    }
}
