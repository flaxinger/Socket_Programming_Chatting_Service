//package Backend;
//
//import lombok.Builder;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.net.Socket;
//import java.security.PrivateKey;
//import java.util.Scanner;
//import java.util.StringTokenizer;
//import java.util.Vector;
//
//@Setter
//@Getter
//@Builder
//public class Details implements Runnable{
//
//    Scanner scanner = new Scanner(System.in);
//    private Vector<String> userList;
//    private String id;
//    private String password;
//    private DataInputStream dis;
//    private DataOutputStream dos;
//    private Socket socket;
//    // state
//    // 0 - Offline
//    // 1 - Online
//    // 2 - Disconnect
//    private int state;
//
//    Details(){}
//
//    @Override
//    public void run(){
//        String message;
//
//        while(true){
//            try{
//                message = dis.readUTF();
//
//                System.out.println(message);
//
////                StringTokenizer st = new StringTokenizer(message);
////                String MsgToSend = st.nextToken();
//
//                for(String userId: userList){
//                    if(userId != id && ){
//
//                    }
//                }
//            }
//            catch (IOException io){
//                io.printStackTrace();
//            }
//        }
//    }
//
//}