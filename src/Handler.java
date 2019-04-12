package src;

import java.util.ArrayList;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.simple.*;
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

    Handler(DbWrapper db) {
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
            StringBuilder req = new StringBuilder();
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

    /**
     * Convert an array of Messages into a marshaled JSON string
     * @param newMessages Messages to marshal
     * @return JSON object holding marshaled message data
     */
    JSONObject marshalMessages(ArrayList<Message> newMessages) {
        // convert ArrayList to JSONArray
        JSONArray newMsgJson = new JSONArray();
        for(Message msg : newMessages) {
            newMsgJson.add(msg.toJSON());
        }
        // add JSONArray to a JSONObject
        JSONObject response = new JSONObject();
        response.put("messages", newMsgJson);

        // send JSON array to client
        return response;
    }
}

/**
 * InitHandler handles the first request for messages when a user
 * logs in.
 */
class InitHandler extends Handler {

    // get this many messages on the first connection
    int recentMessages = 69;
    InitHandler(DbWrapper db) {
        super(db);
    }

    /**
     * Get a constant number of most recent messages from the database
     * @param req - JSON String containing user account information
     * @return - JSON array containing a number of most recent messages
     */
    protected String accessDB(String req) {
        // insert username into database
        database.insertUser(req);
        // get constant number of recent messages
        ArrayList<Message> newMessages = database.getMessages(recentMessages);
        database.updateUsersLastRead(req);
        // send these messages in JSON String form to the client
        return marshalMessages(newMessages).toString();
    }
}

class NMesgHandler extends Handler {

    NMesgHandler(DbWrapper db) {
        super(db);
    }

    /**
     * Get a constant number of most recent messages from the database
     * @param req - JSON String containing user account information
     * @return - JSON array containing a number of most recent messages
     */
    protected String accessDB(String req) {
        // parse client's request
        int numMessages = Integer.getInteger(req);
        // get that many messages
        ArrayList<Message> newMessages = database.getMessages(numMessages);
        // send these messages in JSON String form to the client
        return marshalMessages(newMessages).toString();
    }
}

/**
 * SendHandler handles new messages being sent to the server, storing
 * them in the message database.
 */
class SendHandler extends Handler {

    SendHandler(DbWrapper db) {
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
        // parse JSON request
        JSONObject json = parse(req);
        // convert JSON into Message object
        Message msg = new Message(json);
        // insert user if they weren't already there
        database.insertUser(msg.getUsername());
        // add message to database
        database.insertMessage(msg);
        // empty string means success
        return "";
    }
}
/**
 * PullHandler handles clients asking for all new messages since the last
 * time they checked
 */
class PullHandler extends Handler {

    PullHandler(DbWrapper db) {
        super(db);
    }

    /**
     * Get all the messages in the database that were sent since the
     * last time this user requested new messages
     * @param req - JSON String containing user account information
     * @return - JSON array containing a number of most recent messages
     * @throws ParseException - if JSON request is malformed
     */
    protected String accessDB(String req) throws ParseException {
        // get an ArrayList of new messages this user has not seen
        ArrayList<Message> newMessages = database.getMessages(req);
        // send these messages in JSON String form to the client
        return marshalMessages(newMessages).toString();
    }
}
