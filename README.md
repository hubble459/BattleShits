# BattleShipsServer

BattleShips is a simple game where there is a 2 dimensional grid of 25 by 25 tiles. Each player is given a ship that consists of 3 tiles, which is then placed onto this grid at a random location. Players can each send one torpedo a turn at chosen coordinates. The game will then tell the player if they hit an enemy ship, or if they missed their shot. A winner is declared when all but one ship have sunk.

This is the server for the BattleShips game. The client can be found at [BattleShipsClient](https://github.com/gerwintrip/BattleShipsClient).

## Installation

1. Clone this repository or download and extract the zip file.
2. Run `./gradlew run`
3. Enter the port number, or press enter to use the default port (55555).
4. Wait for at least two players to join.
5. Start the game and enjoy!

## Server Commands

The server can be controlled using the following commands:

* `START` - starts the game
* `SKIP` - skips the current player (only works if the game has started)
* `QUIT` - exits the server

## Server Protocol

Communication between the client and the server uses the following protocol:

#### Client 🡪 Server

* `MOVE~x~y` - to launch a torpedo, where x and y are the coordinates of the move
* `EXIT` - to exit the game
* `PING` - to check if the server is still alive
* `PONG` - response to a PING from the server

#### Server 🡪 Client

* `HELLO~playerNumber` - where playerNumber is the playerNumber of the player
* `HIT~x~y~playerNumber` - where x and y are the coordinates of the hit, and playerNumber is the playerNumber of the player that is hit
* `MISS~x~y` - where x and y are the coordinates of the miss
* `WINNER~playerNumber` - where playerNumber is the playerNumber of the winner
* `LOST~playerNumber` - where playerNumber is the playerNumber of the player who lost
* `ERROR~message` - where message is the error message
* `EXIT` - to exit the game
* `TURN~playerNumber` - where playerNumber is the playerNumber of the player whose turn it is
* `NEWGAME~x~y` - where x and y are the width and height of the new game
* `POS~x~y` - where x and y are the coordinates of a ship part
* `PING` - to check if a client is still alive
* `PONG` - response to a PING from the client
