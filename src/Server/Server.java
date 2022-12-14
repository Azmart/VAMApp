package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private static final ArrayList<ClientHandler> clientHandler = new ArrayList<ClientHandler>();

    public static void main(String[] args) {
        ServerSocket serverSocket;
        Socket socket;
        try {
            serverSocket = new ServerSocket(8889);
            while (true) {
                System.out.println("Waiting for clients...");
                socket = serverSocket.accept();
                System.out.println("Connected");
                ClientHandler clientThread = new ClientHandler(socket, clientHandler);
                clientHandler.add(clientThread);
                clientThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
