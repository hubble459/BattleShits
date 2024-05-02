package nl.applesaph.battleships.game;

import nl.applesaph.battleships.game.models.Player;
import nl.applesaph.battleships.game.models.Ship;
import nl.applesaph.battleships.game.models.ShipPart;
import nl.applesaph.battleships.server.SendCommand;
import nl.applesaph.battleships.server.Server;

import java.util.HashMap;
import java.util.Random;

/**
 * Create a Game of BattleShips
 */
public class Game {
    private final Server server;
    private final int[][] grid = new int[25][25];
    private final HashMap<Integer, Player> players = new HashMap<>();
    private GameState gameState = GameState.LOBBY;
    private int currentPlayer = 0;
    private int firstPlayer = 0;
    private int lastPlayer = 0;

    public Game(Server server) {
        this.server = server;
    }

    private void gameLoop() {
        if (gameState != GameState.RUNNING) return;
        int winner = checkWinner();

        if (winner != -1) {
            gameState = GameState.FINISHED;
            server.sendCommand(SendCommand.WINNER, winner);
            return;
        }

        server.sendCommand(SendCommand.TURN, currentPlayer);
    }

    /**
     * Check who won
     *
     * @return winner ID if found, -1 if not
     */
    private int checkWinner() {
        int amountOfPlayersAlive = 0;
        int winner = -1;
        for (Player player : players.values()) {
            if (!player.hasLost()) {
                amountOfPlayersAlive++;
                winner = player.getPlayerNumber();
            }
        }
        if (amountOfPlayersAlive == 1) {
            return winner;
        } else {
            return -1;
        }
    }

    private int changeTurn(int currentPlayer) {
        //if next player is not dead, return next player, wrap around if needed
        if (currentPlayer < lastPlayer) {
            //check if the next player exits
            if (players.get(currentPlayer + 1) != null && !players.get(currentPlayer + 1).hasLost()) {
                return currentPlayer + 1;
            } else {
                return changeTurn(currentPlayer + 1);
            }
        } else {
            if (players.get(firstPlayer) != null && !players.get(firstPlayer).hasLost()) {
                return firstPlayer;
            } else {
                firstPlayer++;
                return changeTurn(firstPlayer + 1);
            }
        }
    }

    /**
     * Initiate the grid with random ships for each player,
     * because where is the fun in placing your own ship
     */
    public void initGrid() {
        // I'm so silly and random uwu
        Random random = new Random();

        // The word integer really speaks to me,
        // unlike something BAKA-ish like playerNumber
        for (int integer : players.keySet()) {
            // Do you know the ship Shimakaze?
            Ship ship = new Ship();

            // I'm so quirky in switching up the x and the y here
            int x = random.nextInt(grid.length);
            int y = random.nextInt(grid[0].length);
            while (grid[x][y] != 0 || checkNeighbors(grid, x, y, 4)) {
                x = random.nextInt(grid.length);
                y = random.nextInt(grid[0].length);
            }
            // They should really make something like a {@link Random#nextBoolean} for stuff like this!!! URGH amirite?
            int upOrDown = random.nextInt(2);
            // Choose whether to place the ship vertically or horizontally
            if (upOrDown == 0) {
                // Check if the ship is out of bounds, if so, move it back
                if (y + 2 >= grid[0].length) {
                    y -= 2;
                }
                if (y - 2 < 0) {
                    y += 2;
                }
                // I love keeping track of two datasets that have different structures
                grid[x][y] = integer;
                grid[x][y + 1] = integer;
                grid[x][y + 2] = integer;
                ship.addShipPart(x, y);
                ship.addShipPart(x, y + 1);
                ship.addShipPart(x, y + 2);
            } else {
                // Check if the ship is out of bounds, if so, move it back
                // btw x is actually y.. shhh!!!
                if (x + 2 >= grid.length) {
                    x -= 2;
                }
                if (x - 2 < 0) {
                    x += 2;
                }
                grid[x][y] = integer;
                grid[x + 1][y] = integer;
                grid[x + 2][y] = integer;
                ship.addShipPart(x, y);
                ship.addShipPart(x + 1, y);
                ship.addShipPart(x + 2, y);
            }
            // Get player by it's personal integer (ughh~ this word >~<)
            // I wish someone gave me an integer
            players.get(integer).addShip(ship);
        }
    }

    /**
     * My dad works at Microsoft and he told me that this would
     * iterate through all neighboring points on a two dimensional
     * space with a radius of n
     *
     * I used the terms array, x, y, and n because this makes it more readable!
     *
     * @param array grid
     * @param x coordinate
     * @param y coordinate
     * @param n radius
     * @return if all spots in radius n are empty return false, else return true
     */
    public static boolean checkNeighbors(int[][] array, int x, int y, int n) {
       /**
        * These two loops basically create a figurative mini grid
        * If n is two it would look like this
        * [x-2 & y-2] [x-1] [x] [x+1] [x+2]
        * [y-1]
        *  [y]
        * [y+1]
        * [y+2]                      [end]
        */

        // start from n to the left of x
        // continue until x + n is reached
        for (int i = x - n; i <= x + n; i++) {
            // start from y - n and continue until y + n is reached
            for (int j = y - n; j <= y + n; j++) {
                // Check that `i` and `j` can exist in the grid
                // then check if there is a neighbor there
                if (i >= 0 && i < array.length && j >= 0 && j < array[0].length && array[i][j] != 0) {
                    // return true if a neighbor is found
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Add a player to the game
     *
     * @param playerNumber id of the player
     * @param player got rizz
     */
    public void addPlayer(int playerNumber, Player player) {
        players.put(playerNumber, player);
    }

    /**
     * Remove a player from the game if they are misbehaving
     *
     * @param playerNumber id of the player
     */
    public void removePlayer(int playerNumber) {
        players.remove(playerNumber);
    }

    /**
     * BATORU ROWAIARU
     */
    public void startGame() {
        // Fill the grid with ships
        initGrid();

        // We stateful up in this bitch
        gameState = GameState.RUNNING;

        // Notify users of a new game, and specify the grid size, x~y
        // Player 0 is the game admin
        server.sendCommand(SendCommand.NEWGAME, grid[0].length + "~" + grid.length , 0);
        // Absolute chad way to get the first integ- i mean player
        currentPlayer = players.keySet().iterator().next();
        firstPlayer = currentPlayer;
        // *bites lip* integer
        for (int integer : players.keySet()) {
            // Go through all ships and send an event for all ship parts
            for (Ship ship : players.get(integer).getShips()) {
                for (ShipPart part : ship.getShipParts()) {
                    server.sendCommand(SendCommand.POS, part.getX() + "~" + part.getY(), integer);
                }
            }
            // If integer is bigger than the current last player,
            // then integer is bigger
            if (integer > lastPlayer) {
                lastPlayer = integer;
            }
        }
        // Loop the game
        gameLoop();
        // Print the grid
        printGrid(grid);
    }

    /**
     * Hit that gritty
     *
     * @param grid s teeth
     */
    public void printGrid(int[][] grid) {
        // Print garbled messages to confuse the end user
        System.out.println("  0123456789111111111122222");
        // Not my phone number
        System.out.println("            012345678901234");
        // Iterate over the grid rows
        for (int y = 0; y < grid.length; y++) {
            // Align the number if it's bellow 10 :biting_lips:
            // Basically System.out.printf("%02d", y) but better
            if (y < 10) System.out.print("0");
            System.out.print(y);

            // Go through the row
            for (int x = 0; x < grid[y].length; x++) {
                // No ShipPart found!
                if (grid[x][y] == 0) {
                    System.out.print("_");
                } else {
                    // ShipPart found! Print the INTEGER that's inside of it
                    System.out.print(grid[x][y]);
                }
            }
            // New line
            System.out.println();
        }
    }

    /**
     * Just end it already!
     *
     * @param winner winner chicken dinner
     */
    public void endGame(Player winner) {
        // It's over between us, we're GameState.FINISHED!
        gameState = GameState.FINISHED;
        // Tell all gamers who's the pogchamp
        server.sendCommand(SendCommand.WINNER, winner.getPlayerNumber());
        // Mr. Resseti coming in like champ
        resetGame();
    }

    /**
     * Definitely one of those turns
     *
     * @param playerNumber integer
     * @return yes, is turn. or no.. not turn
     */
    public boolean isTurn(int playerNumber) {
        return playerNumber == currentPlayer;
    }

    /**
     * You can't handle me
     *
     * @param playerNumber aka integer
     * @param x or y, who cares
     * @param y or x, i care
     */
    public void handleTurnMessage(int playerNumber, int x, int y) {
        // Can't handle shit if it's not their turn
        if (isTurn(playerNumber)) {
            // LMAO YOU GOT ONE!
            if (grid[x][y] != 0 && grid[x][y] != -1) {
                // HIT THAT MOFO
                players.get(grid[x][y]).isHit(x, y);
                // TELL EVERYONE
                server.sendCommand(SendCommand.HIT, x + "~" + y, grid[x][y]);
                // SET THAT BAD BOY TO ZERO
                grid[x][y] = 0;
            } else {
                // No bitches
                server.sendCommand(SendCommand.MISS, x + "~" + y, playerNumber);
                // Dead space
                grid[x][y] = -1;
            }
            // Who is turn?
            currentPlayer = changeTurn(currentPlayer);
            // Like a record baby, loop right round
            gameLoop();
            // Show server admin what's popping
            printGrid(grid);
        }
    }

    /**
     * Mr. Resseti in action
     */
    public void resetGame() {
        for (int y = 0; y < grid.length; y++) {
            // Taking a different approach and using x to index grid here, instead of 0
            for (int x = 0; x < grid[x].length; x++) {
                grid[x][y] = 0;
            }
        }
        // Fuck em, gone
        players.clear();
        // TFW GameState.LOBBY
        gameState = GameState.LOBBY;
    }

    /**
     * Who even calls this method?
     *
     * @return grid
     */
    public int[][] getGrid() {
        return grid;
    }

    /**
     * When you're feeling weak
     * and can't choose between one of the grid.length * grid[0].length points
     * you may skip the turn, although this will not benefit you in any way
     */
    public void skipTurn() {
        if (gameState != GameState.RUNNING) throw new IllegalStateException("There is no game running!");
        // Get anyone but you to play
        currentPlayer = changeTurn(currentPlayer);
        // LOOOOOOP
        gameLoop();
    }
}
