/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.eclipsesource.json.JsonArray;
import java.util.Vector;
import tools.TimeHandler;
import tools.plainTable;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DatasetStream extends JsonArray {

    protected Dataset.DATATYPE basedata;
    protected String dateformat = Dataset.FORMAT2;

    public DatasetStream(Dataset.DATATYPE bsd) {
        super();
        basedata = bsd;
    }

    public String getDateformat() {
        return dateformat;
    }

    public void setDateformat(String dateformat) {
        this.dateformat = dateformat;
    }

    public DatasetStream addData(Object o) {
        if (basedata == Dataset.DATATYPE.BOOLEAN) {
            if (o instanceof Boolean) {
                add((Boolean) o);
                return this;
            } else if (o instanceof String) {
                add(Boolean.parseBoolean((String) o));
                return this;
            }
        }
        if (basedata == Dataset.DATATYPE.DATE) {
            if (o instanceof TimeHandler) {
                add(((TimeHandler) o).toNumber());
                return this;
            } else if (o instanceof String) {
                add(new TimeHandler((String) o, dateformat).toNumber());
                return this;
            }
        }
        if (basedata == Dataset.DATATYPE.STRING) {
            if (o instanceof String) {
                add((String) o);
                return this;
            }
        }
        if (basedata == Dataset.DATATYPE.NUMBER
                || basedata == Dataset.DATATYPE.DATE) {
            if (o instanceof Double) {
                add((Double) o);
            } else if (o instanceof Integer) {
                add(1.0 * (Integer) o);
            } else if (o instanceof Long) {
                add(1.0 * (Long) o);
            } else if (o instanceof String) {
                add(Double.parseDouble(((String) o).replace(",", ".")));
            }
        }
        return this;
    }

    @Override
    public String toString() {
        int fieldwidth = 20;
        plainTable.Align halign = plainTable.Align.LEFT;
        plainTable.Overflow celloverflow = plainTable.Overflow.WRAP;
        plainTable printer = new plainTable("Data", Math.min(10, this.size()), 1, plainTable.TableType.ALPHANUMERIC);
        printer.setBorders(true, true, true, true);
//        printer.setXLabels(new Vector(this.getHeader().getColumnNames()));
        printer.setColumnWidth(fieldwidth);
        for (int j = 0; j < Math.min(10, size()); j++) {
            if (this.basedata == Dataset.DATATYPE.DATE) {
                printer.setSValue(j, 0, new TimeHandler(get(j).asDouble()).toString());
//                    printer.setSValue(j, i, dsr.get(header.getColumnNames().get(i)).toString());
            } else {
                printer.setSValue(j, 0, get(j).toString());
            }
        }
        return printer.toString();
    }

}

