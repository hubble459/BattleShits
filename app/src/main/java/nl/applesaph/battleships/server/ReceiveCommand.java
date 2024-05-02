package nl.applesaph.battleships.server;

/**
 * Is for me?
 */
public enum ReceiveCommand {
    MOVE,
    EXIT,
    PING,
    PONG,
    UNKNOWN,
    NEWGAME;

    public static ReceiveCommand tryParse(String line) {
        if (line == null || line.isEmpty()) {
            return UNKNOWN;
        }

        final String[] commandParts = line.split("~", 2);
        final String command = commandParts[0];

        switch (command) {
            case "EXIT" ->    {return ReceiveCommand.EXIT;}
            case "MOVE" ->    {return ReceiveCommand.MOVE;}
            case "PING" ->    {return ReceiveCommand.PING;}
            case "PONG" ->    {return ReceiveCommand.PONG;}
            case "NEWGAME" -> {return ReceiveCommand.NEWGAME;}
            default ->        {return ReceiveCommand.UNKNOWN;}
        }
    }
}
