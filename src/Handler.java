package src;

import org.json.simple.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Handler implements the abstract structure for handling messages from clients
 * and accessing the database as necessary. Intended to be extended by other classes.
 */
abstract class Handler implements HttpHandler {

    DbWrapper database;

    public Handler(DbWrapper db) {
        database = db;
    }

    /**
     * Handle requests from clients and access the database
     * @param t - HTTP exchange object, provided by the server when a client accesses a URL
     */
    @Override
    public void handle(HttpExchange t) {
        try {

            // read request data
            BufferedReader reqReader = new BufferedReader(new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8));
            String temp;
            StringBuilder req = new StringBuilder();implement user database connection:
        // Get user ID from request
        // Access user DB to identify which message was sent last
        // collect all messages newer than this
        // aggregate in JSONArray, return as string
            while((temp = reqReader.readLine()) != null) {
                req.append(temp);
            }
            reqReader.close();

            // access database, with parsed json from request body
            String response = accessDB(req.toString());

            // respond to request
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();

        } catch (ParseException e1) {
            // JSON parse failed
            System.err.println("Unable to parse JSON message: " + e1.toString());
            // send error code to client
            sendError(t);
        } catch (IOException e2) {
            // Client communication failed
            System.err.println("Unable to communicate with client: " + e2.toString());
        }
    }

    /**
     * Respond to the client with an error code
     * @param t - HttpExchange that experienced the error
     */
    private void sendError(HttpExchange t) {
        try {
            t.sendResponseHeaders(500, 0);
        }
        catch (IOException e) {
            System.err.println("Unable to communicate with client: " + e.toString());
        }
    }

    /**
     * Access the database. Extended by child classes.
     * @param req - JSON String representing the data sent or requested by the client
     * @return - String response message for client
     * @throws ParseException - if JSON parse fails
     */
    abstract protected String accessDB(String req) throws ParseException;

    /**
     * parse a JSON message
     * @param jsonMsg - JSON string representing data sent by client
     * @return - JSONObject containing data parsed from string
     * @throws ParseException - if JSON parse fails
     */
    JSONObject parse(String jsonMsg) throws ParseException {
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(jsonMsg);
    }
}

/**
 * InitHandler handles the first request for messages when a user
 * logs in.
 */
class InitHandler extends Handler {

    private int mostRecentMessages = 50;

    public InitHandler(DbWrapper db) {
        super(db);
    }

    /**
     * Get a constant number of most recent messages from the database
     *
     * @param req - JSON String containing user account information
     * @return - JSON array containing a number of most recent messages
     * @throws ParseException - if JSON request is malformed
     */
    protected String accessDB(String req) {
        // TODO: return "mostRecentMessages" number of messages
        return "";
    }
}

/**
 * SendHandler handles new messages being sent to the server, storing
 * them in the message database.
 */
class SendHandler extends Handler {

    public SendHandler(DbWrapper db) {
        super(db);
    }

    /**
     * Put a new message in the database
     *
     * @param req - JSON String containing user's message
     * @return - Empty response string to signal a success
     * @throws ParseException - if JSON request is malformed
     */
    protected String accessDB(String req) throws ParseException {
        JSONObject json = parse(req);
        // TODO: add message to database

        return "";
    }
}
/**
 * PullHandler handles clients asking for all new messages since the last
 * time they checked
 */
class PullHandler extends Handler {

    public PullHandler(DbWrapper db) {
        super(db);
    }

    /**
     * Get all the messages in the database that were sent since the
     * last time this user requested new messages
     *
     * @param req - JSON String containing user account information
     * @return - JSON array containing a number of most recent messages
     * @throws ParseException - if JSON request is malformed
     */
    protected String accessDB(String req) {
        // TODO: return all messages newer than given user's most recently viewed message
        return "";
    }
}
