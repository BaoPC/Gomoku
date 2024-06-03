package server;
import java.io.*;
import java.net.Socket;

public class ThreadedSocket extends Thread {

    private static final String SEPARATOR = "\0";
    private Socket sock = null;
    private String nickname;
    private String color;
    private boolean isActive = false;
    private GomokuServer server = null;
    private boolean threadRuns = true;
    private boolean waiting = true;
    private int clientPort = 0;
    ThreadedSocket opponent = null;
    GomokuGame game = null;

    private BufferedReader fromClient = null;
    private PrintWriter fromServer = null;

    public void setIsActive(boolean b) { isActive = b; }
    public boolean getIsActive() { return isActive; }
    public void setOpponent(ThreadedSocket ts) { opponent = ts; }
    public void setGame(GomokuGame g) {game = g;}
    public void doneWaiting() {waiting = false;}

    public ThreadedSocket() {

    }

    public void start(GomokuServer cserver, Socket newSock) {
        System.out.println("Loading thread");
        sock = newSock;
        server = cserver;
        clientPort = sock.getPort();

        nickname = new String();

        nickname = ("Default Nick Name-" + cserver.getCount());
        try {
            open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread t = new Thread(this);
        t.start();

        cserver.incrementCount();
    }

    public void open() throws IOException{
        try {
            fromClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            fromServer = new PrintWriter(sock.getOutputStream());
            fromServer.flush();
            System.out.println("We have initialized to and from server objects");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error creating input/output stream to client");
        }
    }

    @Override
    public void run() {
        System.out.println("We have entered run");
        while(waiting) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while(threadRuns) {
            try {
                String message[] = null;
                String s;
                System.out.println("waiting to read");
                s = fromClient.readLine();
                System.out.println("Read: >" + s + "<");
                if(opponent == null) {
                    System.out.println("opponent somehow isn't initialized in void run");
                } else {
                    System.out.println("Opponent exists: " + opponent.getNickname());
                }
                if ( s != null ) {
                    if (game != null) {
                        message = s.split(SEPARATOR);
                        if (Protocol.isChangeNameMessage(s)) {
                            System.out.println("changing " + nickname + " to " + message[1]);
                            nickname = message[1];
                            opponent.sendString(s);
                        } else if (Protocol.isChatMessage(s)) {
                            System.out.println("received message: " + s);
                            opponent.sendString(s);
                        } else if (Protocol.isGiveupMessage(s)) {
                            System.out.println(nickname + " has given up");
                            opponent.sendString(s);
                            server.end(game);
                        } else if (Protocol.isResetMessage(s)) {
                            System.out.print(nickname + " has requested a reset");
                            game.reset();
                            opponent.sendString(s);
                        } else if (Protocol.isPlayMessage(s)) {
                            System.out.println("Received play message from" + nickname);
                            int[] pts = Protocol.getPlayDetail(s);
                            int x = pts[1];
                            int y = pts[2];
                            Boolean isBlack = false;
                            color = "white";
                            if (pts[0] == 1) {
                                System.out.println("got a black piece: " + pts[1]);
                                color = "black";
                                isBlack = true;
                            }
                            System.out.println("color : " + color + "and x: " + x + " and y: " + y);

                            if (game.isValidPlacement(color, x, y)) {
                                game.setGomArr(x, y, isBlack);
                                game.addMove(s);
                                System.out.println("checking if win exists");
                                game.checkWin();
                                System.out.println("no one won");
                                opponent.sendString(s);
                            } else {
                                sendString(Protocol.generateChatMessage("Server", "WARNING: " +
                                        "you have attempted an illegal move, the server will now undo the last move"));
                                game.rebuild();
                            }
                        }
                    } else {
                        System.out.println("Error: given message was invalid: " + s);
                        String error = "command >" + s + "< is not a viable client to server message. The server will decide winners, "
                                + "losers, and player colors. ";
                        sendString(Protocol.generateChatMessage("server", error));
                    }
                } else {
                    System.out.println("String or game was null! BOO HISS");
                }
            }catch (IOException e) {
                e.printStackTrace();
                server.end(game);
            } catch (java.lang.NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public synchronized void close() {
        try {
            sendString(Protocol.generateChatMessage("SERVER","goodbye"));
            fromClient.close();
            fromServer.close();
            sock.close();
            threadRuns = false;
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public void setColor(String c) {color = c;}
    public String getColor() {return color;}


    @SuppressWarnings("unchecked")
    public synchronized void sendString(String message) {
        System.out.println("sending message: " + message + " to " + nickname);

        fromServer.println(message);

        fromServer.flush();

        System.out.println("message sent!");
    }
}
