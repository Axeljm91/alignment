package com.iba.ialign.common;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/*
 * ListDialog.java is meant to be used by programs such as
 * ListDialogRunner.  It requires no additional files.
 */

/**
 * Use this modal dialog to let the user choose one string from a long
 * list.  See ListDialogRunner.java for an example of using ListDialog.
 * The basics:
 * <pre>
 String[] choices = {"A", "long", "array", "of", "strings"};
 String selectedName = ListDialog.showDialog(
 componentInControllingFrame,
 locatorComponent,
 "A description of the list:",
 "Dialog Title",
 choices,
 choices[0]);
 * </pre>
 */
public class StatusDialog extends JDialog
        implements ActionListener {
    private static Logger log=Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

    private static StatusDialog dialog;
    private static String value = "";
    private JList list;
    private JCheckBox[] checkBoxes;
    private boolean isCancelled = false;
    private boolean prepFinished = false;

    /**
     * Set up and show the dialog.  The first Component argument
     * determines which frame the dialog depends on; it should be
     * a component in the dialog's controlling frame. The second
     * Component argument should be null if you want the dialog
     * to come up with its left corner in the center of the screen;
     * otherwise, it should be the component on top of which the
     * dialog should appear.
     */
    public static String showDialog(Component frameComp,
                                    Component locationComp,
                                    String title,
                                    String message) {
        Frame frame = JOptionPane.getFrameForComponent(frameComp);
        dialog = new StatusDialog(frame,
                locationComp,
                title,
                message);
        dialog.setVisible(true);
        return value;
    }

    private void setValue(String newValue) {
        value = newValue;
        list.setSelectedValue(value, true);
    }

    public StatusDialog(Frame frame, Component locationComp, String title, String message) {
        super(frame, title, true);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                isCancelled = true;
            }

            public void windowClosing(WindowEvent e) {
                isCancelled = true;
            }
        });

        //Create and initialize the buttons.
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(this);

        //Create a container so that we can add a title around
        //the scroll pane.  Can't add a title directly to the
        //scroll pane because its background would be white.
        //Lay out the label and scroll pane from top to bottom.
        JPanel listPane = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill      = GridBagConstraints.BOTH;
        c.gridy = 0;
        listPane.add(new JLabel(), c);

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);

        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(listPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);

        //Initialize values.
        pack();
        setLocationRelativeTo(locationComp);
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    //Handle clicks on the Set and Cancel buttons.
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Cancel":
                isCancelled = true;
            default:
                break;
        }
        this.setVisible(false);
    }
}