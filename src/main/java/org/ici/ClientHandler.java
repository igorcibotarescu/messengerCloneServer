import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private static final List<ClientHandler> CLIENTS = new ArrayList<>();
    private final Socket socket;
    private final String clientIpAddress;
    private final String name;
    private final int clientPort;
    private final BufferedReader bufferedReader;
    private final PrintWriter printWriter;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientIpAddress = socket.getInetAddress().getHostAddress();
        this.clientPort = socket.getPort();
        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
            this.name = this.bufferedReader.readLine();
        } catch (IOException e) {
            System.err.println("Failed to handle a new Client!");
            closeResources();
            throw new RuntimeException(e);
        }
        CLIENTS.add(this);
    }

    private void closeResources() {
        CLIENTS.remove(this);
        try {
            if (this.socket != null) {
                this.socket.close();
            }
            if (this.printWriter != null) {
                this.printWriter.close();
            }
            if (this.bufferedReader != null) {
                this.bufferedReader.close();
            }
        } catch (IOException e) {
            System.err.println("Could not close resources");
        }
    }

    private void sendMessage(String message) {
        if(this.printWriter != null) {
            this.printWriter.println(message);
        }
    }

    @Override
    public void run() {
        try {
            System.out.printf("%s:%d:%s -> Joined the chat!%n", this.clientIpAddress, this.clientPort, this.name);
            broadcast(this.clientIpAddress + ":" + this.clientPort + ":" + this.name + " -> Joined the chat, say hello!");

            String receivedMessage;

            while((receivedMessage = this.bufferedReader.readLine()) != null) {

                System.out.printf("Client(%s:%d:%s): %s%n", this.clientIpAddress, this.clientPort, this.name, receivedMessage);


                if (receivedMessage.contains("->quit")) {
                    String client = receivedMessage.split("->quit")[0];
                    broadcast(client + " left the chat!");
                    break; // terminate client Thread
                }

                if (receivedMessage.equalsIgnoreCase("show clients")) {

                    this.sendMessage(String.valueOf(CLIENTS.stream().
                            filter(client -> client.clientPort != this.clientPort).map(c -> c.clientPort).toList()));
                    continue;
                }

                broadcast(this.clientIpAddress + ":" + this.clientPort + ":"+ this.name + " -> " + receivedMessage);
            }

        } catch (IOException e) {
            System.err.println("Client Socket closed");
        } finally {
            closeResources();
        }
    }

    private void broadcast(final String msg) {
        System.out.println("Broadcasting");
        CLIENTS.stream().filter(client -> client.clientPort != this.clientPort)
                .forEach(clientHandler -> clientHandler.sendMessage(msg));
    }
}
