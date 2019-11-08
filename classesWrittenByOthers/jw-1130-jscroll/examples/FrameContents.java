import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import javax.swing.text.*;

import java.util.*;

import java.awt.event.*;
import java.awt.*;

public class FrameContents extends JPanel implements ActionListener {

    private JTextPane messageBox;
    private JTextField entryField;

    private Document doc;

    public FrameContents() {
        super(new BorderLayout());

        JPanel topFramePanel=new JPanel(new BorderLayout());
        JPanel bottomFramePanel=new JPanel();

        messageBox = new JTextPane();

        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                                        getStyle(StyleContext.DEFAULT_STYLE);
        Style regular = messageBox.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");
        StyleConstants.setBold(def, true);
        Style s = messageBox.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);
        s = messageBox.addStyle("red", regular);
        StyleConstants.setForeground(s, Color.red);

        doc = messageBox.getStyledDocument();

        entryField = new JTextField(20);
        JButton sendButton = new JButton("Send");

        messageBox.setToolTipText("This is the main message window.");
        entryField.setToolTipText("Enter your message here.");
        sendButton.setToolTipText("Click this button to send your message.");

        messageBox.setEditable(false); // can't edit message box

        sendButton.setMnemonic(KeyEvent.VK_S);

        entryField.addActionListener(this);
        sendButton.addActionListener(this); 

        // place the messageBox in a scrollPane
        JScrollPane messageBoxScroller = new JScrollPane(messageBox,
                              JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                              JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        messageBoxScroller.setPreferredSize(new Dimension(300,200));
        messageBoxScroller.setMinimumSize(new Dimension(100,50));

        topFramePanel.add(messageBoxScroller, BorderLayout.CENTER);

        bottomFramePanel.add(entryField);
        bottomFramePanel.add(sendButton);

        add(topFramePanel, BorderLayout.CENTER);
        add(bottomFramePanel, BorderLayout.SOUTH);

    }

    public void actionPerformed(ActionEvent e) {
      String text = "";
      try {
            text = entryField.getText();

            doc.insertString(doc.getLength(), "Staff> ", 
                              messageBox.getStyle("red"));
            doc.insertString(doc.getLength(), text + "\n", 
                              messageBox.getStyle("italic"));

            entryField.setText(""); // clear the old text
      } catch (NullPointerException np) {}
        catch (BadLocationException bl) {}

    }

}
