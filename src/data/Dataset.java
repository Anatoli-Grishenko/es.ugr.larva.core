/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.eclipsesource.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
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
    protected ArrayList<DatasetRow> tuples;
    protected DatasetHeader header;
    protected String name;

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

    protected ArrayList<DatasetRow> getTuples() {
        return tuples;
    }

    public DatasetHeader getHeader() {
        return header;
    }

    @Override
    public String toString() {
        int fieldwidth = 20;
        Align halign = Align.LEFT;
        Overflow celloverflow = plainTable.Overflow.WRAP;
        plainTable printer = new plainTable(name, 1 + getTuples().size(), this.getHeader().getNColumns(), TableType.ALPHANUMERIC);
        printer.setBorders(true, true, true, true);
        printer.setXLabels(new Vector(this.getHeader().getColumnNames()));
        printer.setColumnWidth(fieldwidth);
        int k = 0;
        return printer.toString();
    }

    public Dataset addRow() {
        JsonObject nrow = new JsonObject();
        tuples.add(header.getEmptyRow());
        return this;
    }

    public DatasetRow getRow(int i) {
        if (i >= 0 && i < tuples.size()) {
            return tuples.get(i);
        } else {
            return null;
        }
    }

    public Dataset setData(int row, int column, double v) {
        return setData(row, getHeader().getColumnNames().get(column), v);
    }

    public Dataset setData(int row, int column, String v) {
        return setData(row, getHeader().getColumnNames().get(column), v);
    }

    public Dataset setData(int row, int column, Boolean v) {
        return setData(row, getHeader().getColumnNames().get(column), v);
    }

    public Dataset setData(int row, int column, TimeHandler v) {
        return setData(row, getHeader().getColumnNames().get(column), v);
    }

    public Dataset setData(int row, String column, double v) {
        getRow(row).set(column, v);
        return this;
    }

    public Dataset setData(int row, String column, String v) {
        getRow(row).set(column, v);
        return this;
    }

    public Dataset setData(int row, String column, Boolean v) {
        getRow(row).set(column, v);
        return this;
    }

    public Dataset setData(int row, String column, TimeHandler v) {
        getRow(row).set(column, v.toString());
        return this;
    }
}
