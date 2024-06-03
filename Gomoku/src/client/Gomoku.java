package client;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Document;

public class Gomoku extends JFrame {
    private static final String[] DEFAULTARGS = {"localhost", "11341"};
    private static MessagePanel messagePanel = null;

    public static void main(String[] args) {
        DrawingPanel drawingPanel = new DrawingPanel();
        try {
            if(args.length < 2 ) {
                System.out.println("not enough args, defaulting to: " + DEFAULTARGS[0] +
                        " and " + DEFAULTARGS[1]);
                messagePanel = new MessagePanel(DEFAULTARGS, drawingPanel);
            }
            else
                messagePanel = new MessagePanel(args, drawingPanel);
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        DataPanel dp = new DataPanel(messagePanel);

        messagePanel.setDataPanel(dp);
        drawingPanel.setMessagePanel(messagePanel);
        ButtonPanel bp = new ButtonPanel(messagePanel);


        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Gomoku");
        frame.setBackground(Color.WHITE);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        WindowListener exit = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                messagePanel.stop();
                System.exit(0);
            }
        };



        frame.getContentPane().add(drawingPanel);
        frame.add(messagePanel, BorderLayout.EAST);
        frame.add(dp, BorderLayout.NORTH);
        frame.add(bp, BorderLayout.SOUTH);
        frame.addWindowListener(exit);
        frame.setSize(1000, 900);
        frame.setLocationRelativeTo( null );
        frame.pack();
        frame.setVisible(true);

    }
}