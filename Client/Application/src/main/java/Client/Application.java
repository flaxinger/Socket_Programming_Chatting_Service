package Client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Application {

    public static void main(String args[]) {
        Chatroom chatroom = new Chatroom();
        chatroom.start();
    }
}