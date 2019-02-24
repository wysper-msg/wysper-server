package src;

import org.json.simple.*;

import java.io.BufferedReader;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStreamReader;
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
        System.out.println(String.format("Starting server on port %d", 8000));
        // initialize connection URLs
        server.createContext("/init", new InitHandler());
        server.createContext("/send", new SendHandler());
        server.createContext("/poll", new PollHandler());
        // start server
        server.setExecutor(null); // creates a default executor
        server.start();
        // wait for command to terminate server
        Scanner in = new Scanner(System.in);
        while (true) {
            String line = in.nextLine();
            if((line != null) && (line.equals("exit"))) {
                break;
            }
        }
        // stop server
        server.stop(0);
        System.out.println("Stopping server...");
    }
}

class DBHandler implements  HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        // read data
        BufferedReader msgReader = new BufferedReader(new InputStreamReader(t.getRequestBody(), "utf-8"));
        String msg;
        StringBuffer msgBuffer = new StringBuffer();
        while((msg = msgReader.readLine()) != null) {
            msgBuffer.append(msg);
        }
        msgReader.close();
        // write to database
        accessDB(msgBuffer.toString());
        // respond to request
        String response = "You initialized a connection! (added you to / accessed your entry in user DB)";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    // access database with json message
    // Intended to be overridden in child classes
    void accessDB(String jsonMsg) {
        System.out.println(jsonMsg);
    }

}

// InitHandler handles client registration and login on the server
class InitHandler extends DBHandler {

    @Override
    void accessDB(String JsonUsr) {
        // prototype: checks if user is in the DB. If not, add a new entry for them
        System.out.println(JsonUsr);
    }

}

// SendHandler handles new messages sent by the client
class SendHandler extends DBHandler {

    @Override
    void accessDB(String JsonMsg) {
        // prototype: checks if user is in the DB. If not, add a new entry for them
        System.out.println(JsonMsg);
    }

}

// PollHandler handles clients requesting all new messages since last request
class PollHandler extends DBHandler {

    @Override
    void accessDB(String JsonMsg) {
        // prototype: checks if user is in the DB. If not, add a new entry for them
        System.out.println(JsonMsg);
    }

}