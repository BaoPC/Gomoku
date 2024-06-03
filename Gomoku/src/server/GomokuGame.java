package server;
import java.awt.Color;
import java.util.ArrayList;

public class GomokuGame {
    private static String[][] gomArr = new String[15][15];
    private ThreadedSocket black;
    private ThreadedSocket white;
    private ArrayList<String> playermoves;
    private GomokuServer server;
    private static int turn;

    GomokuGame(ThreadedSocket p1, ThreadedSocket p2, GomokuServer s) {
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
        }
        else if(checkWinCondition("white")) {
            white.sendString(Protocol.generateWinMessage());
            black.sendString(Protocol.generateLoseMessage());
            System.out.println("white win");
        }
    }

    private synchronized boolean checkWinCondition(String color) {
        System.out.println("You have entered checkWinCondition");
        boolean winner = false;
    
        for (int x = 0; x < gomArr.length; x++) {
            for (int y = 0; y < gomArr[x].length; y++) {
    
                if (gomArr[x][y].equals(color)) {
    
                    System.out.println("x:" + x + " y:" + y + " y+1: " + (y + 1));
                    System.out.println("gomArr[x][y]: " + gomArr[x][y]);
    
                    if ((y + 4) < gomArr[x].length) {
                        if (gomArr[x][y].equals(gomArr[x][y + 1]) &&
                            gomArr[x][y].equals(gomArr[x][y + 2]) &&
                            gomArr[x][y].equals(gomArr[x][y + 3]) &&
                            gomArr[x][y].equals(gomArr[x][y + 4])) {
    
                            winner = true;
                            System.out.println("You have won");
                        }
                    }
    
                    if ((x + 4) < gomArr.length) {
                        if (gomArr[x][y].equals(gomArr[x + 1][y]) &&
                            gomArr[x][y].equals(gomArr[x + 2][y]) &&
                            gomArr[x][y].equals(gomArr[x + 3][y]) &&
                            gomArr[x][y].equals(gomArr[x + 4][y])) {
    
                            winner = true;
                            System.out.println("You have won");
                        }
                    }
    
                    if ((x + 4) < gomArr.length && (y + 4) < gomArr[x].length) {
                        if (gomArr[x][y].equals(gomArr[x + 1][y + 1]) &&
                            gomArr[x][y].equals(gomArr[x + 2][y + 2]) &&
                            gomArr[x][y].equals(gomArr[x + 3][y + 3]) &&
                            gomArr[x][y].equals(gomArr[x + 4][y + 4])) {
    
                            winner = true;
                            System.out.println("You have won");
                        }
                    }
    
                    if ((x - 4) >= 0 && (y + 4) < gomArr[x].length) {
                        if (gomArr[x][y].equals(gomArr[x - 1][y + 1]) &&
                            gomArr[x][y].equals(gomArr[x - 2][y + 2]) &&
                            gomArr[x][y].equals(gomArr[x - 3][y + 3]) &&
                            gomArr[x][y].equals(gomArr[x - 4][y + 4])) {
    
                            winner = true;
                            System.out.println("You have won diagonally (bottom-left to top-right)!");
                        }
                    }
                }
            }
        }
        return winner;
    }
    

    public synchronized boolean isValidPlacement(String t, int x, int y) {
        boolean canPlace = false;
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
        for(int x = 0; x < 15; x++) {
            for(int y = 0; y < 15; y++) {
                gomArr[x][y] = "blank";
            }
        }
        turn = 0;
        playermoves.clear();
    }

    public synchronized ThreadedSocket getBlack() {
        return black;
    }

    public synchronized ThreadedSocket getWhite() {
        return white;
    }

    public synchronized void addMove(String s) {
        playermoves.add(s);
    }

    public synchronized void rebuild() {
        white.sendString(Protocol.generateResetMessage());
        black.sendString(Protocol.generateResetMessage());

        for(int i = 0; i < playermoves.size(); i++) {
            String s = playermoves.get(i);
            white.sendString(s);
            black.sendString(s);
        }
    }

}
