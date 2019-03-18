package src;

import java.sql.*;

import java.util.ArrayList;
import java.util.Properties;


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
    private Connection conn;

    /**
     * The main function demonstrates some basic functionality of our dbwrapper class
     * You should call the constructor in the client function to connect to the
     * embedded database.
     * @param args
     */
    public static void main(String[] args)
    {
        //new DbWrapper().go();
        DbWrapper db = new DbWrapper();
        db.createUsersTable();
        db.createMessagesTable();
        db.insertUser("Corey");
        db.insertMessage();
        db.dropMessagesTable();
        db.dropUsersTable();
        System.out.println("SimpleApp finished");
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

    /**
     *  Creates a connection object
     */
    public DbWrapper() {
        String dbname = "wysperdb";
        try {
            this.conn = DriverManager.getConnection(protocol + dbname
                    + ";create=true");

            System.out.println("Created database " + dbname + " and connected");

            // We want to control transactions manually. Autocommit is on by
            // default in JDBC.
            conn.setAutoCommit(false);

            this.statements = new ArrayList<Statement>();
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
                    "userid int NOT NULL GENERATED ALWAYS AS IDENTITY, " +
                    "username varchar(30)," +
                    "last_read int," +
                    "PRIMARY KEY(userid)" +
                    ")");
            System.out.println("Created table users");
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }

    }

    /**
     * Initializes the messages table according to our Sprint2/DB Schema document
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
                    "time timestamp, " +
                    "text varchar(255), " +
                    "PRIMARY KEY(mid)" +
                    ")");
            System.out.println("Created table messages");
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    /**
     * This function adds a new user to the database
     * @param username the user to add to the db
     */
    private void insertUser(String username) {
        PreparedStatement psInsert;
        try {
            psInsert = conn.prepareStatement(
                    "insert into users (username, last_read) values (?, ?)");
            statements.add(psInsert);

            psInsert.setString(1, username);     // Set username to corey
            psInsert.setInt(2, 0);              // Set lastRead to 0
            psInsert.executeUpdate();
            System.out.println("Inserted " + username + " into users");
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
    private void insertMessage() {
        int userid = 20;
        Timestamp time = new Timestamp(System.currentTimeMillis());
        String text = "Whats up";

        PreparedStatement psInsert;
        try {
            psInsert = conn.prepareStatement(
                    "insert into messages (userid, time, text) values (?, ?, ?)");
            statements.add(psInsert);

            psInsert.setInt(1, userid);     // Set userid
            psInsert.setTimestamp(2, time); // Set timestamp
            psInsert.setString(3, text);    // Set message body
            psInsert.executeUpdate();
            System.out.println("Inserted \"" + text + "\" into messages");
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    private void updateUsersRow(String user_name) {
        PreparedStatement updateusers;
        try {
            updateusers = conn.prepareStatement(
                    "UPDATE users SET last_read = (SELECT mid FROM messages ORDER BY mid DESC LIMIT 1) WHERE username = ?");

            statements.add(updateusers);
            updateusers.setString(1, user_name);    // Set username
            updateusers.executeUpdate();
            System.out.println("Updated the user last read");
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    private Message displayMesssage(String user_name){
        Message msg;
        ResultSet rs;
        PreparedStatement getmessage;

        try {
            getmessage = conn.prepareStatement("SELECT userid, text, time  from messages WHERE mid >=(SELECT last_read from users WHERE username = ?) AS lastread");
            //Storing message in result set
            getmessage.setString(1,user_name);
            rs = getmessage.executeQuery()
            if (!rs.next())
            {
                reportFailure("No rows in ResultSet");
            }
            else{
                while(rs.next()){
                    //fetching messages(Add code here to enter values to message class object)
                    System.out.println(rs.getInt(1) +" "+rs.getString(2)+" "+ rs.getString(3) );
                    rs.next();
                }
            }
            System.out.println("Sending messages to server");
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }


    }

    private void updateMessagesRow() {

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
        }
        catch (SQLException sqle) {
            printSQLException(sqle);
        }
    }

    /**
     * This function was provided with the derby installation as a sample app
     * I extracted the functionality into separate functions, but I left this
     * in case we need to refer back to it in the future.
     * TODO: remove it?
     */


    /**
     * <p>
     * Starts the actual demo activities. This includes creating a database by
     * making a connection to Derby (automatically loading the driver),
     * creating a table in the database, and inserting, updating and retrieving
     * some data. Some of the retrieved data is then verified (compared) against
     * the expected results. Finally, the table is deleted and, if the embedded
     * framework is used, the database is shut down.</p>
     * <p>
     * Generally, when using a client/server framework, other clients may be
     * (or want to be) connected to the database, so you should be careful about
     * doing shutdown unless you know that no one else needs to access the
     * database until it is rebooted. That is why this demo will not shut down
     * the database unless it is running Derby embedded.</p>
     *
     * @param args - Optional argument specifying which framework or JDBC driver
     *        to use to connect to Derby. Default is the embedded framework,
     *        see the <code>main()</code> method for details.
     * @see #main(String[])
     */
    void go(String[] args)
    {
        System.out.println("SimpleApp starting in " + framework + " mode");

        /* We will be using Statement and PreparedStatement objects for
         * executing SQL. These objects, as well as Connections and ResultSets,
         * are resources that should be released explicitly after use, hence
         * the try-catch-finally pattern used below.
         * We are storing the Statement and Prepared statement object references
         * in an array list for convenience.
         */
        Connection conn = null;
        statements = new ArrayList<Statement>(); // list of Statements, PreparedStatements
        PreparedStatement psInsert;
        PreparedStatement psUpdate;
        Statement s;
        ResultSet rs = null;
        try
        {
            Properties props = new Properties(); // connection properties
            // providing a user name and password is optional in the embedded
            // and derbyclient frameworks
            props.put("user", "user1");
            props.put("password", "user1");

            /* By default, the schema APP will be used when no username is
             * provided.
             * Otherwise, the schema name is the same as the user name (in this
             * case "user1" or USER1.)
             *
             * Note that user authentication is off by default, meaning that any
             * user can connect to your database using any password. To enable
             * authentication, see the Derby Developer's Guide.
             */

            String dbName = "derbyDB"; // the name of the database

            /*
             * This connection specifies create=true in the connection URL to
             * cause the database to be created when connecting for the first
             * time. To remove the database, remove the directory derbyDB (the
             * same as the database name) and its contents.
             *
             * The directory derbyDB will be created under the directory that
             * the system property derby.system.home points to, or the current
             * directory (user.dir) if derby.system.home is not set.
             */
            conn = DriverManager.getConnection(protocol + dbName
                    + ";create=true", props);

            System.out.println("Connected to and created database " + dbName);

            // We want to control transactions manually. Autocommit is on by
            // default in JDBC.
            conn.setAutoCommit(false);

            /* Creating a statement object that we can use for running various
             * SQL statements commands against the database.*/
            s = conn.createStatement();
            statements.add(s);

            // Here we create a table
            // TODO: Should check here if the tables are already created
            s.execute("create table users(userid int NOT NULL primary key, username varchar(30), last_read int)");
            System.out.println("Created table users");

            // and add a few rows...

            /* It is recommended to use PreparedStatements when you are
             * repeating execution of an SQL statement. PreparedStatements also
             * allows you to parameterize variables. By using PreparedStatements
             * you may increase performance (because the Derby engine does not
             * have to recompile the SQL statement each time it is executed) and
             * improve security (because of Java type checking).
             */
            // parameter 1 is num (int), parameter 2 is addr (varchar)
            psInsert = conn.prepareStatement(
                    "insert into users values (?, ?)");
            statements.add(psInsert);

            psInsert.setString(2, "Corey");     // Set username to corey
            psInsert.setInt(3, 0);              // Set lastRead to 0
            psInsert.executeUpdate();
            System.out.println("Inserted Corey into Users");

            psInsert.setString(2, "Frank");
            psInsert.setInt(3, 0);
            psInsert.executeUpdate();
            System.out.println("Inserted Frank into Users");

            // Let's update some rows as well...

            // parameter 1 and 3 are num (int), parameter 2 is addr (varchar)
            psUpdate = conn.prepareStatement(
                    "update location set username=?, last_read=? where username=?");
            statements.add(psUpdate);


            psUpdate.setInt(2, 300);
            psUpdate.setString(1, "Corey");
            psUpdate.setString(3, "Corey");
            psUpdate.executeUpdate();
            System.out.println("Updated Corey's last_read to 300");


            /*
               We select the rows and verify the results.
             */
            rs = s.executeQuery(
                    "SELECT * FROM users");

            /*  Normally, it is best to use a pattern of
             *  while(rs.next()) {
             *    // do something with the result set
             *  }
             * to process all returned rows, but we are only expecting two rows
             * this time, and want the verification code to be easy to
             * comprehend, so we use a different pattern.
             */

            int number; // street number retrieved from the database
            boolean failure = false;
            if (!rs.next())
            {
                failure = true;
                reportFailure("No rows in ResultSet");
            }

            if ((number = rs.getInt(1)) != 300)
            {
                failure = true;
                reportFailure(
                        "Wrong row returned, expected num=300, got " + number);
            }

            if (!rs.next())
            {
                failure = true;
                reportFailure("Too few rows");
            }

            if ((number = rs.getInt(1)) != 1910)
            {
                failure = true;
                reportFailure(
                        "Wrong row returned, expected num=1910, got " + number);
            }

            if (rs.next())
            {
                failure = true;
                reportFailure("Too many rows");
            }

            if (!failure) {
                System.out.println("Verified the rows");
            }

            // delete the table
            s.execute("drop table location");
            System.out.println("Dropped table location");

            /*
               We commit the transaction. Any changes will be persisted to
               the database now.
             */
            conn.commit();
            System.out.println("Committed the transaction");

            /*
             * In embedded mode, an application should shut down the database.
             * If the application fails to shut down the database,
             * Derby will not perform a checkpoint when the JVM shuts down.
             * This means that it will take longer to boot (connect to) the
             * database the next time, because Derby needs to perform a recovery
             * operation.
             *
             * It is also possible to shut down the Derby system/engine, which
             * automatically shuts down all booted databases.
             *
             * Explicitly shutting down the database or the Derby engine with
             * the connection URL is preferred. This style of shutdown will
             * always throw an SQLException.
             *
             * Not shutting down when in a client environment, see method
             * Javadoc.
             */

            if (framework.equals("embedded"))
            {
                try
                {
                    // the shutdown=true attribute shuts down Derby
                    DriverManager.getConnection("jdbc:derby:;shutdown=true");

                    // To shut down a specific database only, but keep the
                    // engine running (for example for connecting to other
                    // databases), specify a database in the connection URL:
                    //DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true");
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
            }
        }
        catch (SQLException sqle)
        {
            printSQLException(sqle);
        } finally {
            // release all open resources to avoid unnecessary memory usage

            // ResultSet
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (SQLException sqle) {
                printSQLException(sqle);
            }

            // Statements and PreparedStatements
            int i = 0;
            while (!statements.isEmpty()) {
                // PreparedStatement extend Statement
                Statement st = (Statement)statements.remove(i);
                try {
                    if (st != null) {
                        st.close();
                        st = null;
                    }
                } catch (SQLException sqle) {
                    printSQLException(sqle);
                }
            }

            //Connection
            try {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            } catch (SQLException sqle) {
                printSQLException(sqle);
            }
        }
    }





}
