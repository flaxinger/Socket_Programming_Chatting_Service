package Backend;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Getter
@Setter
public class Chatroom{

    static final int FILEPORT = 19199;
    static final int FILEDOWNLOADPORT = 19100;
    public final ConcurrentHashMap<String, Details> map;
    public final ConcurrentHashMap<Integer, String> fileList;
    String fileDirectory;
    int fileKey = 0;
    public final ArrayList<String> userList;
    public Chatroom(){
        map = new ConcurrentHashMap<>();
        fileList = new ConcurrentHashMap<>();
        try {
            Files.createDirectories(Paths.get(System.getProperty("user.dir") + "/fileDirectory"));
        }
        catch(IOException io) {
            io.printStackTrace();
        }
        fileDirectory = System.getProperty("user.dir")+"/fileDirectory/";
        userList = new ArrayList<>();
    }

    public void newUser(Socket socket, String id, String pwd, DataInputStream dis, DataOutputStream dos){

        userList.add(id);
        Details details = new Details();
        details.setSocket(socket);
        details.setId(id);
        details.setPassword(pwd);
        details.setDis(dis);
        details.setDos(dos);
        details.setState(1);
        details.setMessageQueue(new LinkedList<>());
        System.out.println(details.toString());
        this.map.put(id, details);
    }

    public boolean checkUser(String id){
        return map.containsKey(id);
    }

    public boolean checkPassword(String id, String pwd){
        return map.get(id).getPassword().equals(pwd);
    }

    public Details getDetails(String id){
        return map.get(id);
    }


    @Setter
    @Getter
    public class Details implements Runnable {

        Scanner scanner = new Scanner(System.in);
        private String id;
        private String password;
        private DataInputStream dis;
        private DataOutputStream dos;
        private Socket socket;
        private Queue<String> messageQueue;
        // state
        // 0 - Offline
        // 1 - Online
        // 2 - Disconnect
        private int state;

        Details() {}

        @Override
        public void run() {
            String message="";

            while (map.get(id).getState() == 1) {

                if(!map.get(id).getMessageQueue().isEmpty()) {
                    while (!map.get(id).getMessageQueue().isEmpty()) {
                        if (map.get(id).getState() == 1) {
                            System.out.println(id+" queue size is "+map.get(id).getMessageQueue().size());
                            try {
                                map.get(id).getDos().writeUTF(map.get(id).getMessageQueue().poll());
                            } catch (IOException io) {
                                io.printStackTrace();
                            }
                        }
                    }
                }

                try {
                    message = dis.readUTF();
                    System.out.println(message);
                }
                catch(IOException io){
//                    io.printStackTrace();
                    try {
                        System.out.println("client is experiencing issues");
                        sendMessage(id, "user["+id+"] is experiencing network issues");
                        map.get(id).getDos().close();
                        map.get(id).getDis().close();
                        map.get(id).getSocket().close();
                        map.get(id).setState(2);
                    }
                    catch(IOException ioio){
                        ioio.printStackTrace();
                    }
                }


                try{
//                StringTokenizer st = new StringTokenizer(message);
//                String MsgToSend = st.nextToken();
                    if(message.equals("USERISLOGGINGOUT")){
                        sendMessage(id, "user["+id+"] logged out");
                        map.get(id).getDos().close();
                        map.get(id).getDis().close();
                        map.get(id).getSocket().close();
                        map.get(id).setState(0);
                        return;
                    }
                    else if(message.equals("USERFORCEEXIT")){
                        sendMessage(id, "user["+id+"] exited forcefully");
                        map.get(id).getDos().close();
                        map.get(id).getDis().close();
                        map.get(id).getSocket().close();
                        map.get(id).setState(2);
                        return;
                    }
                    else if(message.equals("UPLOADINGFILEFROMCLIENT")){
//                        sendMessage(id, "user["+id+"] exited forcefully");
//                        Socket filesocket = new Socket(InetAddress.getByName("localhost"), FILEPORT);
                        ServerSocket fileServerSocket = new ServerSocket(FILEPORT);
                        Socket fileSocket = fileServerSocket.accept();
                        String fileName = dis.readUTF();
                        System.out.println("Downloading file");
                        fileList.put(fileKey, fileName);
                        fileKey++;

                        File file = new File(fileDirectory + fileName);
                        OutputStream fos = new FileOutputStream(file);
                        InputStream fis = fileSocket.getInputStream();
                        System.out.println(fileDirectory + fileName);
                        byte[] bytes = new byte[8192];
                        int count;
                        while ((count = fis.read(bytes)) > 0) {
                            fos.write(bytes, 0, count);
                        }
                        fos.close();
                        fis.close();
                        fileSocket.close();
                        fileServerSocket.close();

                        SendFileMessage(id, fileName, fileKey-1);
                        System.out.println("Download Done. Key is:" + (fileKey-1));
                    }
                    else if(message.equals("DOWNLOADINGFILEFROMCLIENT")){
//                        sendMessage(id, "user["+id+"] exited forcefully");
//                        Socket filesocket = new Socket(InetAddress.getByName("localhost"), FILEPORT);
                        ServerSocket fileServerSocket = new ServerSocket(FILEDOWNLOADPORT);
                        Socket fileSocket = fileServerSocket.accept();
                        int fileIdx = Integer.valueOf(dis.readUTF());
                        System.out.println("Uploading file");
                        OutputStream fos = fileSocket.getOutputStream();
//                        InputStream fis = fileSocket.getInputStream();
                        InputStream fis = new FileInputStream(fileDirectory+fileList.get(fileIdx));
//                        System.out.println(fileDirectory + fileName);
                        byte[] bytes = new byte[8192];
                        int count;
                        while ((count = fis.read(bytes)) > 0) {
                            fos.write(bytes, 0, count);
                        }
                        fos.close();
                        fis.close();
                        fileSocket.close();
                        fileServerSocket.close();
//                        SendFileMessage(id, fileName, fileKey-1);
//                        System.out.println("Download Done. Key is:" + (fileKey-1));
                        System.out.println("Client Download is Done");
                    }
                    else{
                        sendMessage(id, message);
                    }
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        }
        public void sendMessage(String senderId, String message){
            System.out.println(userList);
            System.out.println(map);
            for(int i = 0;i < userList.size(); i++) {
                System.out.println(map.containsKey(userList.get(i))+" is true " + userList.get(i));
                System.out.println(map.get(userList.get(i)).getState()+" is " + userList.get(i));
                System.out.println(map.get(userList.get(i)).getId()+" is " + userList.get(i));
                System.out.println(map.get(userList.get(i)).getSocket().isClosed()+" is " + userList.get(i));
                System.out.println(map.get(userList.get(i)).getDos().size()+" is " + userList.get(i));
                System.out.println(map.get(userList.get(i)).getDis()+" is " + userList.get(i));

            }

//            for(int i = 0;i < userList.size(); i++) {
//                String userId = userList.get(i);
            map.forEach((k, v) ->{
                System.out.println(k);
                try {
                    if (!k.equals(senderId)) {
                        v.getDos().writeUTF(senderId + "###" + message);
                        System.out.println("Sent " + k + message);
                    }
                }
                catch (IOException io){
                    if(v.getState()!=0) {
                        v.getMessageQueue().add(senderId + "###" + message);
                        System.out.println(k + " increased to " + v.getMessageQueue().size());
//                        io.printStackTrace();
                    }
                }
            });
//            for (String userId : userList) {
//                System.out.println(map.containsKey(userId)+" for "+userId);
//                try {
//                    if (!map.get(userId).getId().equals(senderId)) {
//                        map.get(userId).getDos().writeUTF(senderId + "###" + message);
//                        System.out.println("Sent " + userId + message);
//                    }
//                }
//                catch (IOException io){
//                    if(map.get(userId).getState()!=0) {
//                        map.get(userId).getMessageQueue().add(senderId + "###" + message);
//                        System.out.println(userId + " increased to " + map.get(userId).getMessageQueue().size());
////                        io.printStackTrace();
//                    }
//                }
//            }
        }
        public void SendFileMessage(String senderId, String fileName, int fileId){
            for (String userId : userList) {
                try {
                    if (!map.get(userId).getId().equals(senderId)) {
                        map.get(userId).getDos().writeUTF("SYSTEM###"+String.valueOf(fileId)+"###"+fileName+"###"+senderId);
                        System.out.println("Sent file Download Button");
                    }
                }
                catch (IOException io){
                    if(map.get(userId).getState()!=0) {
                        map.get(userId).getMessageQueue().add("SYSTEM###"+String.valueOf(fileId)+"###"+fileName+"###"+senderId);
                        System.out.println(userId + " increased to " + map.get(userId).getMessageQueue().size());
//                        io.printStackTrace();
                    }
                }
            }
        }
    }
}
