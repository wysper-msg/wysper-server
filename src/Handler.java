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
public class Handler implements HttpHandler {

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
     * @throws ParseException - if JSON parse fails
     */
    protected String accessDB(String req) throws ParseException {
        System.out.println(req);
        return "";
    }

    /**
     *
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
// InitHandler handles client registration and login on the server
class InitHandler extends Handler {

    @Override
    String accessDB(String JsonUsr) {
        // prototype: checks if user is in the DB. If not, add a new entry for them
        System.out.println(JsonUsr);
        return "";
    }

}
*/

// SendHandler handles new messages sent by the client
class SendHandler extends Handler {

    @Override
    protected String accessDB(String req) throws ParseException {
        JSONObject json = parse(req);
        // TODO: store JSON data in message database
        System.out.println(json.get("username") + ": " + json.get("body"));
        return "";
    }
}

// PullHandler handles clients requesting all new messages since last request
class PullHandler extends Handler {

    @Override
    protected String accessDB(String req) {
        // TODO: implement user database connection:
        // Get user ID from request
        // Access user DB to identify which message was sent last
        // collect all messages newer than this
        // aggregate in JSONArray, return as string
        return "";
    }
}
