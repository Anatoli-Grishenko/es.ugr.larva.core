/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import JsonObject.JsonObject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;
import tools.TimeHandler;
import tools.plainTable;
import tools.plainTable.Align;
import tools.plainTable.Overflow;
import tools.plainTable.TableType;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Dataset {

    public static enum DATATYPE {
        BOOLEAN, STRING, NUMBER, DATE
    };
    public static final String FORMAT1 = "uuuu-MM-dd kk:mm:ss:SSS", FORMAT2 = "dd/MM/uuu kk:mm:ss";
    protected ArrayList<DatasetRow> tuples;
    protected DatasetHeader header;
    protected String name, dateTemplate = FORMAT1;
    protected TimeHandler rootTime = new TimeHandler("2000-01-01 00:00:00:000");

    public Dataset(String dname, DatasetHeader dsh) {
        super();
        tuples = new ArrayList();
        header = dsh;
        name = dname;
    }

    public Dataset(String dname, Dataset parent) {
        super();
        tuples = new ArrayList();
        name = dname;
        header = parent.getHeader();
    }

    public Dataset clear() {
        tuples.clear();
        return this;
    }

    public String getDateTemplate() {
        return dateTemplate;
    }

    public void setDateTemplate(String dateTemplate) {
        this.dateTemplate = dateTemplate;
    }
//
//    public TimeHandler getRootTime() {
//        return rootTime;
//    }
//
//    public void setRootTime(TimeHandler rootTime) {
//        this.rootTime = rootTime;
//    }

    protected ArrayList<DatasetRow> getTuples() {
        return tuples;
    }

    int getNRows() {
        return tuples.size();
    }

    int getNColumns() {
        return header.getNColumns();
    }

    public DatasetHeader getHeader() {
        return header;
    }

    @Override
    public String toString() {
        int fieldwidth = 20;
        Align halign = Align.LEFT;
        Overflow celloverflow = plainTable.Overflow.WRAP;
        plainTable printer = new plainTable(name, Math.min(10, this.getNRows()), this.getHeader().getNColumns(), TableType.ALPHANUMERIC);
        printer.setBorders(true, true, true, true);
        printer.setXLabels(new Vector(this.getHeader().getColumnNames()));
        printer.setColumnWidth(fieldwidth);
        for (int j = 0; j < Math.min(10, this.getNRows()); j++) {
            DatasetRow dsr = this.getRow(j);
            for (int i = 0; i < getNColumns(); i++) {
                if (getHeader().getColumnTypes().get(i) == DATATYPE.DATE) {
                    printer.setSValue(j, i, new TimeHandler(dsr.get(header.getColumnNames().get(i)).asDouble()).toString());
//                    printer.setSValue(j, i, dsr.get(header.getColumnNames().get(i)).toString());
                } else {
                    printer.setSValue(j, i, dsr.get(header.getColumnNames().get(i)).toString());
                }
            }
        }
        return printer.toString();
    }

    public DatasetRow addRow() {
        DatasetRow emptyrow = new DatasetRow(this);
        return addRow(emptyrow);

    }

    public DatasetRow addRow(DatasetRow dr) {
        tuples.add(dr);
        return dr;
    }

    public DatasetRow getRow(int i) {
        if (i >= 0 && i < tuples.size()) {
            return tuples.get(i);
        } else {
            return null;
        }
    }

    public Dataset setData(int row, int column, Object o) {
        getRow(row).setData(column, o);
        return this;
    }

    public Dataset setData(int row, String columnname, Object o) {
        getRow(row).setData(columnname, o);
        return this;
    }

    public Dataset loadTSV(String filename) {
        try {
            this.clear();
            System.out.println("Opening ..." + filename);
            Scanner fsc = new Scanner(new File(filename));
            fsc.nextLine();
            while (fsc.hasNext()) {
                String line = fsc.nextLine();
                System.out.print(".");
//                System.out.println(line);
                DatasetRow dsr = new DatasetRow(this);
                dsr.importTSVRow(line);
                addRow(dsr);
            }
            System.out.println("\n" + this.getNRows() + " rows loaded");
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
        return this;
    }
    
    public DatasetStream getColumn(String columnname) {
        DatasetStream res = new DatasetStream(getHeader().getColumnType(columnname));
        res.setDateformat(getDateTemplate());
        for (int i=0; i<getNRows(); i++) {
            res.add(getRow(i).get(columnname));
        }
        return res;
    }
    
    //    public Dataset setData(int row, int column, double v) {
//        return setData(row, getHeader().getColumnNames().get(column), v);
//    }
//
//    public Dataset setData(int row, int column, String v) {
//        return setData(row, getHeader().getColumnNames().get(column), v);
//    }
//
//    public Dataset setData(int row, int column, Boolean v) {
//        return setData(row, getHeader().getColumnNames().get(column), v);
//    }
//
//    public Dataset setData(int row, int column, TimeHandler v) {
//        return setData(row, getHeader().getColumnNames().get(column), v);
//    }
//
//    public Dataset setData(int row, String column, double v) {
//        getRow(row).setData(column, v);
//        return this;
//    }
//
//    public Dataset setData(int row, String column, String v) {
//        getRow(row).setData(column, v);
//        return this;
//    }
//
//    public Dataset setData(int row, String column, Boolean v) {
//        getRow(row).setData(column, v);
//        return this;
//    }
//
//    public Dataset setData(int row, String column, TimeHandler v) {
//        getRow(row).setData(column, v.toString());
//        return this;
//    }
}
