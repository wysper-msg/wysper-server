package src; // Server for the Wysper messaging service

import java.util.Scanner;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

public class Server {

    /**
     *
     * @param port - Port to open a server on
     * @return - Server object configured and ready to use
     */
    private static HttpServer initServer(int port) throws Exception {
        // create server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        // initialize connection URLs
        server.createContext("/init", new InitHandler());
        server.createContext("/send", new SendHandler());
        server.createContext("/poll", new PullHandler());
        // creates a default executor
        server.setExecutor(null);
        return server;
    }

    public static void main(String[] args) throws Exception {

        // get port
        System.out.print("Enter server port: ");
        Scanner scanner = new Scanner(System.in);
        int port = scanner.nextInt();

        // start server
        HttpServer s = initServer(port);
        s.start();
        System.out.println("Starting server. Type \'Exit\' to quit.");

        // wait for termination command
        String command;
        while (true) {
            command = scanner.nextLine();
            if((command != null) && (command.equals("Exit"))) {
                break;
            }
        }

        // stop server
        s.stop(0);
    }
}