package client;
/*
Client connection requests are queued at the port, so the server must accept the connections sequentially.
However, the server can service them simultaneously through the use of threads—one thread per
each client connection.

Original code in this file came from DateServer.java from the class resources page.
 */



import java.awt.Color;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

//this handles messages received from the Server
/*
 * the reason we separate receiving and sending messages from the server:
 *
 * sending messages is event driven. We only send messages when the gui tells us to send messages
 * so there's no reason to have it running in its own while loop or thread
 *
 * however receiving messages is different. We may constantly be receiving messages from the server
 * so we need a loop constantly running for this
 * However since we need a loop to be constantly running, we need this to be in its own thread
 * otherwise if we had it as part of the main thread, the loop would just keep running and take up all the time
 * nothing else would work
 */

public class ClientThread extends Thread implements Runnable {
    private Socket sock = null;
    private BufferedReader fromServer;
    private MessagePanel mPanel;
    private boolean waiting;

    public ClientThread(Socket newsock, MessagePanel gui) {
        System.out.println("Creating new clientthread to handle received messages");

        if (newsock != null) {
            System.out.println("We have liftoff: setting up clientthread");
            //copy the existing socket in Chatty builder so that we can receive input
            sock = newsock;

            //when we receive a new message from the server, we call chatty builder to display it
            mPanel = gui;

            //open an input stream - sets fromServer
            open();

            //start thread
            Thread t = new Thread(this);
            t.start();
            waiting = new Boolean(true);
        }//end if args match
        else {
            System.out.println("Insufficient socket passed is invalid");
            System.exit(0);
        }
    }

    public void open() {
        //set up input stream
        try {
            fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    @Override
    public void run() {
        //block until everything's set up -> on the order of milliseconds
        while(waiting) {

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        //this main loop continuously receives messages from the Server
        while(true) {
            try {
                System.out.println("waiting for message");
                String msg = fromServer.readLine();
                System.out.println("read message " + msg);
                if(Protocol.isChangeNameMessage(msg)) {
                    String[] names = Protocol.getChangeNameDetail(msg);
                    mPanel.setOpponent(names[1]);
                    System.out.println("renaming opponent to: " + names[1]);
                }
                else if(Protocol.isChatMessage(msg)) {
                    //display message handles the parsing
                    System.out.println("displaying chat message: " + msg);
                    mPanel.displayMessage(msg);
                }
                else if(Protocol.isGiveupMessage(msg)) {
                    mPanel.getDrawingPanel().freeze();
                    System.out.println("opponent gave up");
                    mPanel.getDataPanel().updateDialogueArea(MESSAGETYPE.OPPGIVEUP);
                }
                else if(Protocol.isLoseMessage(msg)) {
                    System.out.println("you lost");
                    mPanel.getDrawingPanel().freeze();
                    mPanel.getDataPanel().updateDialogueArea(MESSAGETYPE.YOULOSE);
                }
                else if(Protocol.isWinMessage(msg)) {
                    System.out.println("you win");
                    mPanel.getDrawingPanel().freeze();
                    mPanel.getDataPanel().updateDialogueArea(MESSAGETYPE.YOUWIN);
                }
                else if(Protocol.isPlayMessage(msg)) {
                    int[] pts = Protocol.getPlayDetail(msg);
                    int gap = mPanel.getDrawingPanel().getDistanceBetweenIntersections();
                    Point pt = new Point((pts[1] * gap) + gap, (pts[2] * gap) + gap);
                    Color color = Color.BLACK;
                    if( pts[0] == 0)
                        color = Color.WHITE;

                    System.out.println("drawing " + color + " at x:" + pt.getX() + " y: " + pt.getY());
                    mPanel.getDrawingPanel().drawGomokuPiece(pt, color);
                }
                else if(Protocol.isResetMessage(msg)) {
                    System.out.println("resetting game");
                    mPanel.getDataPanel().updateDialogueArea(MESSAGETYPE.OPPRESET);
                    mPanel.getDrawingPanel().reset();
                }
                else if(Protocol.isSetBlackColorMessage(msg)) {
                    System.out.println("setting us as black");
                    mPanel.getDrawingPanel().setCurrentColor(Color.BLACK);
                    mPanel.getDrawingPanel().unfreeze();
                    mPanel.getDataPanel().updateUsernameArea(MESSAGETYPE.BLACK);
                    mPanel.getDataPanel().updateOpponentArea(MESSAGETYPE.WHITE);
                    mPanel.getDataPanel().updateDialogueArea(MESSAGETYPE.YOURTURN);
                }
                else if(Protocol.isSetWhiteColorMessage(msg)) {
                    System.out.println("setting us as white");
                    mPanel.getDrawingPanel().setCurrentColor(Color.WHITE);
                    mPanel.getDrawingPanel().freeze();
                    mPanel.getDataPanel().updateOpponentArea(MESSAGETYPE.BLACK);
                    mPanel.getDataPanel().updateUsernameArea(MESSAGETYPE.WHITE);
                    mPanel.getDataPanel().updateDialogueArea(MESSAGETYPE.BLACKTURN);
                }
                else {
                    System.out.println("Could not parse: " + msg);
                }

            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("The server has disconnected us, we are now quitting");
                System.exit(0);
            }
        }

    }//end run

    public void close() {
        // TODO Auto-generated method stub
        if (fromServer != null)
        {
            try {
                fromServer.close();
                //close socket in messagepanel
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void doneWaiting () {
        waiting = false;
    }
}