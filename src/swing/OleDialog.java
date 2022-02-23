/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import com.eclipsesource.json.JsonArray;
import data.Ole;
import data.Ole.oletype;
import data.OleConfig;
import data.OleList;
import data.Transform;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.FileSystems;
import static java.nio.file.FileSystems.getDefault;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import static swing.SwingTools.doSwingLater;
import static swing.SwingTools.doSwingWait;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class OleDialog extends JDialog implements ActionListener {

    protected static String doSelectFile(String currentfolder) {
        JFileChooser choose;

        String currentpath = Paths.get("").toAbsolutePath().toString(),
                filepath = FileSystems.getDefault().getPath(currentfolder).normalize().toAbsolutePath().toString();
        choose = new JFileChooser();
        choose.setCurrentDirectory(new File(currentpath));
        choose.setDialogTitle("Please select a file");
        choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (choose.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            String absolutefile = choose.getSelectedFile().toURI().getPath().toString(),
                    relative = new File(currentpath).toURI().relativize(new File(absolutefile).toURI()).getPath();
            return relative;
        } else {
            return null;
        }

    }

    protected static String doSelectFolder(String currentfolder) {
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

    OleConfig output, input;
    HashMap<String, Component> components;
    JTabbedPane tpMain;
    JPanel flMain, flButtons;
    JButton bOK, bCancel;
    int spacing = 5, fieldheight = 20, fieldwidth = 160;
    boolean bresult;
    JFrame parent;

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
                Layout2Ole();
                bresult = true;
                dispose();
                break;
            case "Cancel":
                input = output;
                bresult = false;
                dispose();
                break;
            default:
                if (e.getActionCommand().startsWith(".../")) { //select folder
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
                }

        }

    }

    public boolean run(OleConfig o) {
        tpMain.removeAll();
        output = o;
        input = o;
        Ole2Layout();
        pack();
        setVisible(true);

        return bresult;
    }

    public OleConfig getResult() {
        return output;
    }

    protected JPanel addToLayout(String oid, Ole ocomponents) {
        GridBagConstraints gc;
        JPanel dataPanel;
        JLabel label;
        JTextField text;
        JCheckBox checkbox;
        JComboBox combobox;
        MyList list;
        Ole fieldproperties, panelproperties;
        String tooltip;
        ArrayList<String> select;
        JButton bFileChoose;
        String arraySelect[];
        int columns;

        panelproperties = input.getProperties(oid);
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
        if (panelproperties.getBoolean("border", false)) {
            dataPanel.setBorder(BorderFactory.createTitledBorder(oid));
        }
        for (String sfield : ocomponents.getFieldList()) {
            fieldproperties = input.getProperties(sfield);
            gc.gridwidth = input.getProperties(sfield).getInt("columns", 1);
            if (ocomponents.getField(sfield).contains("<html>")) {
//                gc.gridx = 0;
                gc.gridwidth = GridBagConstraints.REMAINDER;
                label = new JLabel(ocomponents.getField(sfield));
                dataPanel.add(label, gc);
                gc.gridx = columns;
                gc.gridwidth = 1; // Columns?
            } else if (ocomponents.getFieldType(sfield).equals(oletype.INTEGER.name())
                    || ocomponents.getFieldType(sfield).equals(oletype.DOUBLE.name())
                    || ocomponents.getFieldType(sfield).equals(oletype.STRING.name())) {
                label = new JLabel(sfield);
                dataPanel.add(label, gc);
                gc.gridx++;
                if (fieldproperties.getArray("select") != null) { // Combobox
                    select = fieldproperties.getArray("select");
                    arraySelect = Transform.toArray(select);
                    combobox = new JComboBox(arraySelect);
                    combobox.setPreferredSize(new Dimension(fieldwidth, fieldheight));
                    combobox.setSelectedItem(ocomponents.getField(sfield));
                    tooltip = fieldproperties.getString("tooltip");
                    if (tooltip != null) {
                        combobox.setToolTipText(tooltip);
                    }
                    components.put(sfield, combobox);
                    dataPanel.add(combobox, gc);
                    gc.gridx++;

                } else if (fieldproperties.get("folder") != null) {     // Select folder               
                    text = new JTextField();
                    text.setText(ocomponents.getField(sfield));
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
                    gc.gridx++;
                } else if (fieldproperties.get("file") != null) {     // Select file               
                    text = new JTextField();
                    text.setText(ocomponents.getField(sfield));
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
                    gc.gridx++;
                } else {
                    text = new JTextField();
                    text.setText(ocomponents.getField(sfield));
                    text.setPreferredSize(new Dimension(fieldwidth, fieldheight));
                    tooltip = fieldproperties.getString("tooltip");
                    if (tooltip != null) {
                        text.setToolTipText(tooltip);
                    }
                    components.put(sfield, text);
                    dataPanel.add(text, gc);
                    gc.gridx++;
                }
            } else if (ocomponents.getFieldType(sfield).equals(oletype.BOOLEAN.name())) {
                checkbox = new JCheckBox();
                checkbox.setSelected(ocomponents.getBoolean(sfield));
                tooltip = fieldproperties.getString("tooltip");
                if (tooltip != null) {
                    checkbox.setToolTipText(tooltip);
                }
                components.put(sfield, checkbox);
                label = new JLabel(sfield);
//                label = new JLabel(sfield, SwingConstants.LEFT);
                dataPanel.add(label, gc);
                gc.gridx++;
                dataPanel.add(checkbox, gc);
                gc.gridx++;
            } else if (ocomponents.getFieldType(sfield).equals(oletype.ARRAY.name())) {
                int listsize = 3;
                MyList.Type listtype;
                try {
                    listtype = MyList.Type.valueOf(fieldproperties.getString("listtype").toUpperCase());
                }catch(Exception ex) {                    
                    listtype= MyList.Type.STRING;
                }
                list = new MyList().init(listtype);
                list.addAllElements(ocomponents.getArray(sfield));
                list.setVisibleRowCount(listsize);
                tooltip = fieldproperties.getString("tooltip");
                if (tooltip != null) {
                    list.setToolTipText(tooltip);
                }
                components.put(sfield, list);
                label = new JLabel(sfield);
                dataPanel.add(label, gc);
                gc.gridx++;
                gc.gridheight = listsize;
                dataPanel.add(list.getPane(), gc);
                gc.gridx++;
                gc.gridheight = 1;
                dataPanel.add(list.getAddButton(), gc);
                gc.gridy++;
                dataPanel.add(list.getRemoveButton(), gc);
                gc.gridy += listsize;
            } else if (ocomponents.getFieldType(sfield).equals(oletype.OLE.name())) {
                gc.gridx = 0;
                dataPanel.add(addToLayout(sfield, ocomponents.getOle(sfield)), gc);
                gc.gridx = columns;
            }
            if (gc.gridx + 1 >= columns) {
                gc.gridx = 0;
                gc.gridy++;
            }
        }
        return dataPanel;
    }

    protected void Ole2Layout() {
        ArrayList<String> tabs = new ArrayList(input.getAllTabNames());
        JPanel pTab;
        JLabel lA;
        Ole currentTab;
        Ole fieldproperties;
        String tooltip;

        for (String stab : tabs) {
            currentTab = input.getTab(stab);
            fieldproperties = input.getProperties(stab);
            pTab = new JPanel();
            pTab.setLayout(new BoxLayout(pTab, BoxLayout.LINE_AXIS));
            pTab.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            pTab.add(addToLayout(stab, currentTab));
            tpMain.addTab(stab, null, pTab);
            tooltip = fieldproperties.getString("tooltip");
            if (tooltip != null) {
                tpMain.setToolTipTextAt(tpMain.getTabCount() - 1, tooltip);
            }
        }
    }

    protected void getFromLayout(Ole currentTab) {
        JPanel dataPanel;
        JLabel label;
        JTextField text;
        JCheckBox checkbox;
        ArrayList<String> select;
        JComboBox combobox;
        MyList list;
        Ole fieldproperties;

        for (String sfield : currentTab.getFieldList()) {
            fieldproperties = input.getProperties(sfield);
            if (currentTab.getField(sfield).contains("<html>")) {
                continue;
            }
            select = fieldproperties.getArray("select");
            if (currentTab.getFieldType(sfield).equals(oletype.STRING.name())) {
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
                }
            } else if (currentTab.getFieldType(sfield).equals(oletype.DOUBLE.name())) {
                text = (JTextField) components.get(sfield);
                double dv;
                try {
                    dv = Double.parseDouble(text.getText());
                    currentTab.setField(sfield, dv);
                } catch (Exception ex) {
                }
            } else if (currentTab.getFieldType(sfield).equals(oletype.BOOLEAN.name())) {
                checkbox = (JCheckBox) components.get(sfield);
                currentTab.setField(sfield, checkbox.isSelected());
            } else if (currentTab.getFieldType(sfield).equals(oletype.ARRAY.name())) {
                ArrayList<String> sList=new ArrayList();
                JsonArray jaarray = new JsonArray();
                MyList mlist = (MyList) components.get(sfield);
                DefaultListModel mlm = mlist.getListMode();
                for (int i = 0; i < mlm.size(); i++) {
                    sList.add((String)mlm.getElementAt(i));
                }
                currentTab.setField(sfield, new ArrayList(sList));
            } else if (currentTab.getFieldType(sfield).equals(oletype.OLE.name())) {
                getFromLayout(currentTab.getOle(sfield));
            }
        }
    }

    protected void Layout2Ole() {
        ArrayList<String> tabs = new ArrayList(input.getAllTabNames());
        Ole currentTab;
        JTextField jtA;
        output = input;
        for (String stab : tabs) {
            currentTab = input.getTab(stab);
            getFromLayout(currentTab);
        }
    }

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
}
