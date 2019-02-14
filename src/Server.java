package src;

import org.json.simple.JSONObject;

import java.io.*;
import java.net.*;

// A Java program for a Server
import java.net.*;
import java.io.*;

public class Server
{
    //initialize socket and input stream
    private Socket          socket   = null;
    private ServerSocket    server   = null;
    private DataInputStream in       =  null;

    // constructor with port
    public Server(int port)
    {
        // starts server and waits for a connection
        try
        {
            server = new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");

            // accept client
            socket = server.accept();
            System.out.println("Client accepted\n");

            // parse client IP
            String clientIP = socket.getRemoteSocketAddress().toString().split(":")[0].substring(1);


            // takes input from the client socket
            in = new DataInputStream(
                new BufferedInputStream(socket.getInputStream()));

            String line = "";

            // reads message from client until "Shh" is sent
            while (!line.equals("Shh"))
            {
                try
                {
                    line = in.readUTF();
                    System.out.print(clientIP);
                    System.out.print(" says: ");
                    System.out.println(line);

                }
                catch(IOException i)
                {
                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");

            // close connection
            socket.close();
            in.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }

    // message receiver function
    private JSONObject getMessage(DataInputStream in) {
        return null;
    }
    public static void main(String args[])
    {

        Server server = new Server(5000);
    }
}