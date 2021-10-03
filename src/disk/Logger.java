package disk;

import console.Console;
import static console.Console.defText;
import static console.Console.lightgreen;
import static console.Console.lightred;
import static console.Console.white;
import com.eclipsesource.json.JsonObject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import tools.TimeHandler;

/**
 * A general-purpose class for logging messages, errors and exceptions, both on
 * screen and on disk. It can be used from a general program or from an agent's
 * body. It admits as record a String or a JsonObject and it produces the
 * following log - On screen: the string or the JsonObject.toString() in the
 * specified color - On disk: every record on a single line on disk, encoded in
 * JSON, preceded by a timestamp - {"date":"24/06/2020_11:52:23","record":.....}
 * - In order to homogeneize Strings and JsonObjects, Strings are also stored as
 * JsonObjects with the key "info"
 *
 */
public class Logger {

    protected static String _indent; /// Indentation of messages
    protected final String _cindent = "|   ";

    protected String _filename, /// Name of the file  to store the log on disk
            _lastlog, /// Record last log
            _default, /// Default filename    
            _owner, /// Name of the agent which owns the Logger
            _qualifier; /// Label which extends the name of the owner
    protected boolean /// Parameters
            _validFile, /// The selected file is not available
            _echo, /// If true, it echoes everything on screen, otherwise is silent
            _tabular, /// If true, the echo is arranged as a tabulated output
            _overwrite;         /// If true, the log file on disk contains only the last run, otherwise it appends run after run
    protected int _maxLength, /// If the logged message exceeds this length, it is trimmed
            _textColor;             /// Color of the echoed texts
    protected PrintStream _outTo, ///  Default output stream for echoing messages. Std.out
            _errTo;                 /// Default output for errors. Std.err

    /**
     * Initializes the instance
     */
    public Logger() {
        _filename = _default;
        _validFile = false;
        _echo = false;
        _tabular = false;
        _owner = null;
        _outTo = System.out;
        _errTo = System.err;
        _textColor = white;
        _maxLength = -1; ///350;
        _owner = "";
        _qualifier = "";
        _indent = "";
        _overwrite = false;
    }

    /**
     * Returns the name of the selected file to record the log on file
     *
     * @return
     */
    public String getLoggerFileName() {
        return _filename;
    }

    public Logger setOwner(String name) {
        _owner = name;
        return this;
    }

    public Logger setOwnerQualifier(String s) {
        _qualifier = s;
        return this;
    }

    public Logger setLoggerFileName(String fname) {
        if (initRecord(fname)) {
            _filename = fname;
        }
        return this;
    }

    public Logger setOutputTo(PrintStream out) {
        _outTo = out;
        _errTo = out;
        return this;
    }

    public Logger setTextColor(int color) {
        _textColor = color;
        return this;
    }

    public Logger onTabular() {
        _tabular = true;
        return this;
    }

    public Logger offTabular() {
        _tabular = false;
        return this;
    }

    public Logger onOverwrite() {
        this._overwrite = true;
        return this;
    }

    public Logger onAppend() {
        _overwrite = false;
        return this;
    }

    public Logger setEcho(boolean e) {
        _echo = e;
        return this;
    }

    public Logger onEcho() {
        _echo = true;
        return this;
    }

    public Logger offEcho() {
        _echo = false;
        return this;
    }

    public boolean isEcho() {
        return _echo;
    }

    protected boolean initRecord(String filename) {
        File file;

        file = new File(filename);
        if (filename != null) {
            if (file.exists()) {
                if (file.isFile()) {
                    if (_overwrite) {
                        file.delete();
                    }
                    return (_validFile = true);    // Fichero existe
                } else {
                    return (_validFile = false);   // Es Directorio
                }
            } else {
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    logException(ex);
                    return (_validFile = false);   // Fichero nuevo MAL
                }
                return (_validFile = true);        // Fichero nuevo OK
            }
        } else {
            return (_validFile = false);            // null
        }
    }

    protected JsonObject addRecord(JsonObject o) {
        String timeStamp = TimeHandler.Now();//new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(Calendar.getInstance().getTime());
        JsonObject json = new JsonObject(), record = new JsonObject();
        if (!_owner.equals("")) {
            record.add("agent", _owner);
        }
        if (!this._qualifier.equals("")) {
            record.add("label", _qualifier);
        }
        record.merge(o);
        json.add("date", timeStamp).
                add("record", record);
        String toRecord = json.toString();
        if (_validFile) {
            PrintWriter outfile;
            try {
                // save log
                outfile = new PrintWriter(new BufferedWriter(new FileWriter(_filename, true)));
                BufferedWriter out = new BufferedWriter(outfile);
                outfile.println(toRecord);
                outfile.close();
            } catch (IOException ex) {
                logException(ex);
            }

        }
        return json;
    }

    public JsonObject logMessage(String message) {
        Output(message);
        JsonObject jso = new JsonObject().add("info", message);
        return addRecord(jso);
    }

    public JsonObject logMessage(JsonObject details) {
        Output(details.toString());
        return addRecord(details);
    }

    public JsonObject logError(String message) {
        Error(message);
        return addRecord(new JsonObject().add("info", message));
    }

    public JsonObject logError(JsonObject details) {
        Error(details.toString());
        return addRecord(details);

    }

    public JsonObject logException(Exception ex) {
        StringWriter sexc = new StringWriter();
        PrintWriter psexc = new PrintWriter(sexc);
        ex.printStackTrace(psexc);
        return logError(new JsonObject().add("uncaught-exception", ex.toString()).
                add("info", sexc.toString()));

    }

    protected String formatOutput(String s) {
        String res;
        if (this._maxLength > 0 && s.length() > _maxLength || s.contains("filedata")) {
            s = trimString(s, _maxLength);
        }
        String heading;
        if (_qualifier.equals("")) {
            heading = String.format("%-10s", _owner);
        } else {
            heading = String.format("%-10s %-10s", _owner, _qualifier);
        }
        if (_tabular) {
//            res = String.format("%-20s %-20s %s", heading, TimeHandler.Now().substring(11), s);
            res = String.format("%-20s %-20s %s", heading, TimeHandler.Now(), s);
        } else {
            res = _owner + ": " + s;
        }
        if (res.contains("acl_send")) {
            res = defText(lightgreen) + res;
        }
        if (res.contains("acl_receive")) {
            res = defText(lightred) + res;
        }
        return res;
    }

    public void incIndent() {
        _indent += _cindent;
    }

    public void decIndent() {
        if (_indent.length() > 0) {
            _indent = _indent.substring(_cindent.length());
        }
    }

    protected void Output(String s) {
        s = _indent + s;
        try {
            _lastlog = formatOutput(s) + "\n";
            if (_echo) {
                _outTo.printf(_lastlog);
            }
        } catch (Exception Ex) {
            _lastlog = s.substring(0, 10) + "\n";
            if (_echo) {
                _outTo.printf(_lastlog);
            }

        }
    }

    protected void Error(String s) {
        _lastlog = formatOutput(s) + "\n";
        _errTo.printf(_lastlog);
    }

    public static String trimString(String original, int max) {
        String s = original + "";
        if (s.length() > max) {
            return s.substring(0, Math.min(max, s.length())) + "...";
        } else {
            return s;
        }
    }

    public String getLastlog() {
        return _lastlog;
    }

}
