/**
 * @file OleConfig.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package data;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import swing.OleApplication;
import swing.OleDialog;
import swing.SwingTools;

public class OleConfig extends Ole {

    public OleConfig() {
        super();
        setType(oletype.OLECONFIG.name());
    }

    public OleConfig(Ole o) {
        super(o);
        setType(oletype.OLECONFIG.name());
    }

    //--------------------
    public Ole getProperties() {
        if (getOle("properties").getFieldList().size() != 0) {
            return getOle("properties");
        } else {
            return new Ole();
        }
    }

    public Ole getProperties(String sfield) {
        return getProperties().getOle(sfield);
    }

    public Ole getOptions() {
        if (getOle("options").getFieldList().size() != 0) {
            return getOle("options");
        } else {
            return this;
        }
    }

    protected List<String> getTabList() {
        Ole options = getOptions();
        ArrayList<String> res = new ArrayList();
        for (String s : options.getFieldList()) {
            if (options.getFieldType(s).equals(oletype.OLE.name())) {
                res.add(s);
            }
        }
        return res;
    }
//--------------------------------

    public int numTabs() {
        return getTabList().size();
    }

    public List<String> getAllTabNames() {
        ArrayList<String> res = new ArrayList();
        if (numTabs() == 0) {
            res.add("Options");
        } else {
            res = new ArrayList(this.getTabList());
        }
        return res;
    }

    public Ole getTab(String stab) {
        if (numTabs() == 0) {
            return getOptions();
        } else if (getAllTabNames().contains(stab)) {
            return getOptions().getOle(stab);
        } else {
            return new Ole();
        }
    }

    public List<String> getAllTabFields(String stab) {
        ArrayList<String> res = new ArrayList();
        return getTab(stab).getFieldList();
    }

    @Override
    public OleConfig edit(OleApplication parent) {
        SwingTools.doSwingWait(() -> {
            OleDialog oDlg = new OleDialog(parent, "Edit ");
            oDlg.setEdit(true);
            if (oDlg.run(this)) {
                this.set(oDlg.getResult().toPlainJson().toString());
            }
        });
        return this;
    }

    @Override
    public void view(OleApplication parent) {
        OleDialog oDlg = new OleDialog(parent, "View ");
        oDlg.setEdit(false);
        oDlg.run(new OleConfig(this));
    }

}
