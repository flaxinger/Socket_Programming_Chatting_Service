package Backend;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

@Getter
@Setter
public class Chatroom{

    HashMap<String, Details> map;
    ArrayList<String> userList;
    public Chatroom(){
        map = new HashMap<>();
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
        map.put(id, details);
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

        Details() {
        }

        @Override
        public void run() {
            String message;


            while (true) {
                try {

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

                    message = dis.readUTF();

                    System.out.println(message);

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
                    if(message.equals("USERFORCEEXIT")){
                        sendMessage(id, "user["+id+"] exited forcefully");
                        map.get(id).getDos().close();
                        map.get(id).getDis().close();
                        map.get(id).getSocket().close();
                        map.get(id).setState(2);
                        return;
                    }

                    sendMessage(id, message);

                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        }
        public void sendMessage(String senderId, String message){
            try {
                for (String userId : userList) {
                    if (!map.get(userId).getId().equals(senderId)) {
                        switch (map.get(userId).getState()) {
                            case 1:
                                map.get(userId).getDos().writeUTF(senderId + "###" + message);
                                System.out.println("Sent " + userId + message);
                                break;
                            case 2:
                                map.get(userId).getMessageQueue().add(senderId + "###"+message);
                                System.out.println(userId+" increased to "+map.get(userId).getMessageQueue().size());
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            catch (IOException io){
                io.printStackTrace();
            }
        }
    }
}
