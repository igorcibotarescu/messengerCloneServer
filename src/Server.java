import java.io.*;
import java.net.ServerSocket;

public class Server {
    public static final int PORT = 8000;

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running...");
            while (!serverSocket.isClosed()) {
                //Wait for client connections, server keeps running even if client disconnects
                new Thread(new ClientHandler(serverSocket.accept())).start();
            }
        }
    }
}
