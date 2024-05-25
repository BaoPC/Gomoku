package server;


/*
Client connection requests are queued at the port, so the server must accept the connections sequentially. However, the server can service them simultaneously through the use of threads—one thread per each client connection.

The basic flow of logic in such a server is this:

while (true) {
    accept a connection;
    create a thread to deal with the client;
}
The thread reads from and writes to the client connection as necessary.

Original code in this file came from DateServer.java from the class resources page.
 */

//ip = 141.161.88.4

// when you create a new client, send it to all the clients
// add to the list of buttons whenever a new one is created
// you can also have a client list, and each one starts at null
// every time it's updated, replace list in client w list in server

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class GomokuServer implements Runnable {

    //This is the "real" clientlist, it holds the actual socket connection threads
    //each of which hold an instant of client that identifies who its connecting to
    private ServerSocket serverSocket;
    private boolean connection;
    private ArrayList<GomokuGame> games;
    private static LinkedBlockingQueue<ThreadedSocket> playerQueue;
    private int count = 0;

    public int getCount() {return count;}
    public void incrementCount() {count++;}
    public void sendMessage(ThreadedSocket threadedSocket, String message) {
        threadedSocket.sendString(message);
    }

    // threadedsocket creates input and output streams
    // create, add, and start in while loop in start
    // client thread stores name, socket, input, output, unique id, stores and sends message

    public static void main(String[] args) {
        int portNumber = 11341;
        if(args.length > 1) {
            portNumber = Integer.parseInt(args[0]);
        }
        GomokuServer gomokuServer = new GomokuServer(portNumber);
        // for some reason, we can't run this
    }

    public GomokuServer(int port) {
        connection = true;

        try {
            //start server, initialize everything
            games = new ArrayList<GomokuGame>();
            System.out.println("starting server");
            serverSocket = new ServerSocket(port);
            playerQueue = new LinkedBlockingQueue<>();

            //start thread for main server (receive new client connection requests)
            Thread thread = new Thread(this);
            System.out.println("new thread started in main");
            thread.start();

        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("waiting for client");
                Socket newSocket = serverSocket.accept();
                if(newSocket != null) {
                    //start new thread that connects to the client
                    //threadArrayList keeps track of our thread so we can send group messages, etc
                    ThreadedSocket newthread = new ThreadedSocket();
                    newthread.start(this, newSocket);
                    playerQueue.add(newthread);
                    System.out.println("added new client!");
                    newthread.sendString(Protocol.generateChatMessage("server", "WAITING FOR OPPONENT"));
                }

                if ( playerQueue.size() > 1 ) {
                    System.out.println("created game!");
                    Thread.sleep(2000);
                    // pulls players from the queue - this is not what should happen TODO: fix
                    // sets up new players
                    ThreadedSocket newPlayer1 = playerQueue.poll();
                    ThreadedSocket newPlayer2 = playerQueue.poll();
                    //unblock threads inside game constructor
                    GomokuGame game = new GomokuGame(newPlayer1, newPlayer2, this);
                    games.add(game);

                }
            }
            catch(IOException ioe) {
                System.out.println("Server accept error: " + ioe);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

//    public void remove(ThreadedSocket threadedSocket) {
//        // TODO remove player(s) from queue and put next players up
//        playerQueue.remove();
//        //this is where the next players are set up to play the game
//    }

    // this is for ending a single game
    public synchronized void end(GomokuGame game) {
        ThreadedSocket player1 = game.getBlack();
        ThreadedSocket player2 = game.getWhite();

        // kills threads
        player1.close();
        player2.close();

        // set game to null - or delete it
        games.remove(game);
    }

    // for the actual server going offline - sends to all clients
    public void stop() throws IOException {
        //tell all clients the server is shutting down
        for (GomokuGame game : games) {
            game.getBlack().sendString(Protocol.generateChatMessage("SERVER", "Server is now shutting down"));
            game.getWhite().sendString(Protocol.generateChatMessage("SERVER",  "Server is now shutting down"));

            end(game);
            game = null;

        }
    }

}