package org.ici;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ici.exceptions.*;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;

public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);
    private static int PORT;

    public static void main(String[] args) throws IOException {

        String errorMessage;

        if(args.length == 0) {
            errorMessage = "No port provided";
            logger.error(errorMessage);
            throw new NoPortArgException(errorMessage);
        }
        try {
            PORT = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            errorMessage = "No valid number";
            logger.error(errorMessage);
            throw new InvalidNumberException(errorMessage);
        }
        if(!validPort(PORT)) {
            errorMessage = "Invalid number for port";
            logger.error(errorMessage);
            throw new InvalidPortException(errorMessage);
        }

        try {
            startServer(PORT);
        } catch (PortAlreadyInUseException e) {
            errorMessage = String.format("%d port already in use. Running on a separate one", PORT);
            logger.warn(errorMessage);
            startServer(0);
        } catch (IOException e) {
            errorMessage = "Error occurred when initializing ServerSocket";
            logger.error(errorMessage);
            System.exit(1);
        }
    }

    private static boolean validPort(final int port) {
        return port > 0 && port <= (Math.pow(2, 16) - 1);
    }

    private static void startServer(final int port) throws IOException {
        try(ServerSocket serverSocket = new ServerSocket(port)){
            PORT = serverSocket.getLocalPort();
            logger.info("Server is running on port: {}", PORT);
            while (!serverSocket.isClosed()) {
                new Thread(new ClientHandler(serverSocket.accept())).start();
            }
        } catch (BindException e) {
            throw new PortAlreadyInUseException(e);
        } catch (ClientNotHandledException e) {
            logger.error("Unexpected failure from client");
        }
    }
}

