package client;


// threads have run and close
// client and server have start and stop
// client has start and disconnect
// listening from server - run in the client

// TODO: original window should display public message, not private message

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

//this handles Communication with server and the messaging functions

public class MessagePanel extends JPanel implements ActionListener {

    // variables + getters

    private String username;
    private String opponent;
    private ClientThread clientThread = null;
    private PrintWriter out = null;
    private DataPanel datapanel = null;
    private DrawingPanel drawingPanel = null;
    private Socket sock = null;

    //create all buttons and areas
    JButton sendMessage = new JButton("Send");
    JTextField messageBox = new JTextField(20);
    JTextArea chatHistory = new JTextArea();
//
//    public static void main(String[] args) {
//    	JFrame frame = new JFrame("gomoku");
//    	JPanel panel = new JPanel();
//
//
//    	String[] args1 = {"localhost", "12341"};
//     	try {
//			panel.add(new MessagePanel(args1));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//     	frame.add(panel);
//        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
//        frame.setSize(1000, 700);
//        frame.setLocationRelativeTo( null );
//        frame.pack();
//        frame.setVisible(true);
//    }

    //this function creates all the tabs
    public MessagePanel(String args[], DrawingPanel dp) throws IOException {

        //do this
        //code to draw the message panel here
        setLayout(new BorderLayout());

        JPanel southPanel = new JPanel();
        add(BorderLayout.SOUTH, southPanel);

        username = "you";
        opponent = "them";
        drawingPanel = dp;


        //activate a socket
        startConnection(args[0], Integer.parseInt(args[1]));

        messageBox.setFont(new Font("Arial", Font.PLAIN, 22));
        messageBox.requestFocusInWindow();

        sendMessage.setFont(new Font("Arial", Font.BOLD, 32));
        sendMessage.addActionListener(this);

        southPanel.add(new JScrollPane(messageBox));
        southPanel.add(sendMessage);

        chatHistory.setEditable(false);
        chatHistory.setFont(new Font("Arial", Font.BOLD, 24));
        chatHistory.setLineWrap(true);

        add(new JScrollPane(chatHistory), BorderLayout.CENTER);
        //start the receive socket now that everything is set up
        clientThread.doneWaiting();
    }

    //this function initializes the socket to send and receive messages
    private void startConnection(String hostname, int portnumber) {
        try {
            sock = new Socket(hostname, portnumber);
            out = new PrintWriter(sock.getOutputStream(), true);
            out.flush();


            if(clientThread == null) {
                clientThread = new ClientThread(sock, this);
                clientThread.start();
            }

            int uniqueNum = (int)(Math.random() * 100);
            username = username + uniqueNum;
            System.out.println( "new username: " + username);
            sendMessage(Protocol.generateChangeNameMessage("asd", username));

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // this function displays the received messages
    public void displayMessage(String message) {

        String[] messageDetails = Protocol.getChatDetail(message);
        chatHistory.append("<" + messageDetails[0] + ">: " +
                messageDetails[1] + "\n");
    }

    public void sendMessage(String message) {
        System.out.println("1) Sending message: >" + message + "<");
        out.println(message);
        System.out.println("2) message sent");
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        JButton button = (JButton) e.getSource();

        if (button.getText() == "Send") {

            String contents = messageBox.getText();
            String parsedMessage = Protocol.generateChatMessage(username, contents);
            sendMessage(parsedMessage);
            displayMessage(parsedMessage);
            messageBox.setText("");

        }//end if send
    }

    public void reset() {
        drawingPanel.reset();
    }


    public void stop() {
        sendMessage(Protocol.generateGiveupMessage());
        clientThread.close();
        out.close();

        try {
            sock.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        datapanel.updateDialogueArea(MESSAGETYPE.YOUGIVEUP);
        drawingPanel.freeze();

    }

    public String getOpponent() { return opponent; }

    public void setOpponent(String o) {
        opponent = o;
        Color c = drawingPanel.getCurrentColor();
        MESSAGETYPE m;
        if( c.equals(Color.black))
            m = MESSAGETYPE.WHITE;
        else
            m = MESSAGETYPE.BLACK;


        datapanel.updateOpponentArea(m);
        //do datapanel stuff here to redisplay
    }
    public String getUsername() { return username; }

    public void setUsername(String u) {
        username = u;
        //do datapanel stuff here to redisplay
        //send change username message to server
    }

    //do this : move to drawing panel
    public void windowClosed(WindowEvent e) throws IOException {
        //do this
        //need to close socket and other things
        sendMessage(Protocol.generateGiveupMessage());
    }

    public void setDataPanel(DataPanel dp) {
        if(dp != null) {
            datapanel = dp;
            System.out.println("datapanel set");
        }
        else
            System.out.println("received null panel, can't set datapanel");

    }

    public synchronized DataPanel getDataPanel() {
        System.out.println("getting data panel");
        return datapanel;
    }
    public DrawingPanel getDrawingPanel() {return drawingPanel;}

    public void changeName(String text) {
        // TODO Auto-generated method stub
        sendMessage(Protocol.generateChangeNameMessage(username, text));

        username = text;
        Color c = drawingPanel.getCurrentColor();

        if(c.equals(Color.BLACK))
            datapanel.updateUsernameArea(MESSAGETYPE.BLACK);
        else
            datapanel.updateUsernameArea(MESSAGETYPE.WHITE);


    }

} // end ChatterClient