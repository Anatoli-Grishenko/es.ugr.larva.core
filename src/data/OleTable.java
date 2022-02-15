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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author lcv
 */
public class OleTable extends Ole {

    public OleTable() {
        super();
        InitTable();
    }

    public OleTable(Ole o) {
        super(o);
        InitTable();
    }

    @Override
    public boolean isEmpty() {
        return this.size() < 1;
    }

    public OleTable(ResultSet rs) {
        super();
        InitTable();
        Ole aux;
        String key;
        if (rs == null) {
            return;
        }
        try {
            // get column names
            ResultSetMetaData rsMeta = rs.getMetaData();
            int columnCnt = rsMeta.getColumnCount();
            String name;
            while (rs.next()) {
                aux = new Ole();
                key = "";
                for (int i = 1; i <= columnCnt; i++) {
//                    System.out.println(">>>>> THIS\n" + this.prettyprint());
//                    System.out.println(">>>>> AUX\n" +aux.toPlainJson().toString(WriterConfig.PRETTY_PRINT));
//                    System.out.println(">>>>> THIS\n" + this.toPlainJson().toString(WriterConfig.PRETTY_PRINT));
//                    System.out.println(">>>>> AUX" +aux.toPlainJson().toString(WriterConfig.PRETTY_PRINT));
                    name = rsMeta.getColumnName(i);
                    switch (rsMeta.getColumnType(i)) {
                        case java.sql.Types.INTEGER:
                            aux.setField(name, rs.getInt(name));
                            if (i == 1) {
                                key = "" + rs.getInt(name);
                            }
                            break;
                        case java.sql.Types.TINYINT:
                            aux.setField(name, rs.getBoolean(name));
                            if (i == 1) {
                                key = "" + rs.getBoolean(name);
                            }
                            break;
                        case java.sql.Types.DOUBLE:
                        case java.sql.Types.DECIMAL:
                            aux.setField(name, rs.getInt(name));
                            if (i == 1) {
                                key = "" + rs.getInt(name);
                            }
                            break;
                        case -7:
                            aux.setField(name, rs.getBoolean(name));
                            break;
                        case java.sql.Types.VARCHAR:
                        case java.sql.Types.LONGVARCHAR:
                            try {
                            aux.setField(name, rs.getString(name));
                            if (i == 1) {
                                key = "" + rs.getString(name);
                            }
                        } catch (SQLException Ex) {
                            aux.setField(name, rs.getString(1));
                            if (i == 1) {
                                key = "" + rs.getString(1);
                            }
                        }
                        break;
                        default: // Other result types are not included in the table
                            break;
                    }
                }
                this.addRow(aux);
            }
        } catch (SQLException ex) {
            System.err.println("JSONRESULT:: " + ex.toString());
        }

    }

    private void InitTable() {
        setType(oletype.OLETABLE.name());
        add("rows", new JsonArray()); // No añade el field
    }

    public JsonArray rawRows(){
        return this.get("rows").asArray();
    }
    private void initRows() {
        if (rawRows().size() > 0) {
            Ole o = new Ole(rawRows().get(0).asObject());
            for (String f : o.getFieldList()) {
                addField(f);
            }
        }
    }

    public OleTable addRow(Ole o) {
        rawRows().add(o);
        if (rawRows().size() == 1) {
            initRows();
        }
        return this;
    }

    public Ole getRow(int r) {
        if (0 <= r && r < size()) {
            return new Ole(rawRows().get(r).asObject());
//            return new Ole(rawRows().get(r).asObject().toString());
        } else {
            return new Ole();
        }
    }

    public Ole getRow(String field, int value) {
        Ole res = new Ole(), aux;
        for (JsonValue jsvo : rawRows()) {
            aux = new Ole(jsvo.asObject());
            if (aux.getInt(field) == value) {
                return aux;
            }
        }
        return res;

    }

    public Ole getRow(String field, String value) {
        Ole res = new Ole(), aux;
        for (JsonValue jsvo : rawRows()) {
            aux = new Ole(jsvo.asObject());
            if (aux.getField(field).equals(value)) {
                return aux;
            }
        }
        return res;

    }

    public ArrayList<Ole> getAllRows() {
        return new ArrayList(Transform.toArrayList(rawRows()));
    }

    public ArrayList<Ole> getAllRows(String field, String value) {
        ArrayList<Ole> res = new ArrayList();
        Ole aux;
        for (JsonValue jsvo : rawRows()) {
            aux = new Ole(jsvo.asObject());
            if (aux.getField(field).equals(value)) {
                res.add(aux);
            }
        }
        return res;
    }

    public ArrayList<Ole> getAllRows(String field, int value) {
        ArrayList<Ole> res = new ArrayList();
        Ole aux;
        for (JsonValue jsvo : rawRows()) {
            aux = new Ole(jsvo.asObject());
            if (aux.getInt(field) == value) {
                res.add(aux);
            }
        }
        return res;
    }

    public OleTable getAllRowsOleTable(String field, String value) {
        OleTable res = new OleTable();
        Ole aux;
        for (JsonValue jsvo : rawRows()) {
            aux = new Ole(jsvo.asObject());
            if (aux.getField(field).equals(value)) {
                res.addRow(aux);
            }
        }
        return res;
    }

    public OleTable getAllRowsOleTable(String field, int value) {
        OleTable res = new OleTable();
        Ole aux;
        for (JsonValue jsvo : rawRows()) {
            aux = new Ole(jsvo.asObject());
            if (aux.getInt(field) == value) {
                res.addRow(aux);
            }
        }
        return res;
    }

    public int size() {
        return rawRows().size();
    }

    public String prettyprint() {
        String res = "";
        ArrayList<String> names = new ArrayList();
        int i = 0;
        for (; i<size();i++){ 
            if (i == 0) {
                res += "|";
                names = new ArrayList(getFieldList());
                for (String col : names) {
                    res += String.format("%15s|", col);
                }
                res += "\n+";
                for (String col : names) {
                    res += "---------------+";
                }
            }
            res += "\n|";
            for (String key : names) {
                String value = rawRows().get(i).asObject().get(key).toString();
                res += String.format("%15s|", value.substring(0, Math.min(15, value.length())));
            }
        }
        res += "\n";

        return res;
    }
//    public String prettyprint() {
//        String res = "";
//        ArrayList<String> names = new ArrayList();
//        int i = 0;
//        for (Ole orow : getAllRows()) {
//            if (i == 0) {
//                res += "|";
//                names = new ArrayList(getFieldList());
//                for (String col : names) {
//                    res += String.format("%15s|", col);
//                }
//                res += "\n+";
//                for (String col : names) {
//                    res += "---------------+";
//                }
//            }
//            res += "\n|";
//            for (String key : names) {
//                String value = orow.getField(key);
//                res += String.format("%15s|", value.substring(0, Math.min(15, value.length())));
//            }
//            i++;
//        }
//        res += "\n";
//
//        return res;
//    }
}
