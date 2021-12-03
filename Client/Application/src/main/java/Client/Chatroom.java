package Client;

import lombok.Getter;
import lombok.Setter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Getter
@Setter
public class Chatroom {
    //Creating the Frame
    private JFrame frame;
    private JScrollPane chatScollList;
    private InteractionBar interactionBar;
    private MenuBar menuBar;
    private JPanel chatList;

    public Chatroom(){
        InitComponents();
        AddLogic();
    }

    public void InitComponents(){

        frame = new JFrame("Chat Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);


        //Creating the MenuBar and adding components
        interactionBar = new InteractionBar();
        menuBar = new MenuBar();

        // Text Area at the Center
        chatList = new JPanel();
        chatList.setLayout(new MigLayout("fillx"));
        chatScollList = new JScrollPane(chatList);
//        chatScollList.setLayout(new MigLayout("fillx"));
        chatScollList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);


        //Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.SOUTH, interactionBar);
        frame.getContentPane().add(BorderLayout.NORTH, menuBar.getMenuBar());
        frame.getContentPane().add(BorderLayout.CENTER, chatScollList);

    }

    private void AddLogic(){
        interactionBar.getSendButton().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(interactionBar.getTextField().getText());
                String a = interactionBar.getTextField().getText();
                String[] parts = a.split("--");
                boolean isSender = (parts[0].compareTo("flaxinger")==0)?true:false;
                ChatText message = new ChatText(parts[0], parts[1], isSender);
                if(parts[0].compareTo("flaxinger")==0){
                    chatList.add(message, "wrap,  al right");
                }
                else{
                    chatList.add(message, "wrap,  al left");
                }
                chatList.repaint();
                chatList.revalidate();
            }
        });
    }

    public void start(){
        frame.setVisible(true);
    }

}
