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
        if (getOle("properties").getFieldList().size()!=0)
            return getOle("properties");
        else
            return new Ole();
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

}
//
//  public int numTabs() {
//        return this.getAllTabNames().size();
//    }
//
//    public ArrayList<String> getAllTabNames() {
//        return new ArrayList(this.getFieldList());
//    }
//
//    public Ole getTab(int ntab) {
//        if (0 < ntab && ntab < numTabs()) {
//            return this.getOle(this.getAllTabNames().get(ntab));
//        } else {
//            return new Ole();
//        }
//    }
//
//    public Ole getTab(String stab) {
//        if (getAllTabNames().contains(stab)) {
//            return this.getOle(stab);
//        } else {
//            return new Ole();
//        }
//    }
//
//    public ArrayList <String> getTabAllFields(String stab) {
//        Ole tab = getTab(stab);
//        if (tab.size()==0) {
//            return new ArrayList();
//        } else
//            return new ArrayList(tab.getFieldList());
//    }
//    
//    public JsonValue getField(String stab, String field) {
//        Ole tab = getTab(stab);
//        if (tab.size()==0) {
//            return null;
//        } else {
//            JsonValue pre = tab.get(field);
//            if (pre ==null) {       /// Field does not exist
//                return null;
//            } else if (pre.isArray() ) { /// Field has constraints
//                return pre.asArray().get(0);
//            } else return pre; /// Field has no constraints
//        }
//    }

////   public int numTabs() {
////        return getTabList().size();
////    }
////
////    public ArrayList<String> getAllTabNames() {
////        return getTabList();
////    }
////
////    public Ole getTab(int ntab) {
////        if (0 < ntab && ntab < numTabs()) {
////            return getTab(getTabList().get(ntab));
////        } else {
////            return new Ole();
////        }
////    }
////
////    public Ole getTab(String stab) {
////        if (getAllTabNames().contains(stab)) {
////            return getTabContent().getOle(stab);
////        } else {
////            return new Ole();
////        }
////    }
////
////    public ArrayList<String> getTabAllFields(String stab) {
////        if (getAllTabNames().contains(stab)) {
////            return getTabContent().getArray(stab);
////        } else {
////            return new ArrayList();
////        }
////    }
