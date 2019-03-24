package src;

import java.lang.Class;
import java.sql.*;

import java.util.ArrayList;


/**
 * DbWrapper abstracts all necessary DB functionality that we need for Wysper
 * @TODO: Should store a map from userid to lastRead message and update when we get a pull
 * so we know how many messages to return
 *
 * @TODO: Need to write a function to return the n most recent messages from the messages table
 *
 * @TODO
 */
public class DbWrapper
{
    /* the default framework is embedded */
    private String framework = "embedded";
    private String protocol = "jdbc:derby:";

    private ArrayList<Statement> statements;
    public Connection conn;

    /**
     * The main function demonstrates some basic functionality of our dbwrapper class
     * You should call the constructor in the client function to connect to the
     * embedded database.
     * @param args
     */
    public static void main(String[] args) {

        DbWrapper db = new DbWrapper(true);

        // Insert sample users
        db.insertUser("Corey");
        db.insertUser("Alex");

        // Create sample message objects
        Message msg = new Message("Corey", "Hello");
        Message msg2 = new Message("Alex", "Hello");
        Message msg3 = new Message("Corey", "SDD is fun!");
        Message msg4 = new Message("Corey", "Nvm derby sucks");
        Message msg5 = new Message("Alex", "I <3 derby!");

        // Store our message objects in the database
        db.insertMessage(msg);
        db.insertMessage(msg2);
        db.insertMessage(msg3);
        db.insertMessage(msg4);
        db.insertMessage(msg5);

        System.out.println("\nWe have: " + db.countMessages() + " messages in the db!");

        /*
        *  We will call getMessages twice
        *  On the first call, we want to return all messages since Corey's
        *  last_read should be 0 at this point
        */
        System.out.println("\nOn first call: ");
        ArrayList<Message> ret = db.getMessages("Corey");
        if (ret == null) {
            System.out.println("No messages found");
        }
        else {
            for (Message tmp : ret) {
                System.out.println(tmp);
            }
        }

        /*
         * On second call, we don't want to return any messages since
         * at this point, Corey's last read should be the most recent
         * message
         */
        System.out.println("\nOn second call: ");
        ArrayList<Message> ret2 = db.getMessages("Corey");
        if (ret2 == null) {
            System.out.println("No messages found\n");
        }
        else {
            for (Message tmp : ret2) {
                System.out.println(tmp);
            }
        }
        db.displayAll();

        // Finally cleanup the database
        db.cleanup(true);

        System.out.println("Sample app finished!");

    }

    /**
     * Removes all data associated with the db and shuts down the
     * derby driver
     * @param dropTables specifies whether or not we should remove the
     *                   tables
     */
    public void cleanup(boolean dropTables) {
        try {
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

        //Connection
        try {
            if (this.conn != null) {
                this.conn.close();
                this.conn = null;
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }

        //Statements
        // Statements and PreparedStatements
        int i = 0;
        while (!this.statements.isEmpty()) {
            // PreparedStatement extend Statement
            Statement st = (Statement) this.statements.remove(i);
            try {
                if (st != null) {
                    st.close();
                    st = null;
                }
            } catch (SQLException sqle) {
                printSQLException(sqle);
            }
        }
    }

    /**
     *  Creates a connection object
     * @param createTables specifies whether or not we should create new
     *                     tables for users and messages
     *
     */
    public DbWrapper(boolean createTables) {
        String dbname = "wysperdb";
        try {
            String driver = "org.apache.derby.jdbc.EmbeddedDriver";
            Class.forName(driver).newInstance();
        }
        catch (Exception e) {
            System.out.println(e);
        }
        try {
            this.conn = DriverManager.getConnection(protocol + dbname
                    + ";create=True");

            System.out.println("Created database " + dbname + " and connected");

            // We want to control transactions manually. Autocommit is on by
            // default in JDBC.
            conn.setAutoCommit(false);

            this.statements = new ArrayList<Statement>();

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
     * Initializes the users table according to our Sprint2/DB Schema doc
     */
    private void createUsersTable() {
        try {
            Statement s;
            s = this.conn.createStatement();
            statements.add(s);

            // TODO: Should check here if the tables are already created
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
            printSQLException(sqle);
        }

    }

    /**
     * Counts the number of message objects in the database
     * primarily used for debugging purposes
     * @return the number of messages in the messages table
     */
    public int countMessages() {
        PreparedStatement countMsgs;
        ResultSet rs;
        int count = 0;
        try {
            countMsgs = this.conn.prepareStatement("Select count(*) from messages");
            statements.add(countMsgs);

            rs = countMsgs.executeQuery();
            rs.next();
            count = rs.getInt(1);

            rs.close();
            rs = null;
        }
        catch (SQLException sqle){
            printSQLException(sqle);
        }
        return count;
    }


    /**
     * Initializes the messages table according to our Sprint2/DB Schema document
     *   **Adds a column for username to aid in creating message objects**
     */
    private void createMessagesTable() {
        try {
            Statement s;
            s = this.conn.createStatement();
            statements.add(s);

            // TODO: Should check here if the tables are already created
            // TODO: how big should message body be?
            s.execute("create table messages(" +
                    "mid int NOT NULL GENERATED ALWAYS AS IDENTITY, " +
                    "userid int, " +
                    "username varchar(30), " +
                    "time timestamp, " +
                    "text varchar(255), " +
                    "PRIMARY KEY(mid)" +
                    ")");
            System.out.println("Created table messages");
            this.conn.commit();
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    /**
     * Returns the last_read attribute of a given user in our db
     * @param username the user to check
     * @return the highest messageid that this user has "read"
     */
    public int getLastRead(String username) {
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
     * This function adds a new user to the database
     * @param username the user to add to the db
     */
    public void insertUser(String username) {
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
            printSQLException(sqle);
        }
    }

    /**
     * This function adds a message to our database
     * TODO: Add message to parameters and extract necessary info
     * In wysper-server, we have a Message object that helps pass messages around,
     * but I'm not sure how you want to implement that Trevor so I'll leave it like this
     */
    public void insertMessage(Message msg) {
        String username = msg.getUsername();

        Timestamp time = msg.getTimestamp();
        String text = msg.getBody();

        PreparedStatement psInsert, countMessages;
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
     * Saves messages to the database using the insertMessage function
     * @param msgs a list of messages to save
     */
    public void insertMessages(ArrayList<Message> msgs) {
        for (Message msg : msgs) {
            insertMessage(msg);
        }
    }

    /**
     * Updates the last read for the user to the latest message in the messages table
     * @param user_name, the user to update
     */
    public void updateUsersLastRead(String user_name) {
        PreparedStatement updateusers, checkUser;
        ResultSet rs;
        try {
            updateusers = conn.prepareStatement(
                    "UPDATE users SET last_read = (SELECT max(mid) FROM messages) WHERE username = ?");

            statements.add(updateusers);
            updateusers.setString(1, user_name);    // Set username
            int updated = updateusers.executeUpdate();
            System.out.printf("Updating last_read for (%d) user(s) %s\n",updated, user_name);
            this.conn.commit();
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    public void displayMesssage(String user_name){
        Message msg;
        ResultSet rs;
        PreparedStatement getmessage;

        try {
            getmessage = conn.prepareStatement("SELECT userid, text, time  from messages WHERE mid >=(SELECT last_read from users WHERE username = ?) AS lastread");
            //Storing message in result set
            getmessage.setString(1,user_name);
            rs = getmessage.executeQuery();
            if (!rs.next()) {
                reportFailure("No rows in ResultSet");
            }
            else{
                while(rs.next()){
                    //fetching messages(Add code here to enter values to message class object)
                    System.out.println(rs.getInt(1) +" "+rs.getString(2)+" "+ rs.getString(3) );
                }
            }
            System.out.println("Sending messages to server");
            if (rs != null) {
                rs.close();
                rs = null;
            }
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }

        updateUsersLastRead(user_name);

    }
    public void displayAll(){
        ResultSet rs;
        PreparedStatement getmessage;
        System.out.println("userid, message id, text, time");
        try {
            getmessage = conn.prepareStatement("SELECT userid, mid, text, time  from messages");
            //Storing message in result set
            rs = getmessage.executeQuery();
            if (!rs.next()) {
                reportFailure("No rows in ResultSet");
            }
            else{
                while(rs.next()){
                    //fetching messages(Add code here to enter values to message class object)
                    System.out.println(rs.getInt(1) +", "+rs.getInt(2)+" ,"+rs.getString(3)+", "+ rs.getString(4) );
                }
            }
            System.out.println("Sending messages to server");
            if (rs != null) {
                rs.close();
                rs = null;
            }
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    private void updateMessagesRow() {

    }

    /**
     * Returns a list of message objects that are unread by username
     * @param username the user to get messages for
     */
    public ArrayList<Message> getMessages(String username) {

        ArrayList<Message> ret = new ArrayList<>();
        ResultSet rs;
        PreparedStatement getMessage;
        int last_read = 0;

        try {
            // First this function needs to query the users database and get
            // the last_read messageid of the given user
            last_read =  this.getLastRead("Corey");
            System.out.println("Last read for " + username + " is " + last_read);

            // Next we query the messages table and get the unread messages for this user
            getMessage = conn.prepareStatement("SELECT username, text, time, mid  from messages WHERE mid > ?");
            getMessage.setInt(1,last_read);

            rs = getMessage.executeQuery();

            if (!rs.next()) {
                return null;
            }
            else {
                // As we receive a message, we put it in a new message object and add it to the ret array
                int i = 0;
                Message msg = new Message(rs.getString(1), rs.getString(2), rs.getTimestamp(3));
                ret.add(msg);
                while (rs.next()) {
                    msg = new Message(rs.getString(1), rs.getString(2), rs.getTimestamp(3));
                    ret.add(msg);
                }
            }

            if (rs.next())
                rs.close();
                rs = null;

            // Finally we should update the user to reflect that they have read up to the most recent message
            updateUsersLastRead(username);
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
        return ret;
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
     * Reports a data verification failure to System.err with the given message.
     *
     * @param message A message describing what failed.
     */
    private void reportFailure(String message) {
        System.err.println("\nData verification failed:");
        System.err.println('\t' + message);
    }

    /**
     * Prints details of an SQLException chain to <code>System.err</code>.
     * Details included are SQL State, Error code, Exception message.
     *
     * @param e the SQLException from which to print details.
     */
    public static void printSQLException(SQLException e)
    {
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
