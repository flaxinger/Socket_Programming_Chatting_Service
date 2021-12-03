package Client;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;

@Getter
@Setter
public class MenuBar{

    private JMenuBar menuBar;
    private JMenu menuFile;
    private JMenu menuHelp;
//    private JMenuItem sendFile;
    private JMenuItem menuItemOpen;
    private JMenuItem menuItemSaveAs;

    public MenuBar(){
        InitComponents();
    }

    public void InitComponents(){
        //Creating the panel at bottom and adding components
        menuBar = new JMenuBar();
        menuFile = new JMenu("FILE");
        menuHelp = new JMenu("Help");
        menuBar.add(menuFile);
        menuBar.add(menuHelp);
        menuItemOpen = new JMenuItem("Open");
        menuItemSaveAs = new JMenuItem("Save as");
        menuFile.add(menuItemOpen);
        menuFile.add(menuItemSaveAs);

    }
}
