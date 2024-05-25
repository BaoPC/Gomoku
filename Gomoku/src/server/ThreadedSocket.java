package server;
import java.io.*;
import java.net.Socket;


/*
 * void run() - this function handles receiving all objects from the client.
 * 				It then calls the correct method in the main server (ChattyServer)
 * 				and allows it to handle the obj.
 *
 * void sendMessage() -	This function sends message to the client connected in
 * 						this thread only. It is only called in the ChatterServer
 *
 * The relationship is thus:
 * 		inputstream(fromClient) is from threaded socket to ChatterServer
 * 		outputstream(fromServer) is from ChatterServer to threaded socket
 */

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
    private PrintWriter fromServer = null; //to client

    // getters and setters
    public void setIsActive(boolean b) { isActive = b; }
    public boolean getIsActive() { return isActive; }
    public void setOpponent(ThreadedSocket ts) { opponent = ts; }
    public void setGame(GomokuGame g) {game = g;}
    public void doneWaiting() {waiting = false;}

    public ThreadedSocket() {
        //does nothing, start socket by calling start in server.
    } // end constructor

    public void start(GomokuServer cserver, Socket newSock) {
        System.out.println("Loading thread");
        sock = newSock;
        server = cserver;
        clientPort = sock.getPort(); //return portnumber of socket

        //create client
        nickname = new String();

        nickname = ("Default Nick Name-" + cserver.getCount());
        try {
            open();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Thread t = new Thread(this);
        t.start();

        cserver.incrementCount();
    }

    public void open() throws IOException{
        try {
            //create connections
            fromClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            fromServer = new PrintWriter(sock.getOutputStream());
            fromServer.flush();
//            sendString(Protocol.generateChangeNameMessage(" ", nickname));
            System.out.println("We have initialized to and from server objects");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Error creating input/output stream to client");
        }
    }

    @Override
    public void run() {
        //this handles all the receiving from Client
        //passes the Obj we read to the main server for processing;
        System.out.println("We have entered run");
        //block until we're ready: the game is created
        while(waiting) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        //int i = 0;
        while(threadRuns) {
            //i++;
            //System.out.println("We have entered the while serverRuns loop");
            try {
                String message[] = null;
                String s;
                //System.out.println("We have entered the try loop");
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
                        // first thing the server needs to send is color, your username, your opponent's username
                        if (Protocol.isChangeNameMessage(s)) {
                            System.out.println("changing " + nickname + " to " + message[1]);
                            nickname = message[1];
                            opponent.sendString(s);
                        } else if (Protocol.isChatMessage(s)) {
                            System.out.println("received message: " + s);
                            // this doesn't print right
                            opponent.sendString(s);
                        } else if (Protocol.isGiveupMessage(s)) {
                            System.out.println(nickname + " has given up");
                            opponent.sendString(s); // the other client will set itself as the winner
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
                                game.checkWin(); //if it is a win execution automatically ends here
                                System.out.println("no one won");
                                opponent.sendString(s); //else continue execution here, pass message along
                            } else {
                                sendString(Protocol.generateChatMessage("Server", "WARNING: " +
                                        "you have attempted an illegal move, the server will now undo the last move"));
                                game.rebuild();
                            }
                        } //
                    } else {
                        System.out.println("Error: given message was invalid: " + s);
                        String error = "command >" + s + "< is not a viable client to server message. The server will decide winners, "
                                + "losers, and player colors. ";
                        sendString(Protocol.generateChatMessage("server", error));
                    }
                } else { // string was null or game is null
                    System.out.println("String or game was null! BOO HISS");
                }
            }catch (IOException e) {
                e.printStackTrace();
                //end the game
                server.end(game);
            } catch (java.lang.NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }//end while
    }

    // used by chatter server
    public synchronized  void close() {
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

    //handle sending messages to the client connected in this thread
    //ChatterServer calls this function, we never call it here

    //supress warning when we send ArrayList<Client>
    @SuppressWarnings("unchecked")
    public synchronized void sendString(String message) {
        System.out.println("sending message: " + message + " to " + nickname);

        fromServer.println(message);
//        System.out.println("Opponent is" + opponent.getNickname());
        fromServer.flush();
//        System.out.println("Opponent is" + opponent.getNickname());
        System.out.println("message sent!");
    }
}
