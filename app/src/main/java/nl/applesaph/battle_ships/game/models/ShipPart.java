package nl.applesaph.battle_ships.game.models;

import lombok.Data;

@Data
public class ShipPart {
    private final int x;
    private final int y;
    private boolean isHit;

    public ShipPart(int x, int y) {
        this(x, y, false);
    }

    public ShipPart(int x, int y, boolean isHit) {
        this.x = x;
        this.y = y;
        this.isHit = isHit;
    }
}
