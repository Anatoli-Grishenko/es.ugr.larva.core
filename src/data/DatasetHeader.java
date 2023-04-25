/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import data.Dataset.DATATYPE;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DatasetHeader extends JsonObject {
    ArrayList <DATATYPE> coltypes=new ArrayList();

    public DatasetHeader() {
        super();
      }

    public DatasetHeader(DatasetHeader dsh) {
        super();
        dsh.getColumnNames().forEach(colname -> {
            this.addColumn(colname, dsh.getColumnType(colname));
        });
      }

    public DatasetHeader addColumn(String colname, DATATYPE coltype) {
        this.add(colname,coltype.name());
        coltypes.add(coltype);
        return this;
    }
    
    public int getNColumns() {
        return this.size();
    }
    
    public ArrayList<String> getColumnNames() {
        return new ArrayList(this.names());
    }
    
    
    public DATATYPE getColumnType(String columname) {
        return DATATYPE.valueOf(this.get(columname).asString());
    }
    
    public int getColumnIndex(String columname) {
        return getColumnNames().indexOf(columname);
    }
    
    public ArrayList<DATATYPE> getColumnTypes() {
            return coltypes;
//        return getColumnNames().stream().
//                map(DatasetHeader::getColumnType).
//                collect(Collectors.toList());                
    }
    
//    public DatasetRow getEmptyRow() {
//        DatasetRow row = new DatasetRow();
//        for (String scolname : this.getColumnNames()) {
//            switch (this.getColumnType(scolname)) {
//                case BOOLEAN:
//                    row.add(scolname, false);
//                    break;
//                case STRING:
//                    row.add(scolname, "");
//                    break;
//                case DATE:
//                    row.add(scolname, TimeHandler.Now());
//                    break;
//                case NUMBER:
//                    row.add(scolname, 0.0);
//                    break;
//            }
//        }
//        return row;
//    }
    
}
