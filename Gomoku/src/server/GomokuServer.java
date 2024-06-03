package server;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class GomokuServer implements Runnable {

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


    public static void main(String[] args) {
        int portNumber = 11341;
        if(args.length > 1) {
            portNumber = Integer.parseInt(args[0]);
        }
        GomokuServer gomokuServer = new GomokuServer(portNumber);
    }

    public GomokuServer(int port) {
        connection = true;

        try {
            games = new ArrayList<GomokuGame>();
            System.out.println("starting server");
            serverSocket = new ServerSocket(port);
            playerQueue = new LinkedBlockingQueue<>();

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
                    ThreadedSocket newthread = new ThreadedSocket();
                    newthread.start(this, newSocket);
                    playerQueue.add(newthread);
                    System.out.println("added new client!");
                    newthread.sendString(Protocol.generateChatMessage("server", "WAITING FOR OPPONENT"));
                }

                if ( playerQueue.size() > 1 ) {
                    System.out.println("created game!");
                    Thread.sleep(2000);
                    ThreadedSocket newPlayer1 = playerQueue.poll();
                    ThreadedSocket newPlayer2 = playerQueue.poll();
                    GomokuGame game = new GomokuGame(newPlayer1, newPlayer2, this);
                    games.add(game);

                }
            }
            catch(IOException ioe) {
                System.out.println("Server accept error: " + ioe);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void end(GomokuGame game) {
        ThreadedSocket player1 = game.getBlack();
        ThreadedSocket player2 = game.getWhite();

        player1.close();
        player2.close();

        games.remove(game);
    }

    public void stop() throws IOException {
        for (GomokuGame game : games) {
            game.getBlack().sendString(Protocol.generateChatMessage("SERVER", "Server is now shutting down"));
            game.getWhite().sendString(Protocol.generateChatMessage("SERVER",  "Server is now shutting down"));

            end(game);
            game = null;

        }
    }

}