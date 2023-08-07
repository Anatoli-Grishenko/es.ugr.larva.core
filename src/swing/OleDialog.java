/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import data.Ole;
import data.Ole.oletype;
import data.OleConfig;
import data.OleFile;
import data.Transform;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import map2D.Map2DColor;
import tools.ExceptionHandler;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class OleDialog extends JDialog implements ActionListener {

    OleConfig output, input;
    HashMap<String, Component> components;
    JTabbedPane tpMain;
    JPanel flMain, flButtons;
    JScrollPane scrollPane;
    JButton bOK, bCancel, baux;
    int spacing = 5, fieldheight = 20, fieldwidth = 160;
    boolean bresult;
//    JFrame parent;
    BiConsumer<ActionEvent, OleConfig> buttonListener;
    BiConsumer<Object, ActionEvent> objectListener;
    int listsize = 5;
    boolean edit = true;
    Class c;

    public static String doSelectFile(String currentfolder, String extension) {
        JFileChooser choose;

        String currentpath = Paths.get("").toAbsolutePath().toString(),
                filepath = FileSystems.getDefault().getPath(currentfolder).normalize().toAbsolutePath().toString();
        choose = new JFileChooser();
        choose.setCurrentDirectory(new File(filepath));
        choose.setDialogTitle("Please select a file");
        choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
        choose.addChoosableFileFilter(new FileNameExtensionFilter("Valid files", extension, extension));
        if (choose.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            String absolutefile = choose.getSelectedFile().toURI().getPath().toString(),
                    relative = new File(currentpath).toURI().relativize(new File(absolutefile).toURI()).getPath();
            return relative;
        } else {
            return null;
        }

    }

    public static String doSaveAsFile(String currentfolder) {
        JFileChooser choose;

        String currentpath = Paths.get("").toAbsolutePath().toString(),
                filepath = FileSystems.getDefault().getPath(currentfolder).normalize().toAbsolutePath().toString();
        choose = new JFileChooser();
        choose.setCurrentDirectory(new File(filepath));
        choose.setDialogTitle("Please select a file");
        choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (choose.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            String absolutefile = choose.getSelectedFile().toURI().getPath().toString(),
                    relative = new File(currentpath).toURI().relativize(new File(absolutefile).toURI()).getPath();
            return relative;
        } else {
            return null;
        }

    }

    public static String doSelectFolder(String currentfolder) {
        JFileChooser choose;

        String currentpath = Paths.get("").toAbsolutePath().toString(),
                filepath = FileSystems.getDefault().getPath(currentfolder).normalize().toAbsolutePath().toString();
        choose = new JFileChooser();
        choose.setCurrentDirectory(new File(currentpath));
        choose.setDialogTitle("Please select a folder");
        choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (choose.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            String absolutefile = choose.getSelectedFile().toURI().getPath().toString(),
                    relative = new File(currentpath).toURI().relativize(new File(absolutefile).toURI()).getPath();
            return relative;
        } else {
            return null;
        }

    }

//    public OleDialog(JFrame parent, String title) {
//        super(parent, title, true);
//        
//        tpMain = new JTabbedPane();
//        tpMain.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        flMain = new JPanel();
//        flMain.setLayout(new BoxLayout(flMain, BoxLayout.Y_AXIS));
//        flButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
//        flMain.add(tpMain);
//        bOK = new JButton("OK");
//        bOK.addActionListener(e -> actionPerformed(e));
//        bCancel = new JButton("Cancel");
//        bCancel.addActionListener(e -> actionPerformed(e));
//        flButtons.add(bOK);
//        flButtons.add(bCancel);
//        flMain.add(flButtons);
//        this.getRootPane().setDefaultButton(bOK);
//
////        setBounds(100, 100, 450, 300);
//        components = new HashMap();
//        getContentPane().setLayout(new BorderLayout(0, 0));
////        getContentPane().add(scrollPane, BorderLayout.CENTER);
//        setModalityType(ModalityType.MODELESS);
//        scrollPane = new JScrollPane();
//        scrollPane.add(getContentPane());
//        setContentPane(scrollPane);
//    } 
//    
    public OleDialog addActionListener(BiConsumer<ActionEvent, OleConfig> l) {
        buttonListener = l;
        return this;
    }

    public OleDialog addObjectListener(BiConsumer<Object, ActionEvent> l) {
        objectListener = l;
        return this;
    }
//    public OleDialog(JFrame parent, String title) {
//        super(parent, title, true);
//        
//        tpMain = new JTabbedPane();
//        tpMain.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        flMain = new JPanel();
//        flMain.setLayout(new BoxLayout(flMain, BoxLayout.Y_AXIS));
//        flButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
//        flMain.add(tpMain);
//        bOK = new JButton("OK");
//        bOK.addActionListener(e -> actionPerformed(e));
//        bCancel = new JButton("Cancel");
//        bCancel.addActionListener(e -> actionPerformed(e));
//        flButtons.add(bOK);
//        flButtons.add(bCancel);
//        flMain.add(flButtons);
//        this.getRootPane().setDefaultButton(bOK);
//        components = new HashMap();
//        scrollPane = new JScrollPane();
//        scrollPane.add(flMain);
//        getContentPane().add(scrollPane);
//    }

    public OleDialog(JFrame parent, String title) {
        super(parent, title, true);

        tpMain = new JTabbedPane();
        tpMain.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        flMain = new JPanel();
        flMain.setLayout(new BoxLayout(flMain, BoxLayout.Y_AXIS));
        flButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        flMain.add(tpMain);
        bOK = new JButton("OK");
        bOK.addActionListener(e -> actionPerformed(e));
        bCancel = new JButton("Cancel");
        bCancel.addActionListener(e -> actionPerformed(e));
        flButtons.add(bOK);
        flButtons.add(bCancel);
        flMain.add(flButtons);
        this.getRootPane().setDefaultButton(bOK);

        components = new HashMap();
        getContentPane().add(flMain);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser choose;
        String sfield;
        JTextField choosevalue;
        switch (e.getActionCommand()) {
            case "OK":
                output = getValues(output);
                bresult = true;
                dispose();
                break;
            case "Cancel":
                bresult = false;
                dispose();
                break;
            default:
                if (e.getActionCommand().startsWith("VIEW")) { //view Instance
                    sfield = e.getActionCommand().replace("VIEW ", "");
                    Ole ofield = new Ole().set(((JTextField) components.get(sfield)).getText());
                    if (ofield.getType().equals(oletype.OLECONFIG.name())) {
                        OleConfig oCfgaux = new OleConfig(ofield);
                        oCfgaux.view((OleApplication) getParent());
                    } else if (ofield.getType().equals(oletype.OLEFILE.name())) {
                        OleFile oFgaux = new OleFile(ofield);
                        oFgaux.saveFile("./");
                        final ProcessBuilder pb = new ProcessBuilder("/usr/bin/xdg-open", oFgaux.getFileName());
                        pb.directory(new File("./"));
                        try {
                            final Process p = pb.start();
                        } catch (IOException ex) {
                            new ExceptionHandler(ex);
                        }
                    }
                } else if (e.getActionCommand().startsWith(".../")) { //select folder
                    sfield = e.getActionCommand().replace(".../", "");
                    if (components.get(sfield) != null) {
                        choosevalue = (JTextField) components.get(sfield);
                        String res = this.doSelectFolder(choosevalue.getText().length() > 0 ? choosevalue.getText() : "./");
                        if (res != null) {
                            choosevalue.setText(res);
                        }
                    }
                } else if (e.getActionCommand().startsWith("...")) { //select file
                    sfield = e.getActionCommand().replace("...", "");
                    if (components.get(sfield) != null) {
                        choosevalue = (JTextField) components.get(sfield);
                        choose = new JFileChooser();
                        String currentpath = Paths.get("").toAbsolutePath().toString(),
                                filepath = FileSystems.getDefault().getPath(choosevalue.getText()).normalize().toAbsolutePath().toString();
//                        String cwd = Paths.get("").toAbsolutePath().toString();
                        choose.setCurrentDirectory(new File(currentpath));
                        choose.setDialogTitle("Please select " + sfield);
                        choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        if (choose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                            String absolutefile = choose.getSelectedFile().getAbsolutePath().toString(),
                                    relative = new File(currentpath).toURI().relativize(new File(absolutefile).toURI()).getPath();
                            choosevalue.setText(relative);
                        }
                    }
                } else if (e.getActionCommand().startsWith("[") && e.getActionCommand().endsWith("]")) {
                    // Any external listener will receive a copy of the object as it is in the
                    // moment of clicking, and, after it returns, the set of values is restored
                    if (buttonListener != null) {
                        input = getValues(input);
                        buttonListener.accept(e, input);
                        setValues(input);
                    }
                } else {
                    Ole oControls = input.getProperties().getOle("control");
                    try {
                        c = Class.forName(oControls.getOle(e.getActionCommand()).getField("class"));
                        if (!oControls.getField(e.getActionCommand()).isEmpty()) {
                            if (objectListener != null) {
                                output = getValues(output);
                                Object o = OleConfig.toObject(output, c);
                                objectListener.accept(o, e);
                                setValues(input);
                            }
                        }
                    } catch (ClassNotFoundException ex) {
                        new ExceptionHandler(ex);
                    }

                }
        }
    }
//                            input = getValues(input);
//                            Class c = Class.forName(oControls.getOle(e.getActionCommand()).getField("class"));
//                            Object obj = OleConfig.toObject(input, c);
//                            BiConsumer<Object, ActionEvent> validator = new BiConsumer<Object, ActionEvent>() {
//                                @Override
//                                public void accept(Object t, ActionEvent ae) {
//                                    switch (ae.getActionCommand()) {
//                                        case "webcam":
//                                            WebCam wcTemp = new WebCam(obj.getWebcam());
//                                            wcTemp.init(obj.getWebcam());
//                                            wcTemp.read();
//                                            Map2DColor maux = new Map2DColor().fromString(wcTemp.getLastRead());
//                                            maux.show();
//                                            break;
//
//                                    }
//                                }
//                            } //                            try {
//                                    //                                Class c = Class.forName(oControls.getOle(e.getActionCommand()).getField("class"));
//                                    //                                Method mval = null;
//                                    //                                for (Method m : c.getDeclaredMethods()) {
//                                    //                                    System.out.println(m.getName());
//                                    //                                    if (m.getName().equals(oControls.getOle(e.getActionCommand()))) {
//                                    //                                        mval = m;
//                                    //                                        break;
//                                    //                                    }
//                                    //                                }
//                                    //                                Object o = OleConfig.toObject(input, c);
//                                    //                                mval.invoke(null, o, e);
//                                    //                            } catch (Exception ex) {
//                                    //                                new ExceptionHandler(ex);
//                                    //                            }
//                            try {
//                                Class c = Class.forName(oControls.getOle(e.getActionCommand()).getField("class"));
//                                Object o = OleConfig.toObject(input, c);
//                                objectListener.accept(o, e);
//                                setValues(input);
//                            } catch (Exception ex) {
//                            }

    public boolean run(OleConfig o) {
        SwingTools.doSwingWait(() -> {
            try {
                run(o, "");
            } catch (Exception ex) {
                new ExceptionHandler(ex);
            }
        }
        );
        return this.bresult;
//        tpMain.removeAll();
//        output = new OleConfig(o);
//        input = new OleConfig(o);
//        setLayout(input);
//        setValues(input);        
//        pack();
//        setVisible(true);
//        return bresult;
    }

    public boolean run(OleConfig o, String defaulttab) {
//        if (o.getDescription() != null && o.getDescription().length() > 0) {
//            this.setTitle(o.getDescription());
//        }
        tpMain.removeAll();
        output = new OleConfig(o);
        input = new OleConfig(o);
        setLayout(input);
        setValues(input);
        if (o.getAllTabNames().contains(defaulttab)) {
            tpMain.setSelectedIndex(o.getAllTabNames().indexOf(defaulttab));
        }
        pack();
        setVisible(true);
        return bresult;
    }

    public OleConfig getResult() {
        return output;
    }

    //////////////////////////// setLayout(), setValues(), getValues()
    protected void setLayout(OleConfig olecfg) {
        ArrayList<String> tabs = new ArrayList(olecfg.getAllTabNames());
        JPanel pTab;
        JLabel lA;
        Ole currentTab;
        Ole fieldproperties;
        String tooltip;

        for (String stab : tabs) {
            currentTab = olecfg.getTab(stab);
            fieldproperties = olecfg.getProperties(stab);
            pTab = new JPanel();
            pTab.setLayout(new BoxLayout(pTab, BoxLayout.LINE_AXIS));
            pTab.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            pTab.add(setLayout(stab, currentTab, olecfg));
            tpMain.addTab(stab, null, pTab);
            tooltip = fieldproperties.getString("tooltip");
            if (tooltip != null) {
                tpMain.setToolTipTextAt(tpMain.getTabCount() - 1, tooltip);
            }
        }
        if (olecfg.getProperties().get("control") != null) {
            Ole oControls = olecfg.getProperties().getOle("control");
            JButton bAux;
            for (String n : oControls.getFieldList()) {
                Ole oleControl = oControls.getOle(n);
                bAux = new JButton(oleControl.getField("label"));
                bAux.setActionCommand(n);
                bAux.addActionListener(this);
                flButtons.add(bAux);
            }
        }
    }

    protected JPanel setLayout(String oid, Ole ocomponents, OleConfig olecfg) {
        GridBagConstraints gc;
        JPanel dataPanel;
        JLabel label;
        JTextField text;
        JCheckBox checkbox;
        JComboBox combobox;
        OleList list;
        Ole fieldproperties, panelproperties;
        String tooltip;
        ArrayList<String> select;
        JButton bFileChoose, bAux;
        String arraySelect[];
        int columns;

        panelproperties = olecfg.getProperties(oid);
        columns = panelproperties.getInt("columns", 1);
        dataPanel = new JPanel();
        dataPanel.setLayout(new GridBagLayout());
        gc = new GridBagConstraints();
        gc.weightx = 1;
        gc.weighty = 1;
        gc.insets = new Insets(spacing, spacing, spacing, spacing);
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 1; // Columns?
        gc.gridheight = 1;
        if (!olecfg.getAllTabNames().contains(oid) && panelproperties.getBoolean("border", true)) {
            dataPanel.setBorder(BorderFactory.createTitledBorder(oid));
        }
        for (String sfield : ocomponents.getFieldList()) {
            fieldproperties = olecfg.getProperties(sfield);
            gc.gridwidth = olecfg.getProperties(sfield).getInt("columns", 1);
            if (ocomponents.getField(sfield).contains("<html>")) {
                gc.gridwidth = GridBagConstraints.REMAINDER;
                label = new JLabel(ocomponents.getField(sfield));
                dataPanel.add(label, gc);
                gc.gridx = columns;
                gc.gridwidth = 1; // Columns?
            } else if (sfield.startsWith("[") && sfield.endsWith("]")) {
                bAux = new JButton(ocomponents.getField(sfield));
                bAux.setActionCommand(sfield);
                bAux.addActionListener(this);
                gc.gridwidth = 1;
                dataPanel.add(bAux, gc);
                gc.gridx++;
            } else if (ocomponents.getFieldType(sfield).equals(oletype.STRING.name())
                    && fieldproperties.getString("type", "").equals("icon")) {
//                    gc.gridx++;
                ImageIcon icon = SwingTools.toIcon(ocomponents.getString(sfield, "").replace("file:", ""), -1, fieldproperties.getInt("height", -1));
                label = new JLabel(icon);
                components.put(sfield, label);
                dataPanel.add(label, gc);
                gc.gridx++;

            } else if (ocomponents.getFieldType(sfield).equals(oletype.INTEGER.name())
                    || ocomponents.getFieldType(sfield).equals(oletype.DOUBLE.name())
                    || ocomponents.getFieldType(sfield).equals(oletype.STRING.name())) {
                label = new JLabel(sfield);
                dataPanel.add(label, gc);
                gc.gridx++;
                if (fieldproperties.getArray("select") != null) { // Combobox
                    select = fieldproperties.getArray("select");
                    arraySelect = new String[]{};
//                    Transform.toArrayString(select);
                    combobox = new JComboBox(arraySelect);
                    combobox.setPreferredSize(new Dimension(fieldwidth, fieldheight));
//                    combobox.setSelectedItem(ocomponents.getField(sfield));
                    tooltip = fieldproperties.getString("tooltip");
                    if (tooltip != null) {
                        combobox.setToolTipText(tooltip);
                    }
                    components.put(sfield, combobox);
                    dataPanel.add(combobox, gc);
                    gc.gridx++;

                    combobox.setEnabled(edit);

                } else if (fieldproperties.get("folder") != null) {     // Select folder               
                    text = new JTextField();
//                    text.setText(ocomponents.getField(sfield));
                    text.setPreferredSize(new Dimension(fieldwidth, fieldheight));
                    text.setEnabled(false);
                    tooltip = fieldproperties.getString("tooltip");
                    if (tooltip != null) {
                        text.setToolTipText(tooltip);
                    }
                    components.put(sfield, text);
                    dataPanel.add(text, gc);
                    gc.gridx++;
                    bFileChoose = new JButton("...");
                    bFileChoose.setActionCommand(".../" + sfield);
                    bFileChoose.addActionListener(this);
                    dataPanel.add(bFileChoose, gc);
                    bFileChoose.setEnabled(edit);
                    gc.gridx++;
                } else if (fieldproperties.get("file") != null) {     // Select file               
                    text = new JTextField();
//                    text.setText(ocomponents.getField(sfield));
                    text.setPreferredSize(new Dimension(fieldwidth, fieldheight));
                    text.setEnabled(false);
                    tooltip = fieldproperties.getString("tooltip");
                    if (tooltip != null) {
                        text.setToolTipText(tooltip);
                    }
                    components.put(sfield, text);
                    dataPanel.add(text, gc);
                    gc.gridx++;
                    bFileChoose = new JButton("...");
                    bFileChoose.setActionCommand("..." + sfield);
                    bFileChoose.addActionListener(this);
                    dataPanel.add(bFileChoose, gc);
                    bFileChoose.setEnabled(edit);
                    gc.gridx++;
                } else if (Ole.isOle(ocomponents.getField(sfield))) {
                    Ole oaux = new Ole().set(ocomponents.getField(sfield));
                    text = new JTextField();
                    text.setPreferredSize(new Dimension(fieldwidth, fieldheight));
                    text.setEnabled(false);
                    tooltip = fieldproperties.getString("tooltip");
                    if (tooltip != null) {
                        text.setToolTipText(tooltip);
                    }
                    components.put(sfield, text);
                    dataPanel.add(text, gc);
                    gc.gridx++;
                    OleButton oButton;
                    if (oaux.getType().equals(oletype.OLECONFIG.name())) {
//                        if (edit) {
//                            oButton = new OleButton((OleApplication) getParent(), "EDIT " + sfield, "settings");
//                        } else {
                        oButton = new OleButton((OleApplication) getParent(), "VIEW " + sfield, "settings");
//                        }
                    } else if (oaux.getType().equals(oletype.OLEFILE.name())) {
                        oButton = new OleButton((OleApplication) getParent(), "VIEW " + sfield, "article");
                    } else {
                        oButton = new OleButton((OleApplication) getParent(), "VIEW " + sfield, "warning");
                    }
                    oButton.setFlat();
                    oButton.setIcon();
                    oButton.setText("");
                    oButton.addActionListener(this);
                    dataPanel.add(oButton, gc);
//                    bFileChoose = new JButton("VIEW");
//                    bFileChoose.setIcon(SwingTools.toIcon("resources/icons/", WIDTH, WIDTH));
//                    bFileChoose.setActionCommand("VIEW " + sfield);
//                    bFileChoose.addActionListener(this);
//                    dataPanel.add(bFileChoose, gc);
//                    bFileChoose.setEnabled(edit);
                    gc.gridx++;
                } else {
                    text = new JTextField();
//                    text.setText(ocomponents.getField(sfield));
                    text.setPreferredSize(new Dimension(fieldwidth, fieldheight));
                    tooltip = fieldproperties.getString("tooltip");
                    if (tooltip != null) {
                        text.setToolTipText(tooltip);
                    }
                    components.put(sfield, text);
                    dataPanel.add(text, gc);
                    gc.gridx++;
                    text.setEnabled(edit);
                }
            } else if (ocomponents.getFieldType(sfield).equals(oletype.BOOLEAN.name())) {
                checkbox = new JCheckBox();
//                checkbox.setSelected(ocomponents.getBoolean(sfield));
                tooltip = fieldproperties.getString("tooltip");
                if (tooltip != null) {
                    checkbox.setToolTipText(tooltip);
                }
                components.put(sfield, checkbox);
                label = new JLabel(sfield);
                dataPanel.add(label, gc);
                gc.gridx++;
                dataPanel.add(checkbox, gc);
                gc.gridx++;
                checkbox.setEnabled(edit);

            } else if (ocomponents.getFieldType(sfield).equals(oletype.ARRAY.name())) { // Lists
                listsize = fieldproperties.getInt("rows", 5);
                OleList.Type listtype;
                try {
                    listtype = OleList.Type.valueOf(fieldproperties.getString("listtype").toUpperCase());
                } catch (Exception ex) {
                    listtype = OleList.Type.STRING;
                }
                list = new OleList().init(new OleConfig(fieldproperties));
                tooltip = fieldproperties.getString("tooltip", "");
                if (tooltip != null) {
                    list.setToolTipText(tooltip);
                }
                components.put(sfield, list);
                list.setEnabled(edit);
                label = new JLabel(sfield);
                dataPanel.add(label, gc);
                gc.gridx++;
                gc.gridheight = listsize;
                dataPanel.add(list.getPane(), gc);
                gc.gridx++;
                gc.gridheight = 1;
                dataPanel.add(list.getAddButton(), gc);
//                gc.gridy++;
                gc.gridx++;
                dataPanel.add(list.getRemoveButton(), gc);
                gc.gridy += listsize;
            } else if (ocomponents.getFieldType(sfield).equals(oletype.OLE.name())) {
                gc.gridx = 0;
                dataPanel.add(setLayout(sfield, ocomponents.getOle(sfield), olecfg), gc);
                gc.gridx = columns;
            }
            if (gc.gridx + 1 >= columns) {
                gc.gridx = 0;
                gc.gridy++;
            }

        }
        return dataPanel;
    }

    protected void setValues(OleConfig olecfg) {
        ArrayList<String> tabs = new ArrayList(olecfg.getAllTabNames());
        JPanel pTab;
        JLabel lA;
        Ole currentTab;
        Ole fieldproperties;
        String tooltip;

        for (String stab : tabs) {
            currentTab = olecfg.getTab(stab);
            setValues(stab, currentTab, olecfg);
        }
    }

    protected void setValues(String oid, Ole ocomponents, OleConfig olecfg) {
        GridBagConstraints gc;
        JPanel dataPanel;
        JLabel label;
        JTextField text;
        JCheckBox checkbox;
        JComboBox combobox;
        OleList list;
        Ole fieldproperties, panelproperties;
        String tooltip;
        ArrayList<String> select;
        JButton bFileChoose, bAux;
        String arraySelect[];
        int columns;

        for (String sfield : ocomponents.getFieldList()) {
            fieldproperties = olecfg.getProperties(sfield);
            if (ocomponents.getField(sfield).contains("<html>")) {
            } else if (sfield.startsWith("[") && sfield.endsWith("]")) {
            } else if (ocomponents.getFieldType(sfield).equals(oletype.STRING.name())
                    && fieldproperties.getString("type", "").equals("icon")) {
            } else if (ocomponents.getFieldType(sfield).equals(oletype.INTEGER.name())
                    || ocomponents.getFieldType(sfield).equals(oletype.DOUBLE.name())
                    || ocomponents.getFieldType(sfield).equals(oletype.STRING.name())) {
                if (fieldproperties.getArray("select") != null) { // Combobox
                    combobox = (JComboBox) components.get(sfield);
                    select = fieldproperties.getArray("select");
                    arraySelect = Transform.toArrayString(select);
                    combobox.removeAllItems();
                    for (String sitem : arraySelect) {
                        combobox.addItem(sitem);
                    }
                    combobox.setSelectedItem(ocomponents.getField(sfield));
                } else if (fieldproperties.get("folder") != null) {     // Select folder               
                    text = (JTextField) components.get(sfield);
                    text.setText(ocomponents.getField(sfield));
                } else if (fieldproperties.get("file") != null) {     // Select file               
                    text = (JTextField) components.get(sfield);
                    text.setText(ocomponents.getField(sfield));
                } else if (Ole.isOle(ocomponents.getField(sfield))) {
                    text = (JTextField) components.get(sfield);
                    text.setText(ocomponents.getField(sfield));
                } else {
                    text = (JTextField) components.get(sfield);
                    text.setText(ocomponents.getField(sfield));
                }
            } else if (ocomponents.getFieldType(sfield).equals(oletype.BOOLEAN.name())) {
                checkbox = (JCheckBox) components.get(sfield);
                checkbox.setSelected(ocomponents.getBoolean(sfield));
            } else if (ocomponents.getFieldType(sfield).equals(oletype.ARRAY.name())) { // Lists
                ArrayList<String> selected;
                if (fieldproperties.getArray("selected") == null) {
                    selected = new ArrayList();
                } else {
                    selected = new ArrayList(fieldproperties.getArray("selected"));
                }
                list = (OleList) components.get(sfield);
                list.clear();
                list.addAllElements(ocomponents.getArray(sfield));
                if (selected != null && selected.size() > 0) {
                    for (String selection : selected) {
                        list.setSelectedValue(selection, bresult);
                    }
                }
//                list.setVisibleRowCount(listsize);
            } else if (ocomponents.getFieldType(sfield).equals(oletype.OLE.name())) {
                setValues(sfield, ocomponents.getOle(sfield), olecfg);
            }
        }
    }

    protected OleConfig getValues(OleConfig olecfg) {
        ArrayList<String> tabs = new ArrayList(olecfg.getAllTabNames());
        Ole currentTab;
        for (String stab : tabs) {
            currentTab = olecfg.getTab(stab);
            olecfg = getValues(currentTab, olecfg);
        }
        return olecfg;
    }

    protected OleConfig getValues(Ole currentTab, OleConfig olecfg) {
        JTextField text;
        JCheckBox checkbox;
        ArrayList<String> select;
        JComboBox combobox;
        OleList list;
        Ole fieldproperties;

        for (String sfield : currentTab.getFieldList()) {
            fieldproperties = olecfg.getProperties(sfield);
            if (currentTab.getField(sfield).contains("<html>")) {
                continue;
            }
            if (sfield.startsWith("[") && sfield.endsWith("]")) {
                continue;
            }
            select = fieldproperties.getArray("select");
            if (currentTab.getFieldType(sfield).equals(oletype.STRING.name())
                    && fieldproperties.getString("type", "").equals("icon")) {
            } else if (currentTab.getFieldType(sfield).equals(oletype.STRING.name())) {
                if (select == null) {
                    text = (JTextField) components.get(sfield);
                    currentTab.setField(sfield, text.getText());
                } else {
                    combobox = (JComboBox) components.get(sfield);
                    currentTab.setField(sfield, (String) combobox.getSelectedItem());
                }
            } else if (currentTab.getFieldType(sfield).equals(oletype.INTEGER.name())) {
                text = (JTextField) components.get(sfield);
                int iv;
                try {
                    iv = Integer.parseInt(text.getText());
                    currentTab.setField(sfield, iv);
                } catch (Exception ex) {
                    new ExceptionHandler(ex);
                }
            } else if (currentTab.getFieldType(sfield).equals(oletype.DOUBLE.name())) {
                text = (JTextField) components.get(sfield);
                double dv;
                try {
                    dv = Double.parseDouble(text.getText());
                    currentTab.setField(sfield, dv);
                } catch (Exception ex) {
                    new ExceptionHandler(ex);
                }
            } else if (currentTab.getFieldType(sfield).equals(oletype.BOOLEAN.name())) {
                checkbox = (JCheckBox) components.get(sfield);
                currentTab.setField(sfield, checkbox.isSelected());
            } else if (currentTab.getFieldType(sfield).equals(oletype.ARRAY.name())) {
                ArrayList<String> sList = new ArrayList();
                OleList mlist = (OleList) components.get(sfield);
                DefaultListModel mlm = mlist.getListMode();
                String slist[] = new String[mlm.size()];
                for (int i = 0; i < mlm.size(); i++) {
                    sList.add((String) mlm.getElementAt(i));
                    slist[i] = (String) mlm.getElementAt(i);
                }
//                currentTab.setField(sfield, new ArrayList<String>(sList));
                currentTab.setField(sfield, slist);
                if (mlist.getSelectedValue() != null) {
                    fieldproperties.setField("selected", (String) mlist.getSelectedValue());
                }
            } else if (currentTab.getFieldType(sfield).equals(oletype.OLE.name())) {
                olecfg = getValues(currentTab.getOle(sfield), olecfg);
            }
        }
        return olecfg;
    }

    //////////////////////////// setLayout(), setValues(), getValues()
//    protected JPanel addToLayout(String oid, Ole ocomponents) {
//        GridBagConstraints gc;
//        JPanel dataPanel;
//        JLabel label;
//        JTextField text;
//        JCheckBox checkbox;
//        JComboBox combobox;
//        OleList list;
//        Ole fieldproperties, panelproperties;
//        String tooltip;
//        ArrayList<String> select;
//        JButton bFileChoose, bAux;
//        String arraySelect[];
//        int columns;
//
//        panelproperties = input.getProperties(oid);
//        columns = panelproperties.getInt("columns", 1);
//        dataPanel = new JPanel();
//        dataPanel.setLayout(new GridBagLayout());
//        gc = new GridBagConstraints();
//        gc.weightx = 1;
//        gc.weighty = 1;
//        gc.insets = new Insets(spacing, spacing, spacing, spacing);
//        gc.anchor = GridBagConstraints.NORTHWEST;
//        gc.fill = GridBagConstraints.HORIZONTAL;
//        gc.gridx = 0;
//        gc.gridy = 0;
//        gc.gridwidth = 1; // Columns?
//        gc.gridheight = 1;
//        if (panelproperties.getBoolean("border", false)) {
//            dataPanel.setBorder(BorderFactory.createTitledBorder(oid));
//        }
//        for (String sfield : ocomponents.getFieldList()) {
//            fieldproperties = input.getProperties(sfield);
//            gc.gridwidth = input.getProperties(sfield).getInt("columns", 1);
//            if (ocomponents.getField(sfield).contains("<html>")) {
//                gc.gridwidth = GridBagConstraints.REMAINDER;
//                label = new JLabel(ocomponents.getField(sfield));
//                dataPanel.add(label, gc);
//                gc.gridx = columns;
//                gc.gridwidth = 1; // Columns?
//            } else if (sfield.startsWith("[") && sfield.endsWith("]")) {
//                bAux = new JButton(ocomponents.getField(sfield));
//                bAux.setActionCommand(sfield);
//                bAux.addActionListener(this);
//                gc.gridwidth = 1;
//                dataPanel.add(bAux, gc);
//                gc.gridx++;
//            } else if (ocomponents.getFieldType(sfield).equals(oletype.INTEGER.name())
//                    || ocomponents.getFieldType(sfield).equals(oletype.DOUBLE.name())
//                    || ocomponents.getFieldType(sfield).equals(oletype.STRING.name())) {
//                label = new JLabel(sfield);
//                dataPanel.add(label, gc);
//                gc.gridx++;
//                if (fieldproperties.getArray("select") != null) { // Combobox
//                    select = fieldproperties.getArray("select");
//                    arraySelect = Transform.toArrayString(select);
//                    combobox = new JComboBox(arraySelect);
//                    combobox.setPreferredSize(new Dimension(fieldwidth, fieldheight));
//                    combobox.setSelectedItem(ocomponents.getField(sfield));
//                    tooltip = fieldproperties.getString("tooltip");
//                    if (tooltip != null) {
//                        combobox.setToolTipText(tooltip);
//                    }
//                    components.put(sfield, combobox);
//                    dataPanel.add(combobox, gc);
//                    gc.gridx++;
//
//                } else if (fieldproperties.get("folder") != null) {     // Select folder               
//                    text = new JTextField();
//                    text.setText(ocomponents.getField(sfield));
//                    text.setPreferredSize(new Dimension(fieldwidth, fieldheight));
//                    text.setEnabled(false);
//                    tooltip = fieldproperties.getString("tooltip");
//                    if (tooltip != null) {
//                        text.setToolTipText(tooltip);
//                    }
//                    components.put(sfield, text);
//                    dataPanel.add(text, gc);
//                    gc.gridx++;
//                    bFileChoose = new JButton("...");
//                    bFileChoose.setActionCommand(".../" + sfield);
//                    bFileChoose.addActionListener(this);
//                    dataPanel.add(bFileChoose, gc);
//                    gc.gridx++;
//                } else if (fieldproperties.get("file") != null) {     // Select file               
//                    text = new JTextField();
//                    text.setText(ocomponents.getField(sfield));
//                    text.setPreferredSize(new Dimension(fieldwidth, fieldheight));
//                    text.setEnabled(false);
//                    tooltip = fieldproperties.getString("tooltip");
//                    if (tooltip != null) {
//                        text.setToolTipText(tooltip);
//                    }
//                    components.put(sfield, text);
//                    dataPanel.add(text, gc);
//                    gc.gridx++;
//                    bFileChoose = new JButton("...");
//                    bFileChoose.setActionCommand("..." + sfield);
//                    bFileChoose.addActionListener(this);
//                    dataPanel.add(bFileChoose, gc);
//                    gc.gridx++;
//                } else {
//                    text = new JTextField();
//                    text.setText(ocomponents.getField(sfield));
//                    text.setPreferredSize(new Dimension(fieldwidth, fieldheight));
//                    tooltip = fieldproperties.getString("tooltip");
//                    if (tooltip != null) {
//                        text.setToolTipText(tooltip);
//                    }
//                    components.put(sfield, text);
//                    dataPanel.add(text, gc);
//                    gc.gridx++;
//                }
//            } else if (ocomponents.getFieldType(sfield).equals(oletype.BOOLEAN.name())) {
//                checkbox = new JCheckBox();
//                checkbox.setSelected(ocomponents.getBoolean(sfield));
//                tooltip = fieldproperties.getString("tooltip");
//                if (tooltip != null) {
//                    checkbox.setToolTipText(tooltip);
//                }
//                components.put(sfield, checkbox);
//                label = new JLabel(sfield);
////                label = new JLabel(sfield, SwingConstants.LEFT);
//                dataPanel.add(label, gc);
//                gc.gridx++;
//                dataPanel.add(checkbox, gc);
//                gc.gridx++;
//            } else if (ocomponents.getFieldType(sfield).equals(oletype.ARRAY.name())) { // Lists
//                OleList.Type listtype;
//                try {
//                    listtype = OleList.Type.valueOf(fieldproperties.getString("listtype").toUpperCase());
//                } catch (Exception ex) {
//                    listtype = OleList.Type.STRING;
//                }
//                list = new OleList().init(listtype);
//                list.addAllElements(ocomponents.getArray(sfield));
//                list.setVisibleRowCount(listsize);
//                tooltip = fieldproperties.getString("tooltip");
//                if (tooltip != null) {
//                    list.setToolTipText(tooltip);
//                }
//                components.put(sfield, list);
//                label = new JLabel(sfield);
//                dataPanel.add(label, gc);
//                gc.gridx++;
//                gc.gridheight = listsize;
//                dataPanel.add(list.getPane(), gc);
//                gc.gridx++;
//                gc.gridheight = 1;
//                dataPanel.add(list.getAddButton(), gc);
//                gc.gridy++;
//                dataPanel.add(list.getRemoveButton(), gc);
//                gc.gridy += listsize;
//            } else if (ocomponents.getFieldType(sfield).equals(oletype.OLE.name())) {
//                gc.gridx = 0;
//                dataPanel.add(addToLayout(sfield, ocomponents.getOle(sfield)), gc);
//                gc.gridx = columns;
//            }
//            if (gc.gridx + 1 >= columns) {
//                gc.gridx = 0;
//                gc.gridy++;
//            }
//        }
//        return dataPanel;
//    }
//
//    protected void Ole2Layout() {
//        ArrayList<String> tabs = new ArrayList(input.getAllTabNames());
//        JPanel pTab;
//        JLabel lA;
//        Ole currentTab;
//        Ole fieldproperties;
//        String tooltip;
//
//        for (String stab : tabs) {
//            currentTab = input.getTab(stab);
//            fieldproperties = input.getProperties(stab);
//            pTab = new JPanel();
//            pTab.setLayout(new BoxLayout(pTab, BoxLayout.LINE_AXIS));
//            pTab.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
//            pTab.add(addToLayout(stab, currentTab));
//            tpMain.addTab(stab, null, pTab);
//            tooltip = fieldproperties.getString("tooltip");
//            if (tooltip != null) {
//                tpMain.setToolTipTextAt(tpMain.getTabCount() - 1, tooltip);
//            }
//        }
//    }
//
//    protected void getFromLayout(Ole currentTab) {
//        JPanel dataPanel;
//        JLabel label;
//        JTextField text;
//        JCheckBox checkbox;
//        ArrayList<String> select;
//        JComboBox combobox;
//        OleList list;
//        Ole fieldproperties;
//
//        for (String sfield : currentTab.getFieldList()) {
//            fieldproperties = input.getProperties(sfield);
//            if (currentTab.getField(sfield).contains("<html>")) {
//                continue;
//            }
//            select = fieldproperties.getArray("select");
//            if (currentTab.getFieldType(sfield).equals(oletype.STRING.name())) {
//                if (select == null) {
//                    text = (JTextField) components.get(sfield);
//                    currentTab.setField(sfield, text.getText());
//                } else {
//                    combobox = (JComboBox) components.get(sfield);
//                    currentTab.setField(sfield, (String) combobox.getSelectedItem());
//                }
//            } else if (currentTab.getFieldType(sfield).equals(oletype.INTEGER.name())) {
//                text = (JTextField) components.get(sfield);
//                int iv;
//                try {
//                    iv = Integer.parseInt(text.getText());
//                    currentTab.setField(sfield, iv);
//                } catch (Exception ex) {
//                }
//            } else if (currentTab.getFieldType(sfield).equals(oletype.DOUBLE.name())) {
//                text = (JTextField) components.get(sfield);
//                double dv;
//                try {
//                    dv = Double.parseDouble(text.getText());
//                    currentTab.setField(sfield, dv);
//                } catch (Exception ex) {
//                }
//            } else if (currentTab.getFieldType(sfield).equals(oletype.BOOLEAN.name())) {
//                checkbox = (JCheckBox) components.get(sfield);
//                currentTab.setField(sfield, checkbox.isSelected());
//            } else if (currentTab.getFieldType(sfield).equals(oletype.ARRAY.name())) {
//                ArrayList<String> sList = new ArrayList();
//                JsonArray jaarray = new JsonArray();
//                OleList mlist = (OleList) components.get(sfield);
//                DefaultListModel mlm = mlist.getListMode();
//                for (int i = 0; i < mlm.size(); i++) {
//                    sList.add((String) mlm.getElementAt(i));
//                }
//                currentTab.setField(sfield, new ArrayList(sList));
//            } else if (currentTab.getFieldType(sfield).equals(oletype.OLE.name())) {
//                getFromLayout(currentTab.getOle(sfield));
//            }
//        }
//    }
//
//    protected void Layout2Ole() {
//        ArrayList<String> tabs = new ArrayList(input.getAllTabNames());
//        Ole currentTab;
//        JTextField jtA;
//        output = input;
//        for (String stab : tabs) {
//            currentTab = input.getTab(stab);
//            getFromLayout(currentTab);
//        }
//    }
//    protected String doSelectFolder(String currentfile) {
//        JFileChooser choose = new JFileChooser();
//        String cwd = Paths.get("").toAbsolutePath().toString() + "/" + (currentfolder.length() > 0 ? currentfolder : "./");
//        choose.setCurrentDirectory(new File(cwd));
//        choose.setDialogTitle("Please select a folder");
//        choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//        if (choose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
//           return choose.getSelectedFile().getName();
//        } else
//            return null;
//
//    }
    public boolean run(OleDialog oDlg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
        for (Component c : components.values()) {
            c.setEnabled(edit);
        }
    }
}
