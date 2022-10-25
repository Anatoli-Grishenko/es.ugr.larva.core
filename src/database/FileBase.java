/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import com.eclipsesource.json.JsonValue;
import data.Ole;
import data.OleFile;
import data.OleTable;
import java.io.File;
import java.util.HashMap;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class FileBase extends Ole {

    protected Ole _Schema;
    protected HashMap<String, OleTable> _Tables;
    protected String defaultPath = "filebase";

    public FileBase() {
        super();
        setType("FileBase");
        init();
    }

    public FileBase(Ole o) {
        super(o);
        setType("FileBase");
        init();
    }

    protected void init() {
        _Schema = new Ole();
        _Tables = new HashMap();
    }

    public boolean setSchema(Ole schema) {
        OleTable ot;
        File f;
        init();
        try {
            _Schema = new Ole(schema);
            if (!_Schema.saveAsFile(defaultPath, "/schema.json", true)) {
                return false;
            }
            for (JsonValue oTable : schema.get("tables").asArray()) {
                ot = new OleTable(new Ole(oTable.asObject()));
                f = new File(defaultPath + "/" + ot.getField("name") + ".json");
                f.createNewFile();
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public FileBase addTable(String name, OleTable table) {
        _Tables.put(name, table);
        return this;
    }

}
