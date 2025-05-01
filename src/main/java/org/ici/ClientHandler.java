package org.ici;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ici.exceptions.ClientNotHandledException;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(ClientHandler.class);

    private static final List<ClientHandler> CLIENTS = new ArrayList<>();
    private final Socket socket;
    private final String clientIpAddress;
    private final String name;
    private final int clientPort;
    private final BufferedReader bufferedReader;
    private final PrintWriter printWriter;

    public ClientHandler(Socket socket) throws ClientNotHandledException {
        this.socket = socket;
        this.clientIpAddress = socket.getInetAddress().getHostAddress();
        this.clientPort = socket.getPort();
        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
            this.name = this.bufferedReader.readLine();
        } catch (IOException e) {
            String error = String.format("Failed to handle a new Client with ip: %s and port: %s", this.clientIpAddress, this.clientPort);
            logger.error(error);
            closeResources();
            throw new ClientNotHandledException(error);
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
            logger.error("Could not close resources");
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
            logger.info("{}:{}:{} -> Joined the chat!", this.clientIpAddress, this.clientPort, this.name);
            broadcast(this.clientIpAddress + ":" + this.clientPort + ":" + this.name + " -> Joined the chat, say hello!");

            String receivedMessage;

            while((receivedMessage = this.bufferedReader.readLine()) != null) {

                logger.info("Client({}:{}:{}): {}", this.clientIpAddress, this.clientPort, this.name, receivedMessage);


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
            logger.error("Client Socket closed");
        } finally {
            closeResources();
        }
    }

    private void broadcast(final String msg) {
        logger.info("Broadcasting");
        CLIENTS.stream().filter(client -> client.clientPort != this.clientPort)
                .forEach(clientHandler -> clientHandler.sendMessage(msg));
    }
}
