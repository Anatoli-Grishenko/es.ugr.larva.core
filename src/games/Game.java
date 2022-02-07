/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package games;

import agswing.AGDrawPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import swing.LARVAFrame;
import swing.MyDrawPane;
import swing.MyPlainButton;
import swing.SwingTools;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Game {

    Semaphore smHasEvent;

    public static enum GameEvent {
        NOTHING, UP, DOWN, LEFT, RIGHT, MOUSERCLICK, MOUSELCLICK, SPACE, MOUSEUP, MOUSEDOWN, ESCAPE,
        KEYBOARD, RETURN, TAB, RESTART
    };

    protected String id;
    protected LARVAFrame mainFrame;
    protected JPanel pButton, pMain;
    protected MyDrawPane pScene;
    protected int width, height;
    protected ArrayList<GameEvent> eventQueue;
    protected GameScene scene;
    protected MyPlainButton bZoomIn, bZoomOut, bHide, bRestart;
    protected boolean showControls = false;

    int swing = 0;

    public Game(String name, int width, int height) {
        super();

        //System.out.println("init constructor");
        id = name;
        eventQueue = new ArrayList();
        scene = new GameScene();
        this.width = width;
        this.height = height;
        smHasEvent = new Semaphore(0);
        pScene = new MyDrawPane(g -> this.drawScene(g));
        pButton = new JPanel();
        pMain = new JPanel();
        mainFrame = new LARVAFrame();
        mainFrame.setKeyListener(e -> this.GameKeyListener(e));
        mainFrame.setListener(e -> this.GameListener(e));
        bZoomIn = new MyPlainButton("ZoomIn", "zoom_in.png", mainFrame);
        bZoomOut = new MyPlainButton("ZoomOut", "zoom_out.png", mainFrame);
        bHide = new MyPlainButton("Hide", "moveDown.png", mainFrame);
        bRestart = new MyPlainButton("Restart", "rotateRight.png", mainFrame);
        pButton.setFocusable(true);
        pButton.requestFocusInWindow();
        mainFrame.setFocusable(true);
        mainFrame.requestFocusInWindow();

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        resize();
        pButton.add(bZoomIn);
        pButton.add(bZoomOut);
        pButton.add(bHide);
        pButton.add(bRestart);
        pMain.add(pScene);
        //pMain.add(pButton);
        mainFrame.add(pMain);
        mainFrame.pack();
        mainFrame.validate();
        mainFrame.show();
        //System.out.println("end constructor");

    }

    public void resize() {
        //System.out.println("init resize");
// Define and size panels
        // Drawing
//        this.removeAll();
        pScene.setPreferredSize(new Dimension(width * getScene().getCell()+2, height * getScene().getCell()));
        pScene.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        pScene.setBackground(Color.BLACK);

        // Buttons
//        pButton.removeAll();
        pButton.setPreferredSize(new Dimension(pScene.getPreferredSize().width, getScene().getCell()));
        pButton.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        pButton.setBackground(Color.DARK_GRAY);
        pButton.setLayout(new FlowLayout(FlowLayout.CENTER));
        // Main panel
//        pMain.removeAll();
        pMain.setPreferredSize(new Dimension(pScene.getPreferredSize().width, pScene.getPreferredSize().height + pButton.getPreferredSize().height));
        pMain.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        pMain.setBackground(Color.BLACK);
//        pMain.setLayout(new FlowLayout(FlowLayout.CENTER));
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));

        // Frame
//        mainFrame.removeAll();
        mainFrame.setSize(new Dimension(pMain.getPreferredSize().width, pMain.getPreferredSize().height));
        // Mount panels
//        refresh();
        //System.out.println("end resize");

    }

    public void refresh() {
        //System.out.println("init swing " + swing);
//        this.Alert("Refresh");
//        SwingTools.doSwingWait(() -> {

        //System.out.println("Pre-refresh");
        mainFrame.repaint();
        pScene.repaint();
        //System.out.println("Post-refresh");
//        });
        //System.out.println("end swing " + (swing++));
    }

    public void drawScene(Graphics2D g) {
        //System.out.println("Panel draw " + g.toString());
        if (getScene() != null) {
            getScene().setG(g);
            //System.out.println("Panel: grid");

            for (int x = 0; x <= width; x += 1) {
                getScene().getG().setColor(Color.GRAY);
                getScene().getG().drawLine(x * getScene().getCell(), 0, x * getScene().getCell(), height * getScene().getCell());
            }
            for (int y = 0; y <= height; y += 1) {
                getScene().getG().setColor(Color.GRAY);
                getScene().getG().drawLine(0, y * getScene().getCell(), width * getScene().getCell(), y * getScene().getCell());
            }

            getScene().showScene();
        }

    }

    public int getSpeed() {
        return getScene().getSpeed();
    }

    public void setSpeed(int speed) {
        getScene().setSpeed(speed);
    }

    public int getZoom() {
        return getScene().getZoom();
    }

    public void setZoom(int zoom) {
        getScene().setZoom(zoom);
    }

    public void zoomIn() {
        this.setZoom(getZoom() + 1);
    }

    public void zoomOut() {
        if (getZoom() > 1) {
            this.setZoom(getZoom() - 1);
        }
    }

    public void delay(long milis) {
        try {
            Thread.sleep(milis);
        } catch (Exception ex) {
        }
    }

    public void GameListener(ActionEvent e) {
//        System.out.println("GListener " + e.getActionCommand());
        switch(e.getActionCommand()) {
            case "Hide":
                if (this.showControls) {
                    pMain.remove(pButton);
                    pMain.validate();
                    this.refresh();
                } else {
                    pMain.add(pButton);
                    pMain.validate();
                    this.refresh();
                }
                this.showControls = !this.showControls;
                break;
            case "Restart":
                if (this.Confirm("Restart game?")) {
                    addEvent(GameEvent.RESTART);
                }
                break;
        }
    }

    public void GameKeyListener(KeyEvent e) {
        System.out.println("Game Key " + e.getKeyChar());
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                addEvent(GameEvent.UP);
                break;
            case KeyEvent.VK_DOWN:
                addEvent(GameEvent.DOWN);
                break;
            case KeyEvent.VK_LEFT:
                addEvent(GameEvent.LEFT);
                break;
            case KeyEvent.VK_RIGHT:
                addEvent(GameEvent.RIGHT);
                break;
            case KeyEvent.VK_ESCAPE:
                addEvent(GameEvent.ESCAPE);
                break;
            case KeyEvent.VK_CONTROL:
                if (this.showControls) {
                    pMain.remove(pButton);
                    pMain.validate();
                    this.refresh();
                } else {
                    pMain.add(pButton);
                    pMain.validate();
                    this.refresh();
                }
                this.showControls = !this.showControls;
                break;
            default:
                switch (e.getKeyChar()) {
                    case '\n':
                        addEvent(GameEvent.RETURN);
                        break;
                    case ' ':
                        addEvent(GameEvent.SPACE);
                        break;
                    default:
                        addEvent(GameEvent.KEYBOARD);
                }
        }
    }

    /* synchronized*/ public void addEvent(GameEvent e) {
        System.out.println("queue " + e.name());
        eventQueue.add(e);
        smHasEvent.release(1);

    }

    /* synchronized*/ public boolean hasEvent() {
        long lwait = 100, maxwait = 10000;
        while (0 < maxwait && this.eventQueue.isEmpty()) {
//            //System.out.println(TimeHandler.Now()+"-->"+eventQueue.size());
            try {
                Thread.sleep(lwait);
            } catch (Exception ex) {
            }
            maxwait -= lwait;
        }
//        try{smHasEvent.acquire(1);}catch(Exception Ex){}
        System.out.println("Check event");
        return this.eventQueue.size() > 0;
//return waitEvent();
    }

    /* synchronized*/ public boolean waitEvent() {
//        long lwait=100,maxwait=10000;
//        while(0 < maxwait && this.eventQueue.isEmpty()) {
////            //System.out.println(TimeHandler.Now()+"-->"+eventQueue.size());
//            try {Thread.sleep(lwait);}catch(Exception ex){}
//            maxwait-=lwait;
//        }
        System.out.println("Wait event");
        try {
            smHasEvent.acquire(1);
        } catch (Exception Ex) {
        }
        return this.eventQueue.size() > 0;
    }

    /* synchronized*/ public void waitReturn() {
        boolean exit = false;
        String sevent;

        while (!exit) {
            if (hasEvent()) {
                sevent = nextEvent();
                exit = sevent.equals(GameEvent.RETURN.name());
            }
        }

    }

    /* synchronized*/ public String nextEvent() {
        if (hasEvent()) {
            GameEvent res = eventQueue.get(0);
            eventQueue.remove(0);
            System.out.println("Pop " + res.name());
            return res.name();
        } else {
            return GameEvent.NOTHING.name();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        mainFrame.setTitle(id);
    }

    public void addGameObject(GameObject go) {
        SwingTools.doSwingWait(() -> {
            getScene().addGameObject(go);
            this.refresh();
        });
    }

    public void clearAllGameObjects() {
        SwingTools.doSwingWait(() -> {
            getScene().clear();
            this.refresh();
        });
    }

    public void moveRight(String id, double distance) {
        SwingTools.doSwingWait(() -> {
            GameObject go = getScene().getGameObject(id);
            go.setX(go.getX()+distance);
            this.refresh();
        });
        delay(getScene().getSpeed());
    }

    public void moveLeft(String id, double distance) {
        SwingTools.doSwingWait(() -> {
            GameObject go = getScene().getGameObject(id);
            double lx = go.getX();
            lx -= distance;
            go.setX(lx);
            this.refresh();
        });
        delay(getScene().getSpeed());
    }

    public void moveCellRight(String id) {
        moveRight(id, getScene().getCell());
    }

    public void moveCellLeft(String id) {
        moveLeft(id, getScene().getCell());
    }

    public void moveCellUp(String id) {
        moveUp(id, getScene().getCell());
    }

    public void moveCellDown(String id) {
        moveDown(id, getScene().getCell());
    }

    public void moveUp(String id, double distance) {
        SwingTools.doSwingWait(() -> {
            GameObject go = getScene().getGameObject(id);
            double ly = go.getY();
            ly -= distance;
            go.setY(ly);
            this.refresh();
            delay(getScene().getSpeed());
        });
    }

    public void moveDown(String id, double distance) {
        SwingTools.doSwingWait(() -> {
            GameObject go = getScene().getGameObject(id);
            double ly = go.getY();
            ly += distance;
            go.setY(ly);
            this.refresh();
        });
        delay(getScene().getSpeed());
    }

    public void changeSprite(String id, String sprite) {
        SwingTools.doSwingWait(() -> {
            GameCharacter go = (GameCharacter) getScene().getGameObject(id);
            go.setCurrentFacet(sprite);
            this.refresh();
        });
        delay(getScene().getSpeed());
    }

    public void changeLabel(String id, String newlabel) {
        SwingTools.doSwingWait(() -> {
            GameLabel go = (GameLabel) getScene().getGameObject(id);
            go.setLabel(newlabel);
            this.refresh();
        });
    }
    
    

    public void setCellX(String id, int cellx) {
        getScene().getGameObject(id).setX(cellx);
    }
    public void setCellY(String id, int celly) {
        getScene().getGameObject(id).setY(celly);
    }

    public int getCellX(String id) {
        return (int) (getScene().getGameObject(id).getX() / getScene().getCell());
    }

    public int getCellY(String id) {
        return (int)(getScene().getGameObject(id).getY() / getScene().getCell());
    }

    public GameScene getScene() {
        return scene;
    }

    public void setScene(GameScene scene) {
        this.scene = scene;
    }

    /**
     * This method ask the user for confirmation (yes=true, no = false) in front
     * of a given message
     *
     * @param message The question asked to the user
     * @return true if the user select yes or false if the user selects no
     */
    public boolean Confirm(String message) {
        int op = JOptionPane.showConfirmDialog(null,
                message, getId(), JOptionPane.YES_NO_OPTION);

        return op == JOptionPane.YES_OPTION;
    }

    /**
     * It shows a message to the user and waits until the user confirms it has
     * read it
     *
     * @param message
     */
    public void Alert(String message) {
        JOptionPane.showMessageDialog(null,
                message, getId(), JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * It asks the user to input a String
     *
     * @param message The message shown to the user
     * @return The string typed by the user
     */
    public String inputLine(String message, String value) {
        String res = JOptionPane.showInputDialog(null, message,
                getId(),
                JOptionPane.QUESTION_MESSAGE, null, null, value).toString();
        return res;
    }

    public int inputInt(String message, int value) {
        int res;
        String sres = "";
        try {
            sres = inputLine(message, "" + value);
            res = Integer.parseInt(sres);
            return res;
        } catch (Exception ex) {
            Alert("Error reading number from '" + sres + "'");
            return Integer.MIN_VALUE;
        }
    }

    public double inputDouble(String message, double value) {
        double res;
        String sres = "";
        try {
            sres = inputLine(message, "" + value);
            res = Double.parseDouble(sres);
            return res;
        } catch (Exception ex) {
            Alert("Error reading number from '" + sres + "'");
            return Double.MIN_VALUE;
        }
    }

    /**
     * It asks the user to select an input String only from a set of allowed
     * options
     *
     * @param message The message shown to the user
     * @param options An array of Strings as the set of possible selections
     * @param value The default selection
     * @return The string selected by the user
     */
    public String inputSelect(String message, String[] options, String value) {
        String res = (String) JOptionPane.showInputDialog(null, message, getId(), JOptionPane.QUESTION_MESSAGE, null, options, value);
        return res;
    }

    public void closeGame() {
        mainFrame.closeLARVAFrame();
    }
}
