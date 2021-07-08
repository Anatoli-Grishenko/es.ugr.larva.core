/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import com.eclipsesource.json.JsonValue;
import data.Ole;
import data.OleQuery;
import data.OleTable;
import data.Transform;
import static database.OleDataBase.SQLOP.DELETE;
import static database.OleDataBase.SQLOP.INSERT;
import static database.OleDataBase.SQLOP.SELECT;
import static database.OleDataBase.SQLOP.UPDATE;
import database.OleDataBase.SQLTYPES;
import disk.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import tools.ReportableObject;
import tools.TimeHandler;

/**
 *
 * @author lcv
 */
public class OleDataBase implements ReportableObject {

    public static enum SQLOP {
        SELECT, DELETE, INSERT, UPDATE
    };

    public Ole DBSchema;

    public static enum SQLTYPES {
        INT, TINYINT, BIGINT, DECIMAL, VARCHAR, LONGTEXT, DATE
    };

    public static ArrayList<String> SQLTypes = new ArrayList(Transform.toArrayList(new String[]{"int", "tinyint", "bigint", "decimal", "varchar", "longtext", "date"}));
    public static final int BADRECORD = -1;
    protected Connection _DBconnection;
    protected boolean _isError, _immediateClose;
    protected String _whichError;
    protected ArrayList<String> _errorLog;
    protected String _url, _host, _database, _user, _password;
    protected PreparedStatement st;
    protected ResultSet rs;
    protected String q;
    protected int count, _port;
    protected Logger _dlogger;
    protected OleTable _oleResultSet;
    protected SentenceBuilder _sb;
    // Report
    String lastTransaction, openDate;
    int countTransactions;

    //
    // Lowest level
    //
    public OleDataBase() {
        _dlogger = new Logger();
        _dlogger.setOwner("DataBase API");
        _dlogger.setLoggerFileName("databaselog.json");
        _dlogger.offEcho();
        _dlogger.onTabular();
        _immediateClose = false;
        closeConnection();
    }

    public boolean defineConnection(String host, int port, String database, String user, String password) {
        _host = host;
        _port = port;
        _database = database;
        _user = user;
        _password = password;
        return true;
    }

    public boolean openConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            _url = "jdbc:mysql://" + _host + ":" + _port + "/" + _database + "?serverTimezone=CET";
            _DBconnection = DriverManager.getConnection(_url, _user, _password);
            _dlogger.logMessage("Open connection " + getURL());
            flushError();
            openDate = TimeHandler.Now();
            _sb = new SentenceBuilder(this);
            loadSchema();
        } catch (ClassNotFoundException | SQLException ex) {
            reportException(ex);
            emergencyClose();
            return false;
        }
        return true;
    }

    public String getURL() {
        return _url;
    }

    public final void closeConnection() {
        if (isOpen()) {
            try {
                if (!_immediateClose) {
                    closeTransaction();
                }
                _DBconnection.close();
                _dlogger.logMessage("Closed connection " + getURL());
            } catch (SQLException ex) {
                reportException(ex);
            }
        }
        _DBconnection = null;
        lastTransaction = "";
        countTransactions = 0;
        openDate = "";
        flushError();
    }

    public void emergencyClose() {
        _dlogger.logError("EMERGENCY STOP & EXIT");
        closeConnection();
        System.exit(1);
    }

    public boolean isOpen() {
        return _DBconnection != null;
    }

    public boolean isError() {
        return _isError;
    }

    public boolean canContinue() {
        boolean can = isOpen() && !isError();
        if (!can) {
//            _dlogger.logError("Cannot continue due to: " + _errorLog.get(0));
            emergencyClose();
        }
        return can;
    }

    public void flushError() {
        if (_errorLog != null) {
            _errorLog.clear();
        } else {
            _errorLog = new ArrayList();
        }
        _dlogger.logMessage("Flushing errors out");
        _isError = false;
        _whichError = "";
    }

    public void validationQuery() {
        if (canContinue()) {
            this.DBquery("SELECT 1");
        }
    }

    //
    //
    // Transactions
    //
    //
    public void startCommit() throws SQLException {
        this._DBconnection.setAutoCommit(false);
    }

    public void endCommit() throws SQLException {
        this._DBconnection.commit();
        this._DBconnection.setAutoCommit(true);
    }

    public void rollBack() {
        try {
            System.err.append("Database rolling back");
            this._DBconnection.rollback();
            this.flushError();
        } catch (SQLException ex) {
            reportException(ex);
            emergencyClose();
        }
    }

    public void openTransaction() {
        if (_immediateClose) {
            if (!isOpen()) {
                this.openConnection();
            }
        } else {
            closeTransaction();
        }
    }

    public void closeTransaction() {
        try {
            if (rs != null) {
                rs.close();
            }
            if (st != null) {
                st.close();
            }
        } catch (SQLException ex) {
            reportException(ex);
        }
        if (_immediateClose) {
            this.closeConnection();
        }
    }

    //
    //
    // SentenceBuilder
    //
    //
    public SentenceBuilder sentence(SQLOP o) {
        return _sb.Op(o);
    }

    public OleTable DBSBquery(SentenceBuilder s) {
        return DBquery(s.toString());
    }

    public boolean DBSBupdate(SentenceBuilder s) {
        return DBupdate(s.toString());
    }

    public boolean DBSBinsert(SentenceBuilder s) {
        return DBinsert(s.toString());
    }

    public boolean DBSBdelete(SentenceBuilder s) {
        return DBdelete(s.toString());
    }

    //
    // Abstract object
    //
    public OleTable DBObjectQuery(String table, OleQuery oq) {
        SentenceBuilder sb = new SentenceBuilder(this).Op(SELECT).Table(table);
        oq.getNetFieldList().forEach(f -> {
            sb.Condition(f, oq.getOle(f).getField("comp"),oq.getOle(f).getField("value"));
        });
        return DBSBquery(sb);
    }

    public boolean DBObjectDelete(String table, OleQuery oq) {
        SentenceBuilder sb = new SentenceBuilder(this).Op(DELETE).Table(table);
        oq.getNetFieldList().forEach(f -> {
            sb.Condition(f, oq.getOle(f).getField("comp"),oq.getOle(f).getField("value"));
        });
        return DBSBdelete(sb);
    }

    public boolean DBObjectUpdate(String table, OleQuery find, OleQuery update) {
        SentenceBuilder sb = new SentenceBuilder(this);
        if (find.isEmpty() || DBObjectQuery(table, find).size()==0) {
            sb.Op(INSERT).Table(table);
            update.getNetFieldList().forEach(f -> {
                sb.Pair(f, update.getOle(f).getField("value"));
            });
            return this.DBSBinsert(sb);
        } else {
            sb.Op(UPDATE).Table(table);
            find.getNetFieldList().forEach(f -> {
                sb.Condition(f, find.getOle(f).getField("comp"),find.getOle(f).getField("value"));
            });
            update.getNetFieldList().forEach(f -> {
            sb.Pair(f, update.getOle(f).getField("value"));
            });
            return this.DBSBupdate(sb);
        }
    }

    //
    // Plain SQL sentences
    //
    public OleTable DBquery(String sentence) {
        OleTable res = new OleTable();
        openTransaction();
        countTransactions++;
        lastTransaction = sentence;
        _dlogger.logMessage("QUERY: " + sentence);
        try {

            st = _DBconnection.prepareStatement(sentence);
            rs = st.executeQuery();
            _oleResultSet = new OleTable(rs);
            res = _oleResultSet;
        } catch (SQLException ex) {
            reportException(ex);
        }
        if (_immediateClose) {
            closeTransaction();
        }
        return res;
    }

    public boolean DBupdate(String sentence) {
        openTransaction();
        countTransactions++;
        lastTransaction = sentence;
        _dlogger.logMessage("UPDATE: " + sentence);
        try {
            st = _DBconnection.prepareStatement(sentence);
            count = st.executeUpdate();
            rs = null;
        } catch (SQLException ex) {
            reportException(ex);
        }
        if (_immediateClose) {
            closeTransaction();
        }
        return count > 0;
    }

    public boolean DBinsert(String sentence) {
        openTransaction();
        countTransactions++;
        lastTransaction = sentence;
        _dlogger.logMessage("INSERT: " + sentence);
        try {
            st = _DBconnection.prepareStatement(sentence);
            count = st.executeUpdate();
            rs = null;
        } catch (SQLException ex) {
            reportException(ex);
        }
        if (_immediateClose) {
            closeTransaction();
        }
        return count > 0;
    }

    public boolean DBdelete(String sentence) {
        openTransaction();
        countTransactions++;
        lastTransaction = sentence;
        _dlogger.logMessage("DELETE: " + sentence);
        try {
            st = _DBconnection.prepareStatement(sentence);
            count = st.executeUpdate();
            rs = null;
        } catch (SQLException ex) {
            reportException(ex);
        }
        if (_immediateClose) {
            closeTransaction();
        }
        return count > 0;
    }

    public boolean isEmpty() {
        return _oleResultSet == null || _oleResultSet.size() == 0;
    }

    public ResultSet getResult() {
        return rs;
    }

    public OleTable getOleTable() {
        return new OleTable(_oleResultSet);
    }

    //
    //
    // Metaqueries
    //
    //
    public void loadSchema() {
        OleTable otables, ocols;
        Ole tablecolumns;
        String table, column;
        String type;
        this.DBSchema = new Ole();
        DBSchema.setField("last_update", TimeHandler.Now());
        DBSchema.setField("tables", new ArrayList());
        DBSchema.setField("description", "Schema of database " + this._database + " List of tables");
        otables = this.DBquery("select * from information_schema.TABLES where TABLE_SCHEMA='" + this._database + "'");
        for (JsonValue jsvt : otables.getAllRowsJsonArray()) {
            table = jsvt.asObject().getString("TABLE_NAME", "");
            DBSchema.addToField("tables", table);
            tablecolumns = new Ole();
            tablecolumns.setField("columns", new ArrayList());
            tablecolumns.setField("types", new ArrayList());
            tablecolumns.setField("descrition", "Description of table " + table + ". List of columns names and types");
            ocols = this.DBquery("select * from information_schema.COLUMNS where TABLE_SCHEMA='" + this._database + "' and TABLE_NAME='" + table + "'");
            for (JsonValue jsvc : ocols.getAllRowsJsonArray()) {
                column = jsvc.asObject().getString("COLUMN_NAME", "");
                type = jsvc.asObject().getString("DATA_TYPE", "");
                tablecolumns.addToField("columns", column);
                tablecolumns.addToField("types", type);
                tablecolumns.setField(column, type);
            }
            DBSchema.setField(table, tablecolumns);
        }
    }

    public ArrayList<String> getTableList() {
        return DBSchema.getArray("tables");
    }

    public ArrayList<String> getColumnList(String tablename) {
        return DBSchema.getOle(tablename).getArray("columns");
    }

    public String getColumnType(String tablename, String columname) {
        return DBSchema.getOle(tablename).getField(columname);
    }

//    public String newSentence //
    //
    // OleQueries
    //
    //
        public boolean getORM() {
            this.loadSchema();
            String toFile ="";
            try {
                File f = new File("src/database/"+this._database+".java");
                PrintWriter of = new PrintWriter(f);
                of.println("package database;");
                of.println("import data.OleTable;");
    
                of.println("public class "+this._database+" {");
                of.println("public OleDataBase db;");
                of.println("public "+this._database+"(OleDataBase newdb) { db = newdb;}");
                ArrayList <String> tables = this.DBSchema.getArray("tables");
                for (String table: tables) {
                    of.println("public OleTable "+table+"GetRow(OleQuery oq) {");
                    of.println("return db.DBquery(\"select * from "+table+" where \"+field+ \"=\"+value); }");
                    of.println("public OleTable "+table+"GetRow(String field, String value) {");
                    of.println("return db.DBquery(\"select * from "+table+" where \"+field+ \"='\"+value+\"'\"); }");
                }
            of.println("}");
            of.close();
                return true;
            } catch (FileNotFoundException ex) {
                return false;
            }       
        }
    //
    // Reporting
    //
    public String[] errorLog() {
        return Transform.toArray(_errorLog);
    }

    public void reportException(Exception Ex) {
        StringWriter sexc = new StringWriter();
        PrintWriter psexc = new PrintWriter(sexc);
        Ex.printStackTrace(psexc);
        _isError = Ex != null;
        if (isError()) {
            _whichError += TimeHandler.Now() + "Sentence \n"+this.lastTransaction+"\n" + Ex.toString() + "\n";
            _errorLog.add(_whichError);
            _whichError += sexc.toString();
            _errorLog.add(sexc.toString());
            _dlogger.logError(_whichError);
            _whichError = "";
//            _dlogger.logException(Ex);
        }
    }

    public void reportError(String which) {
        _isError = !which.equals("");
        if (isError()) {
            _whichError += TimeHandler.Now() + " " + which + "\n";
            _errorLog.add(_whichError);
            _dlogger.logError(_whichError);
            _whichError = "";
        }
    }

    @Override
    public String defReportType() {
        return "DataBase Report";
    }

    @Override
    public String[] defReportableObjectList() {
        return new String[]{"open", "ntransactions", "lasttransaction", "nerrors", "errors"};
    }

    @Override
    public String reportObjectStatus(String objectid) {
        switch (objectid) {
            case "ntransactions":
                return "" + this.countTransactions;
            case "lasttransaction":
                return "" + this.lastTransaction;
            case "nerrors":
                return "" + this._errorLog.size();
            case "errors":
                return _errorLog.toString();
            default:
                return this.openDate;

        }
    }

}

class UserContext {

    int userID, groupID, courseID;
    String userName, groupName;
    long chatID;
    ArrayList<UserContext> teamMates;
}
