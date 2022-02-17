/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import data.Ole;
import data.Ole.oletype;
import data.OleConfig;
import data.OleList;
import data.OleRecord;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import static swing.SwingTools.doSwingLater;
import static swing.SwingTools.doSwingWait;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class OleDialog {

    OleConfig output, input;
    LARVAFrame myFrame;
    HashMap<String, Component> components;
    JTabbedPane tpMain;
    JPanel flMain, flButtons;
    MyPlainButton bOK, bCancel;
    int spacing = 5, fieldheight = 20, fieldwidth = 160;
    Semaphore waitDialog;
    boolean bresult;

    public OleDialog(LARVAFrame parent, OleConfig o) {
        myFrame = new LARVAFrame(e -> OleDialogListener(e));
        tpMain = new JTabbedPane();
        tpMain.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        flMain = new JPanel();
        flMain.setLayout(new BoxLayout(flMain, BoxLayout.Y_AXIS));
        flButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        flMain.add(tpMain);
        bOK = new MyPlainButton("OK", "", myFrame);
        bCancel = new MyPlainButton("Cancel", "", myFrame);
        flButtons.add(bOK);
        flButtons.add(bCancel);
        flMain.add(flButtons);
        output = o;
        input = o;
        components = new HashMap();
        myFrame.add(flMain);
        myFrame.pack();
        myFrame.show();
        myFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
       Ole2Layout();
        this.waitDialog = new Semaphore(0);
    }

    public boolean Interact() {
        doSwingWait(() -> {
        });
        try {
            this.waitDialog.acquire(1);
        } catch (Exception Ex) {

        }
        return bresult;
    }

    public OleConfig getResult() {
        return output;
    }

    protected void OleDialogListener(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "OK":
                Layout2Ole();
                myFrame.closeLARVAFrame();
                this.waitDialog.release(1);
                bresult = true;
                break;
            case "Cancel":
                input = output;
                myFrame.closeLARVAFrame();
                this.waitDialog.release(1);
                bresult = false;
                break;
        }
    }

    protected JPanel addToLayout(Ole ocomponents, String title, boolean border) {
        GridBagConstraints gc;
        JPanel dataPanel;
        JLabel label;
        JTextField text;
        JCheckBox checkbox;
        Ole fieldproperties;

        gc = new GridBagConstraints();
        dataPanel = new JPanel();
        dataPanel.setLayout(new GridBagLayout());
        if (border) {
            dataPanel.setBorder(BorderFactory.createTitledBorder(title));
        }
        gc.weightx = 1;
        gc.weighty = 1;
        gc.insets = new Insets(spacing, spacing, spacing, spacing);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;
        for (String sfield : ocomponents.getFieldList()) {
            if (ocomponents.getFieldType(sfield).equals(oletype.INTEGER.name())
                    || ocomponents.getFieldType(sfield).equals(oletype.DOUBLE.name())
                    || ocomponents.getFieldType(sfield).equals(oletype.STRING.name())) {
                text = new JTextField();
                text.setText(ocomponents.getField(sfield));
                text.setPreferredSize(new Dimension(fieldwidth, fieldheight));
                components.put(sfield, text);
                gc.gridx = 0;
                gc.anchor = GridBagConstraints.WEST;
                label = new JLabel(sfield);
                dataPanel.add(label, gc);
                gc.gridx++;
                dataPanel.add(text, gc);
            } else if (ocomponents.getFieldType(sfield).equals(oletype.BOOLEAN.name())) {
                checkbox = new JCheckBox();
                checkbox.setSelected(ocomponents.getBoolean(sfield));
                components.put(sfield, checkbox);
                gc.gridx = 0;
                gc.anchor = GridBagConstraints.WEST;
                label = new JLabel(sfield, SwingConstants.LEFT);
                dataPanel.add(label, gc);
                gc.gridx++;
                dataPanel.add(checkbox, gc);
            }else if (ocomponents.getFieldType(sfield).equals(oletype.OLE.name())) {
                gc.gridx = 0;
                gc.anchor = GridBagConstraints.WEST;
                dataPanel.add(addToLayout(ocomponents.getOle(sfield), sfield, true), gc);
            }
             gc.gridy++;
      }
        return dataPanel;
    }

    protected void Ole2Layout() {
        ArrayList<String> tabs = new ArrayList(input.getAllTabNames());
        JPanel pTab;
        JLabel lA;
        Ole currentTab;
        GridBagConstraints gc = new GridBagConstraints(), gc2;
        gc.weightx = 1;
        gc.weighty = 1;
        gc.insets = new Insets(spacing, spacing, spacing, spacing);

        for (String stab : tabs) {
            currentTab = input.getTab(stab);
            pTab = new JPanel();
            pTab.setLayout(new BoxLayout(pTab, BoxLayout.LINE_AXIS));
            pTab.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            pTab.add(addToLayout(currentTab, "", false));
            tpMain.addTab(stab, null, pTab);
        }
        myFrame.pack();
    }
//                }//                    case "LIST":
//                        OleList olist = new OleList(tab.getOle(sfield));
//                        olist = olist.getIntersection(olist);
//                        String slist[] = new OleList(olist).prettyprint().split(" ");
//                        JComboBox jcbA = new JComboBox(slist);
//                        jcbA.setPreferredSize(new Dimension(fieldwidth, fieldheight));
//                        components.put(sfield, jcbA);
//                        jcbA.setSelectedItem(slist[0]);
//                        gc.gridx = 0;
//                        gc.anchor = GridBagConstraints.WEST;
//                        lA = new JLabel(sfield, SwingConstants.LEFT);
//                        pTab.add(lA, gc);
//                        gc.gridx++;
//                        pTab.add(jcbA, gc);
//                        break;
//                    case "RECORD":
//                        OleRecord olrecord = new OleRecord(tab.getOle(sfield));
//                        JPanel group = new JPanel();
//                        group.setLayout(new GridBagLayout());
//                        group.setBorder(BorderFactory.createTitledBorder(sfield));
//                        gc2 = new GridBagConstraints();
//                        gc2.weightx = 1;
//                        gc2.weighty = 1;
//                        gc2.insets = new Insets(spacing, spacing, spacing, spacing);
//                        gc2.anchor = GridBagConstraints.WEST;
//                        gc2.gridx = 0;
//                        gc2.gridy = 0;
//                        for (String sr2 : olrecord.getFieldList()) {
//                            lA = new JLabel(sr2, SwingConstants.LEFT);
//                            group.add(lA, gc2);
//                            gc2.gridx = (gc2.gridx + 1) % 4;
//                            jcA = new JCheckBox();
//                            jcA.setSelected(olrecord.getBoolean(sr2));
//                            components.put(sr2, jcA);
//                            group.add(jcA, gc2);
//                            gc2.gridx = (gc2.gridx + 1) % 4;
//                            if (gc2.gridx == 0) {
//                                gc2.gridy++;
//                            }
//                            gc.gridx = 0;
//                            gc.gridwidth = 4;
//                            pTab.add(group, gc);
//                        }
//                        break;
//

    protected void getFromLayout(Ole currentTab) {
       JPanel dataPanel;
        JLabel label;
        JTextField text;
        JCheckBox checkbox;

        for (String sfield : currentTab.getFieldList()) {
//                System.out.println("OLEDIALOG Tab" + stab + " Field " + sfield);
                if (currentTab.getFieldType(sfield).equals(oletype.STRING.name())) {
                    text = (JTextField) components.get(sfield);
                    currentTab.setField(sfield, text.getText());
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
//        for (String stab : fields) {
//            tab = new OleRecord();
//            myTab = new OleRecord(output.getOle(stab));
//            for (String sfield : myTab.getFieldList()) {
//                switch (myTab.getFieldType(sfield)) {
//                    case "STRING":
//                        JTextField jtA = (JTextField) components.get(sfield);
//                        tab.setField(sfield, jtA.getText());
//                        break;
//                    case "INTEGER":
//                    case "DOUBLE":
//                        int iv;
//                        double db;
//                        jtA = (JTextField) components.get(sfield);
//                        try {
//                            iv = Integer.parseInt(jtA.getText());
//                            tab.setField(sfield, iv);
//                        } catch (Exception ex) {
//                            db = Double.parseDouble(jtA.getText());
//                            tab.setField(sfield, db);
//                        }
//                        break;
//
//                    case "BOOLEAN":
//                        JCheckBox jcA = (JCheckBox) components.get(sfield);
//                        tab.setField(sfield, jcA.isSelected());
//                        break;
//                    case "LIST":
//                        OleList olist = new OleList(),
//                         olistaux = new OleList();
//                        olistaux.addUniqueItem(new OleList(myTab.getOle(sfield)).prettyprint().split(" "));
//                        JComboBox jcbA = (JComboBox) components.get(sfield);
//                        olist.addUniqueItem((String) jcbA.getSelectedItem());
//                        olist.addDupItem(olistaux.prettyprint().split(" "));
//                        tab.setField(sfield, olist);
//                        break;
//                    case "RECORD":
//                        OleRecord olrecord = new OleRecord(myTab.getOle(sfield)),
//                         oleres = new OleRecord();
//                        for (String sr2 : olrecord.getFieldList()) {
//                            jcA = (JCheckBox) components.get(sr2);
//                            oleres.setField(sr2, jcA.isSelected());
//                        }
//                        tab.setField(sfield, oleres);
//                        break;
//
//                }
//            }
//            input.setField(stab, tab);
//        }
        }
    }

}
