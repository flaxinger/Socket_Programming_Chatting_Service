package Client;

import lombok.Getter;
import lombok.Setter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

@Getter
@Setter
public class InteractionBar extends JPanel{

//    private JPanel panel;
    private JLabel label;
    private JTextField textField;
    private JButton sendButton;
    private JButton logoutButton;
    private JButton fileUpload;

    public InteractionBar(){
        InitComponents();
    }

    public void InitComponents(){

        this.setLayout(new MigLayout("fillx"));

//        panel = new JPanel(); // the panel is not visible in output
        label = new JLabel("Enter Text");
        textField = new JTextField(); // accepts upto 10 characters

        sendButton = new JButton("Send");
        logoutButton = new JButton("Logout");
        fileUpload = new JButton("File");

        this.add(label); // Components Added using Flow Layout
        this.add(textField, "w 100%");
        this.add(sendButton);
        this.add(logoutButton);
        this.add(fileUpload);

    }
}
