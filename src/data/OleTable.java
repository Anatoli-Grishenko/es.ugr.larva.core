/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import glossary.ole;
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

    JsonArray rows;

    public OleTable() {
        super();
        Init();
    }

    public OleTable(OleTable o) {
        super(o);
        Init();
    }

    @Override
    public boolean isEmpty() {
        return this.size()<1;
    }

    public OleTable(ResultSet rs) {
        super();
        Init();
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
                    name = rsMeta.getColumnName(i);
                    switch (rsMeta.getColumnType(i)) {
                        case java.sql.Types.INTEGER:
                            aux.setField(name, rs.getInt(name));
                            if (i == 1) {
                                key = "" + rs.getInt(name);
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
                        default: 
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
                    }
                }
                this.addRow(aux);
            }
        } catch (SQLException ex) {
            System.err.println("JSONRESULT:: " + ex.toString());
        }

    }

    private void Init() {
        setType(ole.TABLE.name());
        // There is previous data in the ole data
        if (this.getFullFieldList().contains("rows")) {
            rows = data.get("rows").asArray();
            initRows();
        } else {
            checkField("rows");
            rows = new JsonArray();
            data.set("rows", rows);
        }

    }

    private void initRows() {
        if (rows.size() > 0) {
            Ole o = new Ole(rows.get(0).asObject());
            for (String f : o.getFullFieldList()) {
                checkField(f);
            }
        }
    }

    public OleTable addRow(Ole o) {
        rows.add(o.toJson());
        if (rows.size() == 1) {
            initRows();
        }
        return this;
    }

    public Ole getRow(int r) {
        if (0 <= r && r < size()) {
            return new Ole(rows.get(r).asObject());
//            return new Ole(rows.get(r).asObject().toString());
        } else {
            return new Ole();
        }
    }

    public Ole getRow(String field, int value) {
        Ole res = new Ole(), aux;
        for (JsonValue jsvo : rows) {
            aux = new Ole(jsvo.asObject());
            if (aux.getInt(field) == value) {
                return aux;
            }
        }
        return res;

    }

    public Ole getRow(String field, String value) {
        Ole res = new Ole(), aux;
        for (JsonValue jsvo : rows) {
            aux = new Ole(jsvo.asObject());
            if (aux.getField(field).equals(value)) {
                return aux;
            }
        }
        return res;

    }

    public ArrayList<Ole> getAllRows() {
        return new ArrayList(Transform.toArrayList(this.rows));
    }

    public ArrayList<Ole> getAllRows(String field, String value) {
        ArrayList<Ole> res = new ArrayList();
        Ole aux;
        for (JsonValue jsvo : rows) {
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
        for (JsonValue jsvo : rows) {
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
        for (JsonValue jsvo : rows) {
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
        for (JsonValue jsvo : rows) {
            aux = new Ole(jsvo.asObject());
            if (aux.getInt(field) == value) {
                res.addRow(aux);
            }
        }
        return res;
    }

    public JsonArray getAllRowsJsonArray() {
        return this.rows;
    }

    public int size() {
        return rows.size();
    }
}
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package data;
//
//import com.eclipsesource.json.JsonArray;
//import com.eclipsesource.json.JsonObject;
//import glossary.ole;
//import java.sql.ResultSet;
//import java.sql.ResultSetMetaData;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.HashMap;
//
///**
// *
// * @author lcv
// */
//public class OleTable extends Ole {
//    JsonArray rows = new JsonArray();
//    
//    public OleTable() {
//        super();
//        Init();
//    }
//    
//    public OleTable(OleTable o) {
//        super(o);
//        Init();
//    }
//
//    public OleTable(ResultSet rs) {
//        super();
//        Init();
//        Ole aux;
//        String key;
//        if (rs==null)
//            return ;
//        try {
//            // get column names
//            ResultSetMetaData rsMeta = rs.getMetaData();
//            int columnCnt = rsMeta.getColumnCount();
//            String name;
//            while (rs.next()) {
//                aux = new Ole();
//                key = "";
//                for (int i = 1; i <= columnCnt; i++) {
//                    name = rsMeta.getColumnName(i);
//                    switch (rsMeta.getColumnType(i)) {
//                        case java.sql.Types.INTEGER:
//                            aux.setField(name, rs.getInt(name));
//                            if (i == 1) {
//                                key = "" + rs.getInt(name);
//                            }
//                            break;
//                        case java.sql.Types.DOUBLE:
//                        case java.sql.Types.DECIMAL:
//                            aux.setField(name, rs.getInt(name));
//                            if (i == 1) {
//                                key = "" + rs.getInt(name);
//                            }
//                            break;
//                        case -7:
//                            aux.setField(name, rs.getBoolean(name));
//                            break;
//                        case java.sql.Types.VARCHAR:
//                        case java.sql.Types.LONGVARCHAR:
//                        default: 
//                            try {
//                            aux.setField(name, rs.getString(name));
//                            if (i == 1) {
//                                key = "" + rs.getString(name);
//                            }
//                        } catch (SQLException Ex) {
//                            aux.setField(name, rs.getString(1));
//                            if (i == 1) {
//                                key = "" + rs.getString(1);
//                            }
//                        }
//                        break;
//                    }
//                }
//                this.addRow(aux);
//            }
//        } catch (SQLException ex) {
//            System.err.println("JSONRESULT:: " + ex.toString());
//        }
//
//        
//    }
//
//    private void Init() {
//        setType(ole.TABLE.name());
//        checkField("rows");
//        data.set("rows", rows);
//
//    }
//
//    public OleTable addRow(Ole o) {
//        if (rows.size()==0) {
//            for (String f : o.getFullFieldList())
//                checkField(f);
//        }
//        rows.add(o.toJson());
//        return this;
//    }
//    
//    public Ole getRow(int r) {
//        if (0<= r && r < size()) {
//            return new Ole(rows.get(r).asObject());
////            return new Ole(rows.get(r).asObject().toString());
//        } else
//            return new Ole();
//    }
//    
//    public Ole getRow(String field, int value) {
//        return new Ole(rows.get(r).asObject().toString());
//     
//    }
//    
//    public JsonArray getAllRowsJsonArray() {
//        return this.rows;
//    }
//    
//    public int size() {
//        return rows.size();
//    }
//}
