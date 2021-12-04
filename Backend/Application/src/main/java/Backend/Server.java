package Backend;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {

    static final int PORT = 19191;
    static final Chatroom CHATROOM = new Chatroom();
    public Server(){}


    public void run(){

        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(PORT);
        }
        catch (IOException io){
            io.printStackTrace();
        }


        while(true){
            try {
                socket = serverSocket.accept();
                System.out.println("New Connection was made");

                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                // Check Duplicate
                boolean done = false;
                String id = "";
                String password = "";
                while(!done){
                    id = dis.readUTF();
                    password = dis.readUTF();
                    System.out.println(id+' '+password);
                    boolean exists = CHATROOM.checkUser(id);
                    System.out.println(done);
                    if(!exists){
                        CHATROOM.newUser(socket, id, password, dis, dos);
                        done = true;
                    }
                    else{
                        if(CHATROOM.checkPassword(id, password)){
                            if(CHATROOM.getMap().get(id).getState() == 2)
                                CHATROOM.getMap().get(id).sendMessage(id, "user [" + id + "] reconnected.");
                            else if(CHATROOM.getMap().get(id).getState() == 0)
                                CHATROOM.getMap().get(id).setMessageQueue(new LinkedList<>());
                            CHATROOM.getMap().get(id).setState(1);
                            CHATROOM.getMap().get(id).setDos(dos);
                            CHATROOM.getMap().get(id).setDis(dis);
                            CHATROOM.getMap().get(id).setSocket(socket);
                            done = true;
                        }
                    }
                    if(done){
                        dos.writeUTF("LOGINCOMPLETE");
                    }
                    else{
                        dos.writeUTF("LOGINFAIL");
                    }

                }
                Thread t = new Thread(CHATROOM.getDetails(id));
                t.start();
                System.out.println("Chatting Initialized");
            }
            catch (IOException io){
                System.out.println("IO error: "+io);
            }
        }
    }
}
