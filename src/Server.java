package src; // Server for the Wysper messaging service

import java.util.Scanner;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

public class Server {

    private static DbWrapper db;
    private static boolean dropTables = false;

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
        server.createContext("/send", new SendHandler(db));
        server.createContext("/poll", new PullHandler(db));
        // creates a default executor
        server.setExecutor(null);
        return server;
    }

    private static void saveSessionChoice(Scanner scanner) {

        String choice = scanner.nextLine();
        do {
            System.out.print("Permanently delete data on shutdown? (y/n) (default n): ");
            choice = scanner.nextLine();

        } while (!(choice.equals("y") || choice.equals("n") || choice.equals("")));

        if (choice.equals("n") || choice.equals("")) {
            dropTables = false;
        }
        else {
            dropTables = true;
        }
    }

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        // get port
        System.out.print("Enter server port: ");
        int port = scanner.nextInt();


        // start server
        saveSessionChoice(scanner);
        db = new DbWrapper(true);
        HttpServer s = initServer(port);
        s.start();
        System.out.println("Starting server. Type \'Exit\' to quit.");

        // wait for termination command
        String command = null;
        while (command == null || !command.equals("Exit")) {
            command = scanner.nextLine();
            if (command.equals("ShowMessages")) {
                db.displayAll();
           }
        }

        // stop server
        s.stop(0);
        System.out.println("Shutting down server...");
        db.cleanup(dropTables);
    }
}