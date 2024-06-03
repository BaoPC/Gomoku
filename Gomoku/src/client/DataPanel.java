package client;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class DataPanel extends JPanel {

    private static final String BLACKTURN = "Black is now playing...";
    private static final String WHITETURN = "White is now playing...";
    private static final String YOURTURN = "You are now playing";
    private static final String YOUWINMESSAGE =  "You have won!";
    private static final String YOULOSEMESSAGE = "You have lost.";
    private static final String OPPRESETMESSAGE = "The opponent has requested a reset";
    private static final String YOURESETMESSAGE = "You have requested a reset";
    private static final String WAITINGFORCONNECTION = "Waiting for an opponent to connect";
    private static final String OPPCONNECTING = "An opponent is connecting...";
    private static final String OPPONENTNAME = "playing against: \n";
    private static final String YOURNAME = "playing as: \n";
    private static final String BLACKCOLOR = " : Black";
    private static final String WHITECOLOR = " : White";
    private static final String OPPGIVEUP = "Your opponent has given up, you win!";
    private static final String YOUGIVEUP = "You have given up.";

    private MessagePanel mainPanel;
    private JTextArea currentUsername;
    private JTextArea opponent;
    private JTextArea dialogue;

    public DataPanel(MessagePanel mp) {
        setLayout(new FlowLayout());
        mainPanel = mp;

        currentUsername = new JTextArea();
        opponent = new JTextArea();
        dialogue = new JTextArea();

        setUpTextArea(currentUsername);
        setUpTextArea(opponent);
        setUpTextArea(dialogue);

        add(currentUsername);
        add(opponent);
        add(dialogue);

        updateDialogueArea(MESSAGETYPE.WAITING);
        updateUsernameArea(MESSAGETYPE.WAITING);
        updateOpponentArea(MESSAGETYPE.WAITING);
    }

    private void setUpTextArea(JTextArea jf) {
        jf.setEditable(false);
        jf.setFont(new Font("Arial", Font.BOLD, 24));
    }

    public void updateDialogueArea(MESSAGETYPE t) {
        switch(t) {
            case YOUWIN:
                dialogue.setText(YOUWINMESSAGE);
                break;
            case YOULOSE:
                dialogue.setText(YOULOSEMESSAGE);
                break;
            case BLACKTURN:
                dialogue.setText(BLACKTURN);
                break;
            case WHITETURN:
                dialogue.setText(WHITETURN);
                break;
            case YOURTURN:
                dialogue.setText(YOURTURN);
                break;
            case YOURESET:
                dialogue.setText(YOURESETMESSAGE);
                break;
            case OPPRESET:
                dialogue.setText(OPPRESETMESSAGE);
                break;
            case WAITING:
                dialogue.setText(WAITINGFORCONNECTION);
                break;
            case OPPCONNECTING:
                dialogue.setText(OPPCONNECTING);
                break;
            case OPPGIVEUP:
                dialogue.setText(OPPGIVEUP);
                break;
            case YOUGIVEUP:
                dialogue.setText(YOUGIVEUP);

        }
    }

    public void updateOpponentArea(MESSAGETYPE c) {
        if(c == MESSAGETYPE.BLACK)
            opponent.setText(OPPONENTNAME + mainPanel.getOpponent() + BLACKCOLOR);
        else if (c == MESSAGETYPE.WHITE)
            opponent.setText(OPPONENTNAME + mainPanel.getOpponent() + WHITECOLOR);
        else
            opponent.setText(OPPONENTNAME + mainPanel.getOpponent());
    }

    public void updateUsernameArea(MESSAGETYPE c) {
        if (mainPanel.getUsername() == null) {
            System.out.println("mainPanel username is null");
        }

        System.out.println("mainPanel.getUsername() " + mainPanel.getUsername());

        if(c == MESSAGETYPE.BLACK) {
            currentUsername.setText(YOURNAME + mainPanel.getUsername() + BLACKCOLOR);
        } else if(c == MESSAGETYPE.WHITE) {
            currentUsername.setText(YOURNAME + mainPanel.getUsername() + WHITECOLOR);
        } else {
            currentUsername.setText(YOURNAME + mainPanel.getUsername());
        }
    }
}
