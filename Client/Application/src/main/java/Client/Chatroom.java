package Client;

import lombok.Getter;
import lombok.Setter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;

@Getter
@Setter
public class Chatroom {
    //Creating the Frame
    private JFrame frame;
    private JScrollPane chatScollList;
    private InteractionBar interactionBar;
    private MenuBar menuBar;
    private JPanel chatList;


    // variables related to socket
    private String message;
    private final flag newMessage = new flag();
    private DataInputStream dis;
    private DataOutputStream dos;
    static final int PORT = 19191;
    Socket socket = null;
    private String userId;

    public Chatroom(){
        initSocket();
        Login login = new Login(frame, dos, dis);
        login.setVisible(true);

        message = "";
        while(!login.isSucceeded()){
        }
        userId = login.getUsername();
        System.out.println(userId);
        login.dispose();
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
                message = interactionBar.getTextField().getText();
                newMessage.newMessage = true;
//                System.out.println("sending: "+message+" and "+newMessage);
//                String[] parts = a.split("--");
//                boolean isSender = (parts[0].compareTo("flaxinger")==0)?true:false;
//                ChatText message = new ChatText(parts[0], parts[1], isSender);
//                if(parts[0].compareTo("flaxinger")==0){
//
//                }
//                else{
//                    chatList.add(message, "wrap,  al left");
//                }
                chatList.add(new ChatText(userId, message, true), "wrap,  al right");
                chatList.repaint();
                chatList.revalidate();
            }
        });

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {

                try {
                    dos.writeUTF("USERFORCEEXIT");
                    dos.close();
                    dis.close();
                    socket.close();
                    System.exit(0);
                }
                catch (IOException io){
                    io.printStackTrace();
                }


            }
        });

        Thread sendMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                while (true) {
                    if(newMessage.newMessage) {
                        try {
                            // write on the output stream
                            dos.writeUTF(message);
                            newMessage.newMessage = false;
//                            System.out.println("message is" + message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        Thread readMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                while (true) {
                    System.out.printf(".");
                    try {
                        String msg = dis.readUTF();
                        StringTokenizer st = new StringTokenizer(msg, "###");
                        String senderId = st.nextToken();
                        msg = st.nextToken();
                        chatList.add(new ChatText(senderId, msg, false), "wrap,  al left");
                        chatList.repaint();
                        chatList.revalidate();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        sendMessage.start();
        readMessage.start();
    }

    public void start(){
        frame.setVisible(true);
    }

    public void initSocket(){
        try {
            socket = new Socket(InetAddress.getByName("localhost"), PORT);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        }
        catch (UnknownHostException uh){
            uh.printStackTrace();
        }
        catch (IOException io){
            io.printStackTrace();
        }
        System.out.println("Socket was made");
    }

    public class flag{
        public volatile boolean newMessage = false;
    }
}
