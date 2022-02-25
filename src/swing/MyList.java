/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class MyList extends JList implements ListSelectionListener, ActionListener {

    public static enum Type {
        STRING, FILE, FOLDER
    };
    private JButton add, remove;
    private DefaultListModel listModel;
    private JScrollPane listPane;
    private Type mytype;

    public MyList() {
        super();
    }

    public MyList init(Type t) {
        listModel = new DefaultListModel();
        add = new JButton("+");
        add.addActionListener(this);
        remove = new JButton("-");
        remove.addActionListener(this);
        mytype = t;
        this.setModel(listModel);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        addListSelectionListener(this);
        setVisibleRowCount(3);
        listPane = new JScrollPane(this);
        listPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        listPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        listPane.setPreferredSize(new Dimension(100,150));
        return this;
    }

    public MyList addElement(String element) {
        listModel.addElement(element);
        return this;
    }

    public MyList addAllElements(ArrayList<String> elements) {
        for (String element : elements) {
            listModel.addElement(element);
        }
        return this;
    }

    public MyList removeElement(int pos) {
        listModel.remove(pos);
        return this;
    }

    public MyList removeElement(String element) {
        listModel.removeElement(element);
//        int index = this.gets
//        listModel.addElement(element);
        return this;
    }

    public MyList clear(){
        listModel.clear();
        return this;
    }
    
    private int getIndex(String what) {
        int res = -1;
        for (int i = 0; i < listModel.size(); i++) {
            if (what.equals((String) listModel.get(i))) {
                return i;
            }
        }
        return res;
    }

    public DefaultListModel getListMode() {
        return listModel;
    }

    public JScrollPane getPane() {
        return listPane;
    }

    public JButton getAddButton() {
        return add;
    }

    public JButton getRemoveButton() {
        return remove;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {
            if (getSelectedIndex() == -1) {
                remove.setEnabled(false);

            } else {
                remove.setEnabled(true);
            }
        } else
                remove.setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String toAdd = "";
        switch (e.getActionCommand()) {
            case "+":
                switch (mytype) {
                    case FILE:
                        toAdd= OleDialog.doSelectFile("./");
                        break;
                    case FOLDER:
                        toAdd= OleDialog.doSelectFolder("./");
                        break;
                    case STRING:
                        toAdd = JOptionPane.showInputDialog(null, "Please type new string to add", "List", JOptionPane.QUESTION_MESSAGE);
                        break;
                }
                if (toAdd != null && toAdd.length() > 0 && !listModel.contains(toAdd)) {
                    int index=listModel.size() + 1;
                    listModel.addElement(toAdd);
                    this.setSelectedIndex(listModel.size()-1);
                    this.ensureIndexIsVisible(listModel.size()-1);
                }
                break;
            case "-":
                int index = this.getSelectedIndex();
                this.removeElement(index);
                break;
        }
    }

}
