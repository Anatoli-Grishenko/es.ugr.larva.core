/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import JsonObject.JsonObject;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DatasetRow extends JsonObject {
    DatasetHeader header;
    Dataset dset;

    public DatasetRow(Dataset ds) {
        super();
        dset=ds;
        header = ds.getHeader();
        for (String scolname : header.getColumnNames()) {
            switch (header.getColumnType(scolname)) {
                case BOOLEAN:
                    setData(scolname, false);
                    break;
                case STRING:
                    setData(scolname, "");
                    break;
                case DATE:
                    setData(scolname, TimeHandler.Now(dset.getDateTemplate()));
                    break;
                case NUMBER:
                    setData(scolname, 0.0);
                    break;
            }
        }

    }

    protected Dataset getDataset() {
        return dset;
    }

    public DatasetRow setData(int column, Object o) {
        return setData(header.getColumnNames().get(column), o);
    }

    public DatasetRow setData(String columnname, Object o) {
        if (header.getColumnType(columnname) == Dataset.DATATYPE.BOOLEAN) {
            if (o instanceof Boolean) {
                set(columnname, (Boolean) o);
                return this;
            } else if (o instanceof String) {
                set(columnname, Boolean.parseBoolean((String) o));
                return this;            }
        }
        if (header.getColumnType(columnname) == Dataset.DATATYPE.DATE) {
            if (o instanceof TimeHandler) {
                setData(columnname, ((TimeHandler) o).toNumber());
                return this;
            } else if (o instanceof String) {
                setData(columnname,new TimeHandler((String) o,dset.getDateTemplate()).toNumber());
                return this;            }
        }
        if (header.getColumnType(columnname) == Dataset.DATATYPE.STRING) {
            if (o instanceof String) {
                set(columnname, (String) o);
                return this;
            } 
       }
        if (header.getColumnType(columnname) == Dataset.DATATYPE.NUMBER ||
                header.getColumnType(columnname) == Dataset.DATATYPE.DATE) {
            if (o instanceof Double) {
                set(columnname, (Double) o);
            } else if (o instanceof Integer) {
                setData(columnname, 1.0*(Integer) o);
            } else if (o instanceof Long) {
                setData(columnname, 1.0*(Long) o);
            } else if (o instanceof String) {
                set(columnname, Double.parseDouble(((String) o).replace(",",".")));
            }
        }
//        if (header.getColumnType(columnname) == Dataset.DATATYPE.STRING
//                && o instanceof String) {
//            set(columnname, (String) o);
//            return this;
//        }
//        if (header.getColumnType(columnname) == Dataset.DATATYPE.NUMBER
//                && (o instanceof Integer || o instanceof Double)) {
//            set(columnname, (Double) o);
//            return this;
//        }
//        if (header.getColumnType(columnname) == Dataset.DATATYPE.DATE
//                && o instanceof TimeHandler) {
//            set(columnname, ((TimeHandler) o).toString());
//            return this;
//        }
            return this;
    }

    public DatasetRow importTSVRow(String csvline) {
        String fields[] = csvline.split("\t");
        for (int i = 0; i < fields.length; i++) {
            this.setData(getDataset().getHeader().getColumnNames().get(i), fields[i]);
        }
        return this;
    }
//   protected DatasetRow setData(int column, double v) {
//        return setData(getColumnNames().get(column), v);
//    }
//
//    public DatasetRow setData(int column, String v) {
//        return setData(getColumnNames().get(column), v);
//    }
//
//    public DatasetRow setData(int column, Boolean v) {
//        return setData(getHeader().getColumnNames().get(column), v);
//    }
//
//    public DatasetRow setData(int column, TimeHandler v) {
//        return setData(getHeader().getColumnNames().get(column), v);
//    }

//    protected DatasetRow setData(String column, double v) {
//        set(column, v);
//        return this;
//    }
//
//    protected DatasetRow setData(String column, String v) {
//        set(column, v);
//        return this;
//    }
//
//    protected DatasetRow setData(String column, Boolean v) {
//        set(column, v);
//        return this;
//    }
//
//    protected DatasetRow setData(String column, TimeHandler v) {
//        set(column, v.toString());
//        return this;
//    }
}
