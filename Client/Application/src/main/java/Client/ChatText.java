package Client;

import lombok.Getter;
import lombok.Setter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

@Getter
@Setter
public class ChatText extends JLayeredPane{

//    private JLayeredPane layeredPane;
    private JTextPane textPane;
    private String sender;
    private String message;
    private JLabel label;
    private Boolean isSender;

    public ChatText(String sender, String message, boolean isSender) {
        this.sender = sender;
        this.message = message;
        this.isSender = isSender;
        InitComponents();
    }

    private void InitComponents(){
//        layeredPane = new JLayeredPane();
        label = new JLabel();
        label.setText(sender);
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(new Color(221, 246,255));
        textPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textPane.setText(message);

        this.setLayout(new MigLayout("fillx"));
        if(isSender) {
            this.add(label, "wrap, al right");
            this.add(textPane, "wrap, al right");
        }
        else {
            this.add(label, "wrap, al left");
            this.add(textPane, "wrap, al left");
        }
    }
}
