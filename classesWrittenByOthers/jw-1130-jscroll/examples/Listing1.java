import com.tomtessier.scrollabledesktop.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
  * This class provides a simple example of the scrollable desktop.
  *
  * To compile against scrollabledesktop.jar:
  *
  *  javac -classpath scrollabledesktop.jar Listing1.java
  *
  * To execute against scrollabledesktop.jar:
  *
  *  java -classpath .;scrollabledesktop.jar Listing1
  *
  * Note that if scrollabledesktop.jar is added to the global classpath
  *  via the CLASSPATH environment variable, the -classpath attribute 
  *  is unnecessary
  *
  */

public class Listing1 {

      public Listing1() {

            // prepare the JFrame
            JFrame f = new JFrame("Scrollable Desktop Example");
            f.setSize(300,300);
            f.addWindowListener(new WindowAdapter() {
                  public void windowClosing(WindowEvent e) {
                        System.exit(0);
                  }
            });

            // prepare the menuBar
            JMenuBar menuBar = new JMenuBar();
            f.setJMenuBar(menuBar);

            // create the scrollable desktop instance and add it to the JFrame
            JScrollableDesktopPane scrollableDesktop = 
                  new JScrollableDesktopPane(menuBar);

            // add the scrollable desktop to the JFrame
            f.getContentPane().add(scrollableDesktop);
            f.setVisible(true);
      
            // add internal frames to the scrollable desktop
            for (int i=0; i < 3; i++) {
                  JPanel frameContents = new JPanel();
                  frameContents.add(
                        new JLabel( "Internal frame " + i + 
                                    " of JScrollableDesktopPane"));
                  scrollableDesktop.add(frameContents);
            }

      }

      public static void main(String[] arg) {

            new Listing1();

      }


}
