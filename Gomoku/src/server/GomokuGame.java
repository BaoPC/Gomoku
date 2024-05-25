package server;
import java.awt.Color;
import java.util.ArrayList;

// this class will check win conditions and track the pieces in each location

public class GomokuGame {
    // String array can hold null (blank), black, and white
    private static String[][] gomArr = new String[15][15];
    private ThreadedSocket black;
    private ThreadedSocket white;
    private ArrayList<String> playermoves; //allows us to undo the game if invalid placement i.e. not their turn
    private GomokuServer server;
    private static int turn;

//    private boolean winnerExists;

    // constructor
    GomokuGame(ThreadedSocket p1, ThreadedSocket p2, GomokuServer s) {
        //initialize board to blank
        if(p1 == null || p2 == null) {
            if(p1 == null)
                System.out.println("p1 is null");
            if(p2 == null)
                System.out.println("p2 is null");
        }
        else {
            System.out.println("Starting game");

            server = s;
            black = p1;
            black.setOpponent(p2);
            black.setGame(this);
            white = p2;
            white.setOpponent(black);
            white.setGame(this);
            playermoves = new ArrayList<String>();
            reset();
            black.doneWaiting();
            white.doneWaiting();
            black.sendString(Protocol.generateSetBlackColorMessage());
            white.sendString(Protocol.generateSetWhiteColorMessage());
        }
    }

    // function allows us to update the colors in the gomoku board
    public synchronized void setGomArr(int x, int y, boolean isBlack) {
        System.out.println("We have entered setGomArr");

        if ( isBlack ) {
            gomArr[x][y] = "black";
        } else {
            gomArr[x][y] = "white";
        }
        System.out.println("x:" + x + "y:" + y + gomArr[x][y]);

        turn++;
    }

    public synchronized void checkWin() {
        if(checkWinCondition("black")) {
            black.sendString(Protocol.generateWinMessage());
            white.sendString(Protocol.generateLoseMessage());
            System.out.println("black win");
            server.end(this);
        }
        else if(checkWinCondition("white")) {
            white.sendString(Protocol.generateWinMessage());
            black.sendString(Protocol.generateLoseMessage());
            System.out.println("white win");
            server.end(this);
        }
    }


    // ENTER FUNCTION CHECK WIN CONDITION
    private synchronized boolean checkWinCondition(String color) {
        System.out.println("You have entered checkWinCondition");
        boolean winner = false;

        // check for vertical wins
        for (int x = 0; x < gomArr.length; x++) { // iterate through rows
//            System.out.println("Entered X for loop");
            for (int y = 0; y < gomArr[x].length; y++) { // iterate through columns
//                System.out.println("Entered Y for loop");
                // 5 is the number of pieces in a row - so it will check 4 beyond itself

                if ( gomArr[x][y].equals(color) ) {
//                    System.out.println("You have found a piece that is not blank!");
//                    System.out.println("We will now check the four pieces below it");

                    System.out.println("x:" + x + " y:" + y + " y+1: " + (y+1));
                    System.out.println("gomArr[x][y]: " + gomArr[x][y]);

                    if((y + 4) < gomArr[y].length) {
                        if (gomArr[x][y].equals(gomArr[x][y + 1]) &&
                                gomArr[x][y].equals(gomArr[x][y + 2]) &&
                                gomArr[x][y].equals(gomArr[x][y + 3]) &&
                                gomArr[x][y].equals(gomArr[x][y + 4]) ) {
                            // if the piece and the 4 pieces below it are all either white or black, winner is true
                            winner = true;

                            // TODO: why does the below line not print?
                            System.out.println("You have won!");
                        }
                    }
                    if((x+4) < gomArr[x].length) {
                        if (gomArr[x][y].equals(gomArr[x+1][y]) &&
                                gomArr[x][y].equals(gomArr[x+2][y]) &&
                                gomArr[x][y].equals(gomArr[x+3][y]) &&
                                gomArr[x][y].equals(gomArr[x+4][y]) ) {
                            // if the piece and the 4 pieces below it are all either white or black, winner is true
                            winner = true;

                            System.out.println("You have won!");
                        }
                    }
                    if( ((x + 4) > gomArr[x].length) && ((y + 4) < gomArr[y].length) ) {
                        if (gomArr[x][y].equals(gomArr[x+1][y+1]) &&
                                gomArr[x][y].equals(gomArr[x+2][y+2]) &&
                                gomArr[x][y].equals(gomArr[x+3][y+3]) &&
                                gomArr[x][y].equals(gomArr[x+4][y+4]) ) {
                            // if the piece and the 4 pieces below it are all either white or black, winner is true
                            winner = true;

                            System.out.println("You have won!");
                        }
                    }
                    if( ((x - 4) >= 0) && ((y + 4) < gomArr[y].length) ) {
                        if (gomArr[x][y].equals(gomArr[x-1][y+1]) &&
                                gomArr[x][y].equals(gomArr[x-2][y+2]) &&
                                gomArr[x][y].equals(gomArr[x-3][y+3]) &&
                                gomArr[x][y].equals(gomArr[x-4][y+4]) ) {
                            // if the piece and the 4 pieces below it are all either white or black, winner is true
                            winner = true;

                            System.out.println("You have won!");
                        }
                    }

                }
            }
        }
//        setWinnerExists(winner);
        return winner;
    }

    // TODO: this whole function might not be necessary - it isn't called and it works
    // on intersections
    public synchronized boolean isValidPlacement(String t, int x, int y) {
        boolean canPlace = false;
        //check if x or y are valid
        if(x < 0 || x > 14) {
            canPlace = false;
            System.out.println("x is out of bounds");
        }
        else if( y < 0 || y > 14) {
            canPlace = false;
            System.out.println("y is out of bounds");
        }
        else if(gomArr[x][y].equals("blank")) {
            System.out.println("blank area");
            if(t.equals("black") && turn % 2 == 0) {
                System.out.println("black can play a piece");
                canPlace = true;
            }
            else if(t.equals("white") && turn % 2 == 1) {
                canPlace = true;
                System.out.println("White can play a piece");
            }
            else if(t.equals("black") && turn % 2 == 1) {
                System.out.println("not black turn");
            }
            else if(t.equals("white") && turn % 2 == 0) {
                System.out.println("not the right turn");
            }
            else {
                System.out.println("how did we even get here");
            }
        }
        else {
            System.out.println("not blank");
            canPlace = false;
        }
        return canPlace;
    }

    public synchronized void reset() {
        for(int x = 0; x < 15; x++) { // TODO: set these to width or size thing
            for(int y = 0; y < 15; y++) {
                gomArr[x][y] = "blank";
            }
        }
        turn = 0;
        playermoves.clear();
//        winnerExists = false;

        // TODO: reset the drawing panel
        // this will cause the client to wipe their board and their Gomoku Array
    }

    public synchronized ThreadedSocket getBlack() {
        return black;
    }

    public synchronized ThreadedSocket getWhite() {
        return white;
    }

    //if we get an invalid movement then we can just send a list of strings
    public synchronized void addMove(String s) {
        playermoves.add(s);
    }

    //rebuild the game using playermoves
    public synchronized void rebuild() {
        white.sendString(Protocol.generateResetMessage());
        black.sendString(Protocol.generateResetMessage());

        for(int i = 0; i < playermoves.size(); i++) {
            String s = playermoves.get(i);
            white.sendString(s);
            black.sendString(s);
        }
        //at this point everyone's board should match the server's version of the game.
    }


//    private void setWinnerExists(boolean w) {winnerExists = w;}
//
//    public boolean getWinnerExists() {return winnerExists;}
}
