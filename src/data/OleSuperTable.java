/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.WriterConfig;
import static crypto.Keygen.getHexaKey;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author lcv
 */
// JsonArray does not contain data, it is an index to the real data, which are common objects
public class OleSuperTable extends OleTable {

    public OleSuperTable() {
        super();
        InitTable();
    }

    public OleSuperTable(Ole o) {
        super(o);
        InitTable();
    }

    @Override
    protected void initRows() {
        if (size() > 0) {
            Ole o = getRow(0);
            for (String f : o.getFieldList()) {
                addField(f);
            }
        }
    }

    @Override
    public OleSuperTable addRow(Ole o) {
        String key = getHexaKey(16);
        rawRows().add(key);
        setField(key, o);
        if (rawRows().size() == 1) {
            initRows();
        }
        return this;
    }

    public OleSuperTable addRow(String rowkey, Ole o) {
        String key = rowkey;
        rawRows().add(key);
        setField(key, o);
        if (rawRows().size() == 1) {
            initRows();
        }
        return this;
    }

    
    @Override
    public Ole getRow(int r) {
        if (0 <= r && r < size()) {
            return getOle(rawRows().get(r).asString());
        } else {
            return null;
        }
    }

    // Index
    public ArrayList<Integer> getAllRowIndex(String field, String value) {
        ArrayList<Integer> indexes = new ArrayList();
        for (int i = 0; i < size(); i++) {
            if (getRow(i).getString(field, "").equals(value)) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    public int getRowIndex(String key) {
        for (int i = 0; i < size(); i++) {
            if (rawRows().get(i).asString().equals(key)) {
                return i;
            }
        }
        return -1;
    }

    public int getRowIndex(String field, String value) {
        ArrayList<Integer> indexes = getAllRowIndex(field, value);
        if (!indexes.isEmpty()) {
            return indexes.get(0);
        } else {
            return -1;
        }
    }

    // derived
//    public Ole getFullRow(String key) {
//        int index = getRowIndex(key);
//        if (index >= 0) {
//            return getFullRow(index);
//        } else {
//            return null;
//        }
//    }
//
//    public Ole getFullRow(String field, String value) {
//        int index = getRowIndex(field, value);
//        if (index >= 0) {
//            return getFullRow(index);
//        } else {
//            return null;
//        }
//    }
//

    @Override
    public Ole getRow(String field, String value) {
        return getRow(getRowIndex(field, value));
    }

    public boolean removeRow(int r) {
        if (0 <= r && r < size()) {
            rawRows().remove(r);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ArrayList<Ole> getAllRows() {
        ArrayList<Ole> res = new ArrayList();
        for (int i = 0; i < size(); i++) {
            res.add(getRow(i));
        }
        return res;
    }

    @Override
    public ArrayList<Ole> getAllRows(String field, String value) {
        ArrayList<Ole> res = new ArrayList();
        for (int i = 0; i < size(); i++) {
            if (getRow(i).getString(field, "").equals(value)) {
                res.add(getRow(i));
            }
        }
        return res;
    }

    @Override
    public ArrayList<Ole> getAllRows(String field, int value) {
        ArrayList<Ole> res = new ArrayList();
        for (int i = 0; i < size(); i++) {
            if (getRow(i).getInt(field, -1) == value) {
                res.add(getRow(i));
            }
        }
        return res;
    }

    @Override
    public OleSuperTable getAllRowsOleTable(String field, String value) {
        OleSuperTable res = new OleSuperTable();
        for (int i = 0; i < size(); i++) {
            if (getRow(i).getString(field, "").equals(value)) {
                res.addRow(getRow(i));
            }
        }
        return res;
    }

    @Override
    public OleSuperTable getAllRowsOleTable(String field, int value) {
        OleSuperTable res = new OleSuperTable();
        for (int i = 0; i < size(); i++) {
            if (getRow(i).getInt(field, -1) == value) {
                res.addRow(getRow(i));
            }
        }
        return res;
    }

}
