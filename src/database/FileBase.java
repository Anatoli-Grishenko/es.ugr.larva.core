/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import JsonObject.JsonValue;
import static crypto.Keygen.getHexaKey;
import data.Ole;
import data.OleFile;
import disk.Logger;
import java.io.File;
import java.util.HashMap;
import data.OleSuperTable;
import java.util.ArrayList;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class FileBase extends Ole {

    protected Ole _Schema;
    protected HashMap<String, OleSuperTable> _Tables;
    protected String _defaultPath = "filebase";
    protected Logger _logger;

    public FileBase(Logger l) {
        super();
        setType("FileBase");
        _logger = l;
        init();
    }

    public FileBase(Ole o, Logger l) {
        super(o);
        setType("FileBase");
        init();
        _logger = l;
    }

    protected void init() {
        _Schema = new Ole();
        _Tables = new HashMap();
    }

    public String getDefaultPath() {
        return _defaultPath;
    }

    public boolean setSchema(Ole schema) {
        OleSuperTable ot;
        File f;
        init();
        try {
            _Schema = new Ole(schema);
//            if (!_Schema.saveAsFile(_defaultPath, "/schema.json", true)) {
//                return false;
//            }
            _logger.setOwner(_Schema.getField("name"));
            _logger.logMessage("Abriendo eschema " + _Schema.getField("name"));
            for (JsonValue oTable : schema.get("tables").asArray()) {
                ot = new OleSuperTable(new Ole(oTable.asObject()));
                String tName = ot.getField("name");
                f = new File(getTableFileName(tName));
                if (!f.exists()) {
                    _logger.logMessage("Creando tabla " + tName);
                    f.createNewFile();
                    ot.saveAsFile("./", getTableFileName(tName), false);
                    addTable(tName, ot);
                } else {
                    _logger.logMessage("Cargando tabla " + tName + "... ");
                    loadTable(tName);
//                    ot.loadFile(getDefaultPath() + "/" + ot.getField("name") + ".json");
                    _logger.logMessage("" + getTableSize(tName) + " filas leídas");
                }
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public FileBase addTable(String name, OleSuperTable table) {
        _Tables.put(name, table);
        saveTable(name);
        return this;
    }

    public OleSuperTable getTable(String tableName) {
        return _Tables.get(tableName);
    }

    public int getTableSize(String tableName) {
        return _Tables.get(tableName).size();
    }

    public boolean loadTable(String tableName) {
        OleSuperTable otable = new OleSuperTable();
        try {
            otable.loadFile(getTableFileName(tableName));
            _Tables.put(tableName, otable);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean saveTable(String tablename) {
        try {
            _logger.logMessage("Salvando tabla " + tablename);
            _Tables.get(tablename).saveAsFile("./", getTableFileName(tablename), true);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean saveAll() {
        try {
            for (String tabname : _Tables.keySet()) {
                saveTable(tabname);
//                _Tables.get(tabname).saveAsFile(getDefaultPath() + "/", tabname + ".json", true);
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String addTuple(String tableName) {
        Ole oRow = new Ole();
        String key = getHexaKey(8);
        oRow.setField(getTablePK(tableName), key);
        getTable(tableName).addRow(key,oRow);
        saveTable(tableName);
        return key;
    }

    public Ole getFullTuple(String tableName, String tuplePK) {
        int index = getTable(tableName).getRowIndex(tuplePK);
        if (index >= 0) {
            return getTable(tableName).getRow(index);
        }
        return null;
    }

    public Ole getFullTuple(String tableName, String field, String value) {
        int index = getTable(tableName).getRowIndex(field, value);
        if (index >= 0) {
            return getTable(tableName).getRow(index);
        }
        return null;
    }

    public ArrayList<Ole> getAllFullTuple(String tableName, String field, String value) {
        ArrayList<Ole> res = new ArrayList();
        for (int i : getTable(tableName).getAllRowIndex(field, value)) {
            res.add(getTable(tableName).getRow(i));
        }
        return res;
    }

    public String getTuplePK(String tableName, String field, String value) {
        int index = getTable(tableName).getRowIndex(field, value);
        if (index >= 0) {
            return getTable(tableName).getRow(index).getField(getTablePK(tableName));
        }
        return null;
    }

    public ArrayList<String> getAllTuplePK(String tableName, String field, String value) {
        ArrayList<String> res = new ArrayList();
        for (int i : getTable(tableName).getAllRowIndex(field, value)) {
            res.add(getTable(tableName).rawRows().get(i).asString());
        }
        return res;
    }

    public boolean updateTuple(String tableName, String tupleID, String field, String value) {
        Ole oRow;
        if (value != null) {
            oRow = getFullTuple(tableName, tupleID);
            oRow.setField(field, value);
            saveTable(tableName);
            return true;
        }
        return false;
    }

    public boolean removeTuplePK(String tableName, String tuplePK) {
        int index = getTable(tableName).getRowIndex(tuplePK);
        if (index >= 0) {
            getTable(tableName).removeRow(index);
            return true;
        }
        return false;
    }

    public String getTablePK(String tableName) {
        return tableName + "ID";
    }

    public String getTableFileName(String tableName) {
        return getDefaultPath() + "/" + tableName + ".json";
    }

}
