/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Vector;
import static tools.StringTools.repeatString;

/**
 * windowFrames[] = {"┌┐└┘─│┬┴┼┤├▄",
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class plainTable {

    public static enum Align {
        CENTER, LEFT, RIGHT
    }

    public static enum Overflow {
        WRAP, MULTILINE
    }

    public static enum TableType {
        NUMERIC, ALPHANUMERIC
    };
    protected TableType _type;
    private String _ID;
    private Vector<String> _xLabel, _yLabel;
    private double _content[][];
    private String _scontent[][];
    private final int _CSV = 0, _JSON = 1;
    int _Width, _Height;
    private static DateTimeFormatter inputdateformat = DateTimeFormatter.ofPattern("dd/MM/uuuu"),
            outputdateformat = inputdateformat;
    private final int maxrows = 50;
    protected boolean extHBorder, extVBorder, intHBorder, intVBorder;
    protected Align _align;
    protected Overflow _Overflow;
    protected int _columnWidths[];

    public plainTable() {
        setType(TableType.ALPHANUMERIC);
        createTable("", 0, 0);
    }

    public plainTable(String id, int filas, int columnas, TableType type) {
        setType(type);
        createTable(id, filas, columnas);
    }

    public int getWidth() {
        return _Width;
    }

    public int getHeight() {
        return _Height;
    }

    public TableType getType() {
        return _type;
    }

    public void setType(TableType _type) {
        this._type = _type;
    }

    public Align getAlign() {
        return _align;
    }

    public void setAlign(Align _align) {
        this._align = _align;
    }

    public Overflow getOverflow() {
        return _Overflow;
    }

    public void setOverflow(Overflow _Overflow) {
        this._Overflow = _Overflow;
    }

    public void setBorders(boolean ExtH, boolean ExtV, boolean IntH, boolean IntV) {
        extHBorder = ExtH;
        extVBorder = ExtV;
        intHBorder = IntH;
        intVBorder = IntV;
    }

    public void setColumnWidth(int column, int w) {
        if (column >= 0 && column < getWidth()) {
            _columnWidths[column] = w;
        }
    }

    public void setColumnWidth(int w) {
        for (int i = 0; i < getWidth(); i++) {
            setColumnWidth(i, w);
        }
    }

    public int getColumnWidth(int column) {
        return _columnWidths[column];
    }

    private void createTable(String id, int filas, int columnas) {
        _ID = id;
        setAlign(Align.LEFT);
        setOverflow(Overflow.WRAP);
        setBorders(true, true, true, true);
        if (filas <= 0 || columnas <= 0) {
            _xLabel = null;
            _yLabel = null;
            _content = null;
        } else {
            _xLabel = new Vector<String>();
            _yLabel = new Vector<String>();
            _Width = columnas;
            _Height = filas;
            _columnWidths = new int[_Width];

            if (getType() == TableType.NUMERIC) {
                _content = new double[filas][columnas];
            } else {
                _scontent = new String[filas][columnas];
            }
        }
    }

    public boolean isValid() {
        if (getType() == TableType.NUMERIC) {
            return _content != null;
        } else {
            return _scontent != null;
        }
    }

    public void setFValue(int f, int c, double v) {
        if (isValid()) {
            _content[f][c] = v;
        }
    }

    public void setSValue(int f, int c, String v) {
        if (isValid()) {
            _scontent[f][c] = v;
        }
    }

    public double getFValue(int f, int c) {
        if (isValid()) {
            return _content[f][c];
        } else {
            return -1;
        }
    }

    public String getSValue(int f, int c) {
        if (isValid()) {
            if (_scontent[f][c] != null) {
                return _scontent[f][c];
            } else {
                return "";
            }
        } else {
            return null;
        }
    }

    public void setXLabels(Vector<String> lab) {
        if (!isValid()) {
            return;
        }
        _xLabel.addAll(lab);
    }

    public void setYLabels(Vector<String> lab) {
        if (!isValid()) {
            return;
        }
        _yLabel.addAll(lab);
    }

    public Vector<String> getXLabels() {
        if (!isValid()) {
            return null;
        }
        return _xLabel;
    }

    public Vector<String> getYLabels() {
        if (!isValid()) {
            return null;
        }
        return _yLabel;
    }

    public double maxColumn(int column) {
        double res = getFValue(0, column);
        for (int i = 1; i < getHeight(); i++) {
            if (getFValue(i, column) > res) {
                res = getFValue(i, column);
            }
        }
        return res;
    }

    public double maxRow(int row) {
        double res = getFValue(row, 0);
        for (int i = 1; i < getWidth(); i++) {
            if (getFValue(row, i) > res) {
                res = getFValue(row, i);
            }
        }
        return res;
    }

    public double minColumn(int column) {
        double res = getFValue(0, column);
        for (int i = 1; i < getHeight(); i++) {
            if (getFValue(i, column) < res) {
                res = getFValue(i, column);
            }
        }
        return res;
    }

    public double minRow(int row) {
        double res = getFValue(row, 0);
        for (int i = 1; i < getWidth(); i++) {
            if (getFValue(row, i) < res) {
                res = getFValue(row, i);
            }
        }
        return res;
    }

    public double sumColumn(int column) {
        double res = 0;
        for (int i = 0; i < getHeight(); i++) {
            res += getFValue(i, column);
        }
        return res;
    }

    public double sumRow(int row) {
        double res = 0;
        for (int i = 0; i < getWidth(); i++) {
            res += getFValue(row, i);
        }
        return res;
    }

    public double averageRow(int row) {
        return sumRow(row) / getWidth();
    }

    public double medianRow(int row) {
        double aux[] = new double[getWidth()];
        for (int i = 0; i < getWidth(); i++) {
            aux[i] = getFValue(row, i);
        }
        Arrays.sort(aux);
        int pos = getWidth() / 2;
        if (getWidth() % 2 == 0) {
            return (aux[pos] + aux[pos - 1]) / 2;
        } else {
            return aux[pos];
        }
    }

    public double sumRowWhen(int rowtosum, int rowtocheck, double criteria) {
        double res = 0;
        double comparator;

        for (int i = 0; i < getWidth(); i++) {
            comparator = getFValue(rowtocheck, i);
            if (comparator == criteria) {
                res += getFValue(rowtosum, i);
            }
        }
        return res;
    }

    public double sumColumnWhen(int columntosum, int columntocheck, double criteria) {
        double res = 0;
        double comparator;

        for (int i = 0; i < getHeight(); i++) {
            if (columntocheck < 0) {
                comparator = i;//LocalDate.parse(_xLabel.get(i),inputdateformat).atStartOfDay().hashCode();
            } else {
                comparator = getFValue(i, columntocheck);
            }
            if (comparator == criteria) {
                res += getFValue(i, columntosum);
            }
        }
        return res;
    }

    public double sumColumnWhen(int columntosum, int columntocheck1, double criteria1,
            int columntocheck2, double criteria2) {
        double res = 0;
        double comparator1, comparator2;

        for (int i = 0; i < getHeight(); i++) {
            if (columntocheck1 < 0) {
                comparator1 = i; //LocalDate.parse(_xLabel.get(i),inputdateformat).atStartOfDay().hashCode();
            } else {
                comparator1 = getFValue(i, columntocheck1);
            }
            if (columntocheck2 < 0) {
                comparator2 = i; //LocalDate.parse(_xLabel.get(i),inputdateformat).atStartOfDay().hashCode();
            } else {
                comparator2 = getFValue(i, columntocheck2);
            }
            if (comparator1 == criteria1 && comparator2 == criteria2) {
                res += getFValue(i, columntosum);
            }
        }
        return res;
    }

    public String toString(int format) {
        String result = "", sep = "\t";

        if (!isValid()) {
            return "";
        }
        result += this._ID + "\n";
        switch (format) {
            default: // TSV
                for (int i = 0; i < _xLabel.size(); i++) {
                    result += _xLabel.get(i);
                    if (i < _xLabel.size() - 1) {
                        result += sep;
                    } else {
                        result += "\n";
                    }
                }
                for (int f = 0; f < getHeight(); f++) {
                    if (!_yLabel.isEmpty()) {
                        result += _yLabel.get(f) + sep;
                    }
                    for (int c = 0; c < getWidth(); c++) {
                        result += "" + String.format("%.5f", getFValue(f, c));
                        if (c < getWidth() - 1) {
                            result += sep;
                        } else {
                            result += "\n";
                        }
                    }
                }
                break;
        }
        for (int i = getHeight(); i < maxrows; i++) {
            result += "\n";
        }
        result += "---\n";
        return result;
    }

    public static String hRuler(int width, int step, boolean extremes) {
        String res = "";
        int ini = 0, end = width - 1;
        for (int i = ini; i <= end; i++) {
            if (i == ini) {
                if (extremes) {
                    res += "├";
                } else {
                    res += "─";
                }
            } else if (i == end) {
                if (extremes) {
                    res += "┤";
                } else {
                    res += "─";
                }
            } else if ((i - ini) % step == 0) {
                res += "┬";
            } else {
                res += "─";
            }

        }
        return res;
    }

    public String gettopLeft() {
        if (extVBorder) {
            if (extHBorder) {
                return "┌";
            } else {
                return "│";
            }
        } else {
            if (extHBorder) {
                return "─";
            } else {
                return " ";
            }
        }
    }

    public String gettopRight() {
        if (extVBorder) {
            if (extHBorder) {
                return "┐";
            } else {
                return "│";
            }
        } else {
            if (extHBorder) {
                return "─";
            } else {
                return " ";
            }
        }
    }

    public String getbottomRight() {
        if (extVBorder) {
            if (extHBorder) {
                return "┘";
            } else {
                return "│";
            }
        } else {
            if (extHBorder) {
                return "─";
            } else {
                return " ";
            }
        }
    }

    public String getbottomLeft() {
        if (extVBorder) {
            if (extHBorder) {
                return "└";
            } else {
                return "│";
            }
        } else {
            if (extHBorder) {
                return "─";
            } else {
                return " ";
            }
        }
    }

    public String getbottomMid() {
        if (intVBorder) {
            if (extHBorder) {
                return "┴";
            } else {
                return "│";
            }
        } else {
            if (extHBorder) {
                return "─";
            } else {
                return " ";
            }
        }
    }

    public String gettopMid() {
        if (intVBorder) {
            if (extHBorder) {
                return "┬";
            } else {
                return "│";
            }
        } else {
            if (extHBorder) {
                return "─";
            } else {
                return " ";
            }
        }
    }

    public String getMidLeft() {
        if (extVBorder) {
            if (extHBorder) {
                return "├";
            } else {
                return "│";
            }
        } else {
            if (extHBorder) {
                return "─";
            } else {
                return " ";
            }
        }
    }

    public String getMidRight() {
        if (extVBorder) {
            if (extHBorder) {
                return "┤";
            } else {
                return "│";
            }
        } else {
            if (extHBorder) {
                return "─";
            } else {
                return " ";
            }
        }
    }

    public String getextV() {
        if (extVBorder) {
            return "│";
        } else {
            return " ";
        }
    }

    public String getinV() {
        if (intVBorder) {
            return "│";
        } else {
            return " ";
        }
    }

    public String getinH() {
        if (intHBorder) {
            return "─";
        } else {
            return " ";
        }
    }

    public String getextH() {
        if (extHBorder) {
            return "─";
        } else {
            return " ";
        }
    }

    public String getinner() {
        if (intVBorder) {
            if (intHBorder) {
                return "┼";
            } else {
                return "│";
            }
        } else {
            if (intHBorder) {
                return "─";
            } else {
                return " ";
            }
        }
    }

    public String topRow() {
        String res = gettopLeft();
        for (int i = 0; i < getWidth(); i++) {
            res += repeatString(getextH(), getColumnWidth(i));
            if (i < getWidth() - 1) {
                res += gettopMid();
            } else {
                res += gettopRight();
            }
        }
        res += "\n";
        return res;
    }

    public String bottomRow() {
        String res = getbottomLeft();
        for (int i = 0; i < getWidth(); i++) {
            res += repeatString(getextH(), getColumnWidth(i));
            if (i < getWidth() - 1) {
                res += getbottomMid();
            } else {
                res += getbottomRight();
            }
        }
        res += "\n";
        return res;
    }

    public String innerRow() {
        String res = getMidLeft();
        for (int i = 0; i < getWidth(); i++) {
            res += repeatString(getinH(), getColumnWidth(i));
            if (i < getWidth() - 1) {
                res += getinner();
            } else {
                res += getMidRight();
            }
        }
        res += "\n";
        return res;
    }

    public String getCell(int f, int c) {
        if (getType() == TableType.ALPHANUMERIC) {
            return StringTools.fitRow(getSValue(f, c), getAlign(), getColumnWidth(c));
        } else {
            return StringTools.fitRow(String.format("%5.2f", getFValue(f, c)), getAlign(), getColumnWidth(c));
        }
    }

    @Override
    public String toString() {
        String result = "";
        if (getID() != null) {
            result += getID() + "("+this.getHeight()+")\n";
        }
        if (!_xLabel.isEmpty()) {
            if (!_yLabel.isEmpty()) {
                result += StringTools.fitRow(" ", getAlign(), getColumnWidth(0));
            }
            for (int i = 0; i < getWidth(); i++) {
                result += StringTools.fitRow(_xLabel.get(i), getAlign(), getColumnWidth(i));
                if (i < _xLabel.size() - 1) {
                    result += " ";
                } else {
                    result += "\n";
                }
            }
        }
        if (extHBorder) {
            if (!_yLabel.isEmpty()) {
                result += StringTools.fitRow(" ", getAlign(), getColumnWidth(0)) + topRow();
            } else {
                result += topRow();
            }
        }
        for (int f = 0; f < getHeight(); f++) {
            if (!_yLabel.isEmpty()) {
                result += StringTools.fitRow(_yLabel.get(f), getAlign(), getColumnWidth(0));
            }
            result += getextV();
            for (int c = 0; c < getWidth(); c++) {
                result += getCell(f, c);
                if (c < getWidth() - 1) {
                    result += getinV();
                } else {
                    result += getextV();
                }
            }
            result += "\n";
            if (f < getHeight() - 1) {
                if (intHBorder) {
                    if (!_yLabel.isEmpty()) {
                        result += StringTools.fitRow(" ", getAlign(), getColumnWidth(0));
                    }
                    result += innerRow();
                }
            }
        }
        if (extHBorder) {
            if (!_yLabel.isEmpty()) {
                result += StringTools.fitRow(" ", getAlign(), getColumnWidth(0));
            }
            result += bottomRow();
        }
        return result;
    }

    public String getID() {
        return _ID;
    }

    public void setID(String _ID) {
        this._ID = _ID;
    }

}
