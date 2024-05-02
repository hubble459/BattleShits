package nl.applesaph.battleships.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Master of all clients,
 * can clone himself to run in separate threads
 * because he's cool like that (and Runnable)
 */
public class ClientHandler implements Runnable {
    private final int playerNumber;
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final Server server;
    private final String username;
    private boolean running;
    private long lastPong;

    /**
     * Handle the client (with care)
     * @param socket client
     * @param server server
     * @param playerNumber integer
     * @throws IOException If the client doesn't want to talk, it's gonna throw this
     */
    public ClientHandler(Socket socket, Server server, int playerNumber, String username) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.username = username;
        this.running = true;
        this.server = server;
        this.playerNumber = playerNumber;
        // Add myself to server-senpai
        server.addClient(playerNumber, this);
        // [CONNECT] 127.0.0.0:55555 with username how_do_i_port_forward [1]
        System.out.println("[CONNECT] " + socket.getInetAddress() + ":" + socket.getPort() + " with username " + server.getUsername(playerNumber) + " [" + playerNumber + "]");
        try {
            // Sleep for an arbitrary amount of time cus maybe he's not ready to talk yet
            Thread.sleep(100);
            // Bet now he is tho, HELLO, YOUR NEW NAME IS 1!
            server.sendCommand(SendCommand.HELLO, Integer.toString(playerNumber), playerNumber);
        } catch (InterruptedException e) {
            // ruh roh, someone interrupted my sleep
            e.printStackTrace();
        }
    }

    /**
     * This one is private because that's what End-to-end encryption is; private
     *
     * @param line not coke, just a string
     * @throws IOException when the break up goes wrong (close method)
     */
    private void parseIncomingMessage(String line) throws IOException {
        final ReceiveCommand command = ReceiveCommand.tryParse(line);
        server.handleCommand(command, this, line);
    }

    /**
     * Most likely because no one else joined, the client just left
     *
     * @throws IOException
     */
    protected void close() throws IOException {
        // :'<
        System.out.printf("[DISCONNECT] %s:%d with username %s [%d]%n", socket.getInetAddress(), socket.getPort(), server.getUsername(playerNumber), playerNumber);
        server.removeClient(playerNumber);
        in.close();
        out.close();
        socket.close();
    }

    /**
     * Send message to client
     *
     * @param message keep it SFW
     */
    protected void send(String message) {
        if (!running) {
            // Like SMH, when would this, like, ever happen?
            // TODO: keep it spicy and don't even check
            throw new IllegalStateException("Not running");
        }
        // Bruh, this bitch not even connected, how's it even running? Who did this LMAO
        if (!socket.isConnected()) {
            throw new IllegalStateException("Socket is not connected");
        }
        // Send message
        out.println(message);
        out.flush();
    }


    @Override
    public void run() {
        while (running) {
            try {
                // Wait for client to say something 👉👈
                String line = in.readLine();
                if (line != null) {
                    // What he mean tho 🥺
                    parseIncomingMessage(line);
                } else {
                    // omg it died.. :o
                    running = false;
                }
            } catch (IOException e) {
                running = false;
            }
        }
        try {
            // Done. If there was a ban function I would use it here
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    public void setLastPong(long lastPong) {
        this.lastPong = lastPong;
    }

    public long getLastPong() {
        return lastPong;
    }

    public Socket getSocket() {
        return socket;
    }
}
