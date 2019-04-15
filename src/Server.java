import java.util.Scanner;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

/**
 * The server for the Wysper messaging server
 */
public class Server {

    private static DbWrapper db;

    /**
     * Initialize HTTP server
     * @param port - Port to open a server on
     * @return - Server object configured and ready to use
     */
    private static HttpServer initServer(int port) throws Exception {
        // create server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        // initialize connection URLs
        server.createContext("/init", new InitHandler(db));
        server.createContext("/getn", new NMesgHandler(db));
        server.createContext("/send", new SendHandler(db));
        server.createContext("/poll", new PullHandler(db));
        // creates a default executor
        server.setExecutor(null);
        return server;
    }

    public static void main(String[] args) throws Exception {

        // check command-line args
        if(args.length != 2) {
            System.err.println("ERROR: Invalid command line arguments");
            System.err.println("Usage: java -jar wysper-server.jar <port> [\"no-save\"]");
            return;
        }

        // get port
        int port = Integer.valueOf(args[0]);
        // get database save choice
        boolean dropTables = args[1].equals("no-save");

        // start server
        db = new DbWrapper("wysperdb", true);
        HttpServer s = initServer(port);
        s.start();
        System.out.println(String.format("Started server on port %d. Type \'Exit\' to quit.", port));

        // wait for termination command
        Scanner scanner = new Scanner(System.in);
        String command = null;
        while (command == null || !command.equals("Exit")) {
            command = scanner.nextLine();
            if (command.equals("ShowMessages")) {
                db.displayAllMessages();
            }
            if (command.equals("ShowUsers")) {
                db.displayAllUsers();
            }
        }

        // stop server
        s.stop(0);
        System.out.println("Shutting down server...");
        db.cleanup(dropTables);
    }
}