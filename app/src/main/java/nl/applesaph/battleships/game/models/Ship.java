package nl.applesaph.battleships.game.models;

import java.util.ArrayList;

import lombok.Data;

@Data
public class Ship {
    private final ArrayList<ShipPart> shipParts = new ArrayList<>();

    /**
     * The Ship got hit on x and y!
     *
     * @param x
     * @param y
     * @return true if it got hit
     */
    public boolean setHit(int x, int y) {
        for (ShipPart shipPart : shipParts) {
            if (shipPart.getX() == x && shipPart.getY() == y) {
                shipPart.setHit(true);
                return true;
            }
        }
        return false;
    }

    /**
     * OMG did it sink fr fr?!
     *
     * @return true.. omg did it really?
     */
    public boolean hasSunk() {
        for (ShipPart shipPart : shipParts) {
            if (!shipPart.isHit()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add a part to this ship, make it longer, stronger and better!
     *
     * @param x where? horizontally
     * @param y where? vertically
     */
    public void addShipPart(int x, int y) {
        shipParts.add(new ShipPart(x, y));
    }
}
