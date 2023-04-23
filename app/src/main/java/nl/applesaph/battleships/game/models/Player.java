package nl.applesaph.battleships.game.models;

import java.util.ArrayList;

import lombok.Data;

@Data
public class Player {
    private final ArrayList<Ship> ships = new ArrayList<>();
    private final int playerNumber;
    private final String playerName;

    public Player(int playerNumber, String playerName) {
        this.playerNumber = playerNumber;
        this.playerName = playerName;
    }

    /**
     * Check if a ship is hit on a specific coordinate
     *
     * @param x
     * @param y
     * @return true if a ship is hit on that point
     */
    public boolean isHit(int x, int y) {
        for (Ship ship : ships) {
            if (ship.setHit(x, y)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a ship to the player board
     * @param ship
     */
    public void addShip(Ship ship) {
        ships.add(ship);
    }

    /**
     * Checks if the player has lost
     * @return true if the player has lost, false if not
     */
    public boolean hasLost() {
        for (Ship ship : ships) {
            if (!ship.hasSunk()) {
                return false;
            }
        }

        return true;
    }
}
