package src;

import org.json.simple.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

// main Server class
public class Server {

    public static void main(String[] args) throws Exception {
        // create server on port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        // initialize connection URLs
        server.createContext("/init", new InitHandler());
        server.createContext("/send", new MsgSendHandler());
        server.createContext("/poll", new MsgPollHandler());
        // start server
        server.setExecutor(null); // creates a default executor
        server.start();
    }
}

// InitHandler handles client registration and login on the server
class InitHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        String response = "You initialized a connection! (added you to / accessed your entry in user DB)";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    int addUserToDB(String JsonUsrObject) {
        // prototype: checks if user is in the DB. If not, add a new entry for them
        return 0;
    }

}

// MsgSendHandler handles new messages sent by the client
class MsgSendHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        String response = "You sent a message to the server! (added message to message DB)";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    int addMsgToDB(String JsonMsgObject) {
        // prototype: add message data to message DB
        return 0;
    }
}

// MsgPollHandler handles clients requesting all new messages since last poll
class MsgPollHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        String response = "You asked for recent messages! (sent you new entries in message DB)";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    int updateUser(int userId, int mostRecentMessage) {
        // prototype: update user's entry to the most recently read message
        return 0;
    }

}