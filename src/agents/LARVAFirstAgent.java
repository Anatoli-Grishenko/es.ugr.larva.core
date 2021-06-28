/**
 * @file LARVABaseAgent.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package agents;

import swing.LARVAFrame;
import disk.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * This is the basic agent in LARVA. It extends a Jade Agent with an API of
 * abstracted services with enhanced functionality or simplified operation
 * regarding the core of Jade. These are the most important services
 * <p>
 * <ul>
 * <li> Simplified access to AMS
 * <li> Simplified access to DF with a pool of service agents and a pool of
 * services provided.
 * <li> Logging capabilitiy both on screen and on disk with deactivable echo on
 * screen. All the notifications are annotated with a timestamp and the own name
 * of the agent in order to identify clearly who, when and what. It also
 * differentiate three levels of information. For more information
 * {@link Logger}
 * <ul>
 * <li> General information
 * <li> Error information
 * <li> MinorException handling
 * </ul>
 * <li> Thanks to the use of OlePassport, it offers automatic operation to load,
 * store and transfer Passports. It supports encryption by the definition of an
 * appropriate instance of Cryptor
 * <li> Support for reading, writing and transmission of any file, of any type
 * and size.
 * <li> Provides a basic behaviour, which has to be acvivated nevertheless, in
 * order to start working without any background on Jade behaviours. This is a
 * repeatable behaviour (Execute()) which acts as the main body of most agents
 * and an associated boolean variable to control the exit and, therefore, the
 * death of the agent.
 * </ul>
 *
 */
public class LARVAFirstAgent extends LARVABaseAgent {

    // JFrame
    protected LARVAFrame myFrame;
    protected JPanel myPane, myMap;
    protected JScrollPane myScrPane;
    protected JTextArea myText;

    protected String title;

    /**
     * Main constructor
     */
    public LARVAFirstAgent() {
        super();
    }

    /**
     * Main JADE setup
     */
    @Override
    public void setup() {
        super.setup();
        this.logger.setEcho(true);
        // create a new frame to store text field and button
        if (this.getArguments() != null && this.getArguments().length > 1) {
            doSwingWait(() -> {
                myText = (JTextArea) this.getArguments()[2];
                myScrPane = (JScrollPane) this.getArguments()[1];
                myFrame = (LARVAFrame) this.getArguments()[0];
            });
            doSwingLater(() -> {
                myFrame.show();
                this.refreshGUI();
            });
        }

    }

    protected boolean isSwing() {
        return this.myFrame != null;
    }

    @Override
    public void takeDown() {
        super.takeDown();
    }

    //
    // Console output
    //
    /**
     * Log an error message. It is sent of Stderr. When the echo is not active,
     * it does not show anything on screen.
     *
     * @param message The error message
     */
    @Override
    protected void Error(String message) {
        logger.logError(message);
        if (isSwing()) {
            myText.append(logger.getLastlog());
            myText.setCaretPosition(Math.max(myText.getText().lastIndexOf("\n"), 0));
            refreshGUI();
            JOptionPane.showMessageDialog(myFrame,
                    logger.getLastlog(), "Agent " + getLocalName(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Log a common message. It is sent of Stdout. When the echo is not active,
     * it does not show anything on screen.
     *
     * @param message The informative message
     */
    @Override
    protected void Info(String message) {
        logger.logMessage(message);
        if (isSwing()) {
            myText.append(logger.getLastlog());
            myText.setCaretPosition(Math.max(myText.getText().lastIndexOf("\n"), 0));
            refreshGUI();
        }
    }

    @Override
    protected boolean Confirm(String message) {
        if (isSwing()) {
            int op = JOptionPane.showConfirmDialog(this.myFrame,
                    message, "Agent " + getLocalName(), JOptionPane.YES_NO_OPTION);

            return op == JOptionPane.YES_OPTION;
        } else {
            return super.Confirm(message);
        }
    }

    @Override
    protected void Alert(String message) {
        if (isSwing()) {
            JOptionPane.showMessageDialog(this.myFrame,
                    message, "Agent " + getLocalName(), JOptionPane.INFORMATION_MESSAGE);
        } else {
            Info(message);
        }
    }

    @Override
    protected String inputLine(String message) {
        if (isSwing()) {
            String res = JOptionPane.showInputDialog(null, message, "Agent " + getLocalName(), JOptionPane.QUESTION_MESSAGE);
            return res;
        } else {
            return super.inputLine(message);
        }
    }

    protected String inputSelect(String message, String [] options, String value) {
        if (isSwing()) {
            String res = (String) JOptionPane.showInputDialog(myFrame, message, "Agent " + getLocalName(), JOptionPane.QUESTION_MESSAGE, null, options,value );
            return res;
        } else {
            return super.inputLine(message);
        }
    }

    protected void refreshGUI() {
        doSwingLater(() -> {
            myFrame.repaint();
        });
    }

    public void doSwingLater(Runnable what) {
        SwingUtilities.invokeLater(() -> {
            what.run();
        });
    }

    public void doSwingWait(Runnable what) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                what.run();
            });
        } catch (Exception ex) {
        }
    }

}
