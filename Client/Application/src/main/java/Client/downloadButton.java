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
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

@Getter
@Setter
public class downloadButton extends JLayeredPane {
    static final int FILEDOWNLOADPORT = 19100;
    int file_idx;
    String fileName;
    private JTextPane textPane;
    private JLabel label;
    private String Sender;
    private DataOutputStream dos;
    private String userId;

    public downloadButton(int idx, String fileName, String Sender, DataOutputStream dos, String id){
        this.file_idx = idx;
        this.fileName = fileName;
        this.Sender = Sender;
        this.dos = dos;
        this.userId = id;
        InitComponents();
    };

    private void InitComponents(){

        label = new JLabel();
        label.setText(Sender);
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(new Color(221, 246,255));
        textPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textPane.setText("Click to Download: "+fileName);
        JButton download = new JButton("Download");


        download.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    dos.writeUTF("DOWNLOADINGFILEFROMCLIENT");
                    dos.writeUTF(String.valueOf(file_idx));
                }
                catch (IOException io){}
                Socket socket = null;
                while(true) {
                    try {
                        Files.createDirectories(Paths.get(System.getProperty("user.dir") + "/fileFlaxingerDownload"));
                        Files.createDirectories(Paths.get(System.getProperty("user.dir") + "/fileFlaxingerDownload/"+userId));
                        socket = new Socket(InetAddress.getByName("localhost"), FILEDOWNLOADPORT);
                        break;
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }

                File file = new File(System.getProperty("user.dir") + "/fileFlaxingerDownload/"+userId+"/"+fileName);
                System.out.println(System.getProperty("user.dir") + "/fileFlaxingerDownload/"+userId+"/"+fileName);
                try {
                    OutputStream fos = new FileOutputStream(file);
                    InputStream fis = socket.getInputStream();
//                    System.out.println(fileDirectory + fileName);
                    byte[] bytes = new byte[8192];
                    int count;
                    while ((count = fis.read(bytes)) > 0) {
                        fos.write(bytes, 0, count);
                    }
                    fos.close();
                    fis.close();
                    socket.close();
                }
                catch (IOException io){
                    System.out.println("Failed to Download file");
                }
                System.out.println("Download is done");
            }
        });

        this.setLayout(new MigLayout("fillx"));
        this.add(label, "wrap, al left");
        this.add(download, "wrap, al left");
        this.add(textPane, "wrap, al left");

    };


}
