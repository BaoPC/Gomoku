package client;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ButtonPanel extends JPanel implements ActionListener{
    JButton reset = null;
    JButton changeName = null;
    JButton quit = null;
    JTextField nameChange = null;
    MessagePanel mPanel = null;



    public ButtonPanel(MessagePanel mp) {
        mPanel = mp;
        reset = new JButton("Reset Game");
        reset.addActionListener(this);
        quit = new JButton("Quit");
        quit.addActionListener(this);
        changeName = new JButton("Change Nickname");
        changeName.addActionListener(this);
        nameChange = new JTextField(10);
        nameChange.setFont(new Font("Arial", Font.PLAIN, 22));
        nameChange.requestFocusInWindow();

        add(reset);
        add(quit);
        add(nameChange);
        add(changeName);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton button = (JButton)e.getSource();
        if(button.getText() == "Reset Game") {
            System.out.println("requested a reset");
            mPanel.getDataPanel().updateDialogueArea(MESSAGETYPE.YOURESET);
            mPanel.sendMessage(Protocol.generateResetMessage());
            mPanel.reset();
        }
        else if(button.getText() == "Change Nickname") {
            System.out.println("changing username");
            mPanel.changeName(nameChange.getText());
            nameChange.setText("");
        }
        else if(button.getText() == "Quit") {
            mPanel.stop();
            System.out.println("She's lost the will to live");

        }

    }
}
