package nl.applesaph.battle_ships.server;

import java.io.IOException;
import java.util.List;

/**
 * If you want to implement your own server,
 * use me... 👉👈
 */
public interface ServerInterface {
    /**
     * Start server
     *
     * @throws IOException when port is already busy or somthing
     */
    void start() throws IOException;

    /**
     * Stop the server
     */
    void stop();

    /**
     * On what port am I running
     * @return port
     */
    int getPort();

    /**
     * Start a new game with all these clients
     *
     * @param clientHandlers players before they're called players
     */
    void newGame(List<ClientHandler> clientHandlers);

    /**
     * Is this guy actually playing?
     *
     * @param clientHandler some bot
     * @return yes if they're in game
     */
    boolean inGame(ClientHandler clientHandler);

    /**
     * Probably something like
     * {@code client.send("move to this x and y");}
     *
     * @param client
     * @param x
     * @param y
     */
    void handleTurnMessage(ClientHandler client, int x, int y);

    /**
     * Yeah! Set it up!!!
     *
     * @param client gregg
     * @param message "hey, is your friend still single?"
     */
    void handleSetupTurnMessage(ClientHandler client, String message);

    /**
     * Today we held a meeting to figure out
     * what the most confusing method name
     * for checking if a player is logged in by their username
     *
     * This one won, who's username?
     *
     * @param username are they logged in?
     * @return this is probably the user id
     */
    int checkUsernameLoggedIn(String username);

    /**
     * Try returning a list of nulls, you'll have more players right?
     * Or would that break everything
     * @return clients with a username
     */
    List<ClientHandler> getLoggedInClients();

    /**
     * Who wants to play??! Yeah you do!! LETS PLAY
     *
     * @return a list of localhost clients
     */
    List<ClientHandler> getWaitingClients();

    /**
     * When gregg wont stop self loathing, just kick him
     * 
     * @param playerNumber greggs id, probably 1
     */
    void removeClient(int playerNumber);

    /**
     * Add it
     * 
     * @param PlayerNumber
     * @param clientHandler
     */
    void addClient(int PlayerNumber, ClientHandler clientHandler);

    /**
     * Aka broadcasting
     * 
     * @param message keep it SFW
     */
    void sendToAll(String message);

    /**
     * Bully someone by leaving them out of your conversation
     * 
     * @param message smack about the excepted ones
     * @param playerNumbers except these
     */
    void sendToAllExcept(String message, int[] playerNumbers);

    /**
     * Private message 😳
     * 
     * @param message
     * @param playerNumber
     */
    void sendToClient(String message, int playerNumber);

    /**
     * Get you a group chat functionality
     * 
     * @param message
     * @param playerNumbers
     */
    void sendToClients(String message, int[] playerNumbers);
}
