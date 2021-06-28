/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import glossary.ole;
import java.util.ArrayList;
import tools.ReportableObject;
import tools.TimeHandler;

/**
 *
 * @author lcv
 */
public class OleReport extends Ole {

    public OleReport() {
        super();
        setType(glossary.ole.REPORT.name());
    }

    private OleReport(Ole o) {
        super(o);
        setType(glossary.ole.REPORT.name());
    }

    public OleReport(ReportableObject o) {
        super();
        setType(ole.REPORT.name());
        setField("name", o.defReportType());
        setField("date", TimeHandler.Now());
        for (String s : o.defReportableObjectList()) {
            setField(s, o.reportObjectStatus(s));
        }
    }

    public String shortTextReport() {
        String sep = "\n";
        String res = "*"+ getField("name") +"*"+ sep + "*"+getField("date")+"*";
        ArrayList<String> fieldsreport = new ArrayList(this.getFullFieldList());
        fieldsreport.remove("ole");
        fieldsreport.remove("name");
        fieldsreport.remove("description");
        for (String s : fieldsreport) {
            OleReport aux;
            if (getField(s).length() > 0 && getField(s).charAt(0) == '{') {
                aux = new OleReport(new Ole(getField(s)));
                if (!aux.isEmpty()) {
                    res += aux.shortTextReport();
                } else {
                    res += sep + s + " " + getField(s) + " ";
                }
            } else {
                res += sep + s + " " + getField(s) + " ";
            }
        }
        res += sep+sep;
        return res;
    }

}
