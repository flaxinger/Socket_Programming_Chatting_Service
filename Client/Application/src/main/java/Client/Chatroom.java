package Client;

import lombok.Getter;
import lombok.Setter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
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
    private Login login;

    // variables related to socket
    private String message;
    private final flag newMessage = new flag();
    private DataInputStream dis;
    private DataOutputStream dos;
    static final int PORT = 19191;
    static final int FILEPORT = 19199;
    Socket socket = null;
    private String userId;
    JFileChooser fileChooser;
    Thread sendMessage, readMessage;

    public Chatroom(){

        initSocket();

        startLogin();
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
        fileChooser = new JFileChooser();
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
                chatList.add(new ChatText(userId, message, true), "wrap,  al right");
                chatList.repaint();
                chatList.revalidate();
            }
        });

        interactionBar.getFileUpload().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
                int result = fileChooser.showOpenDialog(interactionBar);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {

                        dos.writeUTF("UPLOADINGFILEFROMCLIENT");
                        dos.writeUTF(selectedFile.getName());
                        Socket fileSocket = null;
                        while(true){
                            try{
                                fileSocket = new Socket(InetAddress.getByName("localhost"), FILEPORT);
                                break;
                            }
                            catch (IOException io){
                            }
                        }
                        FileInputStream fis = new FileInputStream(selectedFile);
                        OutputStream fos = fileSocket.getOutputStream();

                        int count;
                        byte[] bytes = new byte[8192];
                        while ((count = fis.read(bytes)) > 0) {
                            fos.write(bytes, 0, count);
                        }

                        fos.close();
                        fis.close();
                        fileSocket.close();
                        chatList.add(new ChatText(userId, "UPLOADED FILE", true), "wrap,  al right");
                        chatList.repaint();
                        chatList.revalidate();
                        System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                        System.out.println(selectedFile.getName());
                    }
                    catch (IOException io){
                        io.printStackTrace();
                        System.out.println("Connection has a problem. Please try upload again.");
                    }
                }
            }
        });

        interactionBar.getLogoutButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    dos.writeUTF("USERISLOGGINGOUT");
//                    readMessage.stop();
//                    sendMessage.stop();
                    dos.close();
                    dis.close();
                    socket.close();
                    System.exit(0);
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        });


        frame.addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    dos.writeUTF("USERFORCEEXIT");
//                    readMessage.stop();
//                    sendMessage.stop();
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

        sendMessage = new Thread(new Runnable()
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
                            System.out.println("Server has issues: cannot send message");
                            try {
                                Thread.sleep(1000);
                            }
                            catch (InterruptedException ie){}
                        }
                    }
                }
            }
        });

        readMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                while (true) {
                    System.out.printf(".");
                    try {
                        String msg = dis.readUTF();
                        System.out.println(msg);
                        StringTokenizer st = new StringTokenizer(msg, "###");
                        String senderId = st.nextToken();
                        msg = st.nextToken();

                        if(senderId.equals("SYSTEM")){
                            System.out.println("downloadbutton");
                            String filename= st.nextToken();
                            String realSenderId = st.nextToken();
                            chatList.add(new downloadButton(Integer.valueOf(msg), filename, realSenderId, dos, userId), "wrap, al left");
                        }
                        else {
                            chatList.add(new ChatText(senderId, msg, false), "wrap,  al left");
                        }
                        chatList.repaint();
                        chatList.revalidate();
                    } catch (IOException e) {
                        System.out.println("Server has issues");
                        closeSocket();
                        initSocket();
                        try {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException ie){}

                    } catch (NoSuchElementException ne){}

                }
            }
        });


        sendMessage.start();
        readMessage.start();
    }
    public void startLogin(){
        login = new Login(frame, dos, dis);
        login.getBtnLogin().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    dos.writeUTF(login.getUsername());
                    dos.writeUTF(login.getPassword());
                    String result = dis.readUTF();
                    System.out.println(result);
                    if(result.equals("LOGINCOMPLETE")){
                        login.setSucceeded(true);
                        login.dispose();
                    }
                    else{
                        login.getTfUsername().setText("");
                        login.getPfPassword().setText("");
                        login.setSucceeded(false);
                    }
                }
                catch (IOException io){
                    System.out.println("reconnecting to server");
                    closeSocket();
                    initSocket();
                }
            }
        });
        login.setVisible(true);


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
            System.out.println("Host is unknown");
        }
        catch (IOException io){
            System.out.println("Host IO has exceptions");
        }
        System.out.println("Socket was made");
    }
    public void closeSocket(){
        try {
            dis.close();
            dos.close();
            socket.close();
        }
        catch (IOException io){
            System.out.println("cannot close connection");
        }
    }
    public class flag{
        public volatile boolean newMessage = false;
    }
}
