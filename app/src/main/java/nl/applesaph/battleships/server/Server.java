package nl.applesaph.battleships.server;

import nl.applesaph.battleships.game.Game;
import nl.applesaph.battleships.game.models.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

/**
 * This is like the command center for all cute battle ships like Shimakaze
 */
public class Server implements ServerInterface, Runnable {
    private final HashMap<Integer, ClientHandler> clientHandlers = new HashMap<>();
    private final Game game = new Game(this);
    private final int port;
    private ServerSocket serverSocket;
    private Thread serverThread;

    public Server(int port) {
        this.port = port;
    }

    /**
     * Light it up
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        serverThread = new Thread(this);
        serverThread.start();
        // Only necessary to print the port,
        // because my dad put a password on the
        // port forwarding page
        System.out.println("Server started at " + port);
    }

    /**
     * Read the java doc for {@link Thread#join()} if you want to shed a tear
     */
    public void stop() {
        try {
            serverSocket.close();
            serverThread.join();
        } catch (IOException | InterruptedException ignored) {}
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void newGame(List<ClientHandler> clientHandlers) {
        // fuck you with your new game
    }

    @Override
    public boolean inGame(ClientHandler clientHandler) {
        // no one is playing, always false
        return false;
    }

    @Override
    public void handleTurnMessage(ClientHandler client, int x, int y) {
        // Make the game handle it
        // This exact if statement can also be found in the method that's being called when true
        // We're such silly programmers, gotta make sure nothing can get thru if not is turn!!
        if (game.isTurn(client.getPlayerNumber())) {
            game.handleTurnMessage(client.getPlayerNumber(), x, y);
        }
    }

    /**
     * Deviating from the interface a lil'
     * Skipping is a great addition to the gameplay
     *
     * @throws IllegalStateException
     */
    public void skipTurn() throws IllegalStateException {
        game.skipTurn();
    }

    @Override
    public void handleSetupTurnMessage(ClientHandler client, String message) {
        // how about you handle it yourself
    }

    /**
     * Case insensitive
     */
    @Override
    public int checkUsernameLoggedIn(String username) {
        // If it fits on one line it's faster
        return clientHandlers
            // Go through clients
            .values()
            .stream()
            // Find clients matching the given username
            .filter(client -> client.getUsername().equalsIgnoreCase(username))
            // Stop searching when found
            .findFirst()
            // Map client to its player number
            .map((client) -> client.getPlayerNumber())
            // If not found return -1
            .orElse(-1);
    }

    @Override
    public List<ClientHandler> getLoggedInClients() {
        // Oof
        return null;
    }

    @Override
    public List<ClientHandler> getWaitingClients() {
        // Bigger oof
        return null;
    }

    @Override
    public synchronized void removeClient(int playerNumber) {
        // Gone but not forgotten
        clientHandlers.remove(playerNumber);
        // Shouldn't you tell him that he's been removed tho?
        // This is kind of sad...
    }

    @Override
    public synchronized void addClient(int playerNumber, ClientHandler clientHandler) {
        clientHandlers.put(playerNumber, clientHandler);
    }

    @Override
    public void sendToAll(String message) {
        clientHandlers.forEach((key, value) -> value.send(message));
    }

    @Override
    public void sendToAllExcept(String message, int[] playerNumbers) {
        clientHandlers.forEach((key, value) -> {
            for (int playerNumber : playerNumbers) {
                if (key != playerNumber) {
                    value.send(message);
                }
            }
        });
    }

    @Override
    public void sendToClient(String message, int playerNumber) {
        if (clientHandlers.containsKey(playerNumber)) {
            clientHandlers.get(playerNumber).send(message);
        }
    }

    @Override
    public void sendToClients(String message, int[] playerNumbers) {
        clientHandlers.forEach((key, value) -> {
            for (int playerNumber : playerNumbers) {
                if (key == playerNumber) {
                    value.send(message);
                }
            }
        });
    }

    public synchronized String getUsername(int playerNumber) {
        return clientHandlers.get(playerNumber).getUsername();
    }

    @Override
    public void run() {
        boolean running = true;
        while (running && !serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                // Check for the first message, this should be the username
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // Definitely a username without any weird characters
                // or a 2gb line of text
                String username = in.readLine();
                int playerNumber = checkUsernameLoggedIn(username);
                if (playerNumber != -1) {
                    if (!clientHandlers.isEmpty() && !clientHandlers.get(playerNumber).getSocket().isClosed()) {
                        socket.close();
                        // When username is already taken, randomly close the client socket
                        continue;
                    }
                } else {
                    playerNumber = clientHandlers.size() + 1;
                }
                ClientHandler clientHandler = new ClientHandler(socket, this, playerNumber, username);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();

                // If you look closely you'll see that in the client handler
                // the client is adding itself to the server as well,
                // HashMap bae for just overwriting it tho <3
                clientHandlers.put(playerNumber, clientHandler);
            } catch (IOException e) {
                e.printStackTrace();
                running = false;
            }
        }
    }

    /**
     * Brain of the operation
     * Tested thoroughly!!!
     *
     * @param command
     * @param clientHandler
     * @param line
     * @throws IOException
     */
    public void handleCommand(ReceiveCommand command, ClientHandler clientHandler, String line) throws IOException {
        final int playerNumber = clientHandler.getPlayerNumber();

        // Versatile command
        switch (command) {
            case EXIT -> {
                game.removePlayer(playerNumber);
                clientHandler.close();
            }
            case MOVE -> {
                final String[] commandParts = line.split("~");
                if (commandParts.length != 3) {
                    sendCommand(SendCommand.ERROR, "Invalid move", playerNumber);
                    break;
                }
                final String xStr = commandParts[1];
                final String yStr = commandParts[2];

                if (!isNumeric(xStr) || !isNumeric(yStr)) {
                    sendCommand(SendCommand.ERROR, "Invalid move", playerNumber);
                    break;
                }
                if (!game.isTurn(playerNumber)) {
                    sendCommand(SendCommand.ERROR, "Not your turn", playerNumber);
                    break;
                }
                final int x = Integer.parseInt(xStr);
                final int y = Integer.parseInt(yStr);
                final int maxXSize = game.getGrid().length;
                if (x < 0 || x > maxXSize || y < 0 || y > game.getGrid()[0].length) {
                    sendCommand(SendCommand.ERROR, "X and Y need to be between 0 and " + maxXSize, playerNumber);
                    break;
                }
                // Tell client-chan what you want from it
                handleTurnMessage(clientHandler, x, y);
            }
            // What language is this
            case PING -> clientHandler.send("PONG");
            case PONG -> clientHandler.setLastPong(System.currentTimeMillis());
            case NEWGAME -> {
                try {
                    startGame();
                } catch (IllegalStateException e) {
                    sendCommand(SendCommand.ERROR, e.getMessage(), clientHandler.getPlayerNumber());
                }
            }
            case UNKNOWN -> {
                sendCommand(SendCommand.ERROR, "Invalid command", playerNumber);
            }
        }
    }

    /**
     * Someone wants to play!!!
     */
    public void startGame() {
        /*
         * // Most common error that gets thrown in this program
         * if (clientHandlers.size() < 2) throw new IllegalStateException("You need at least two connected clients to start a game!");
         */
        if (clientHandlers.size() < 2) {
            // Add bots
            
        }

        // OMG what if people were playing?!
        game.resetGame();
        // Re-adding these broken souls
        clientHandlers.forEach((key, value) -> game.addPlayer(key, new Player(key, getUsername(key))));
        // Let them play again, but with much less enthusiasm
        game.startGame();
    }

    /**
     * Check if string is numeric
     *
     * @param value possible number
     * @return true if number
     */
    private boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Send a basic command with empty string as message
     *
     * @param command
     * @param player
     */
    public void sendCommand(SendCommand command, int player) {
        sendCommand(command, "", player);
    }

    /**
     * Send a command to a client
     *
     * @param command
     * @param message
     * @param player
     */
    public void sendCommand(SendCommand command, String message, int player) {
        switch (command) {
            case HIT -> sendToAll("HIT~" + message + "~" + player);
            case MISS -> sendToAll("MISS~" + message);
            case WINNER -> sendToAll("WINNER~" + player);
            case LOST -> sendToAll("LOST~" + player);
            case ERROR -> sendToClient("ERROR~" + message, player);
            case EXIT -> sendToAll("EXIT");
            case TURN -> sendToAll("TURN~" + player);
            case NEWGAME -> sendToAll("NEWGAME~" + message);
            case PING -> sendToClient("PING", player);
            case PONG -> sendToClient("PONG", player);
            // Hope they reply with NYA~
            case HELLO -> sendToClient("HELLO~" + message, player);
            case POS -> sendToClient("POS~" + message, player);
        }
    }
}
