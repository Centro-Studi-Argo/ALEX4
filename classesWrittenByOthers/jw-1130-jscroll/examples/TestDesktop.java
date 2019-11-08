import com.tomtessier.scrollabledesktop.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
  * This class provides a more complex example of the scrollable desktop.
  *
  * To compile against scrollabledesktop.jar:
  *
  *  javac -classpath scrollabledesktop.jar TestDesktop.java
  *
  * To execute against scrollabledesktop.jar:
  *
  *  java -classpath .;scrollabledesktop.jar TestDesktop
  *
  * Note that if scrollabledesktop.jar is added to the global classpath
  *  via the CLASSPATH environment variable, the -classpath attribute 
  *  is unnecessary
  *
  */

public class TestDesktop {

      private JScrollableDesktopPane sdp;
      private JInternalFrame oldFrame;
      private JInternalFrame newFrame;

      private int counter = 1;

      public TestDesktop() {

            // use system look and feel
            try {
                  UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}

            JFrame f = new JFrame("Test Desktop");

            // create the scrollable desktop instance and add it to the JFrame
            sdp = new JScrollableDesktopPane();
            f.getContentPane().add(sdp);

            java.awt.Dimension screenSize = 
                  java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            f.setBounds(50, 50, screenSize.width - 50*2, 
                                screenSize.height-50*2);
            f.addWindowListener(new WindowAdapter() {
                  public void windowClosing(WindowEvent e) {
                        System.exit(0);
                  }
            });

            // prepare the menuBar
            JMenuBar menuBar = createMenuBar();
            f.setJMenuBar(menuBar);

            // register the menu bar with the scrollable desktop
            sdp.registerMenuBar(menuBar);

            // register the default internal frame icon
            sdp.registerDefaultFrameIcon(new ImageIcon("images/frmeicon.gif"));

            f.setVisible(true);

            // example of letting the scrollable desktop build and add the frame
            oldFrame = sdp.add("Frame " + counter++, new FrameContents());

            // example of manually building and adding a frame
            JInternalFrame manuallyBuiltFrame = new BaseInternalFrame("Manually Built");
            manuallyBuiltFrame.getContentPane().add(new FrameContents());
            manuallyBuiltFrame.pack();
            manuallyBuiltFrame.setVisible(true);
            sdp.add(manuallyBuiltFrame);

            // save in newFrame for later adjustment by the menu below
            newFrame = manuallyBuiltFrame;

      }

      public JMenuBar createMenuBar() {

            JMenuBar menuBar = new JMenuBar();

            JMenu menu = new JMenu("File");
            menu.setMnemonic(KeyEvent.VK_F);
            JMenuItem menuItem = new JMenuItem("New frame");
            menuItem.setMnemonic(KeyEvent.VK_N);
            menuItem.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e) {

                        // add a new window, its contents the FrameContents class
                        oldFrame = newFrame;
                        newFrame = 
                              sdp.add("Frame " + counter++, new FrameContents());

                  }
            });

            menu.add(menuItem);

            menuItem = new JMenuItem("Select last frame");
            menuItem.setMnemonic(KeyEvent.VK_S);
            menuItem.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e) {

                        if (oldFrame != null) {
                              sdp.setSelectedFrame(oldFrame);
                        }

                  }
            });

            menu.add(menuItem);


            menuItem = new JMenuItem("Flag contents changed, last frame");
            menuItem.setMnemonic(KeyEvent.VK_F);
            menuItem.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e) {

                        if (oldFrame != null) {
                              sdp.flagContentsChanged(oldFrame);
                        }

                  }
              });

            menu.add(menuItem);


            menuItem = new JMenuItem("Close current frame");
            menuItem.setMnemonic(KeyEvent.VK_C);
            menuItem.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e) {

                        JInternalFrame f = sdp.getSelectedFrame();
                        if (f != null) {
                              sdp.remove(f);
                        }

                  }
            });

            menu.add(menuItem);
            menu.addSeparator();

            menuItem = new JMenuItem("Exit");
            menuItem.setMnemonic(KeyEvent.VK_X);
            menuItem.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e) {
                        System.exit(0);
                  }
              });
            menu.add(menuItem);

            menuBar.add(menu);

            return menuBar;
      }


      public static void main(String[] arg) {

            new TestDesktop();

      }


}
