/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import database.OleDataBase.SQLOP;

/**
 *
 * @author lcv
 */
public class SentenceBuilder {

    String ssentence, scolumns, swhere, svalues, spairs, table;
    OleDataBase db;

    public SentenceBuilder(OleDataBase odb) { //, OleDataBase.SQLOP o, String t) {
        db = odb;
        clear();
    }
    
    public SentenceBuilder clear() {
        table ="";
        scolumns = "";
        swhere = "";
        svalues = "";
        spairs = "";
        return this;
    }

    public SentenceBuilder Op(SQLOP o) {
        clear();
        ssentence = o.name();
        return this;
    }
    
    public SentenceBuilder Table(String t) {
        table = t;
        return this;
    }
    
    public SentenceBuilder Column(String column) {
        if (db.getColumnList(table).contains(column)) {
            if (scolumns.length() == 0) {
                scolumns = column;
            } else {
                scolumns += ", " + column;
            }
            return this;
        } else {
            return null;
        }
    }

    public SentenceBuilder Pair(String column, String value) {
        OleDataBase.SQLTYPES type = OleDataBase.SQLTYPES.valueOf(db.getColumnType(table, column).toUpperCase());
        if (scolumns.length() == 0) {
            scolumns += column;
        } else {
            scolumns += " , " + column + " ";
        }
        if (spairs.length() == 0) {
            spairs += column + " = ";
        } else {
            spairs += " , " + column + " = ";
        }
        switch (type) {
            case BIGINT:
            case TINYINT:
            case INT:
            case DECIMAL:
                if (svalues.length() == 0) {
                    svalues += " " + value + " ";
                } else {
                    svalues += " , " + value + " ";
                }
                spairs += " " + value + " ";
                break;
            case VARCHAR:
            case LONGTEXT:
                if (svalues.length() == 0) {
                    svalues += " '" + value + "' ";
                } else {
                    svalues += " , '" + value + "' ";
                }
                spairs += " '" + value + "' ";
                break;
        }
        return this;
    }

    public SentenceBuilder Condition(String column, String comp, String value) {
        OleDataBase.SQLTYPES type = OleDataBase.SQLTYPES.valueOf(db.getColumnType(table, column).toUpperCase());
        if (swhere.length() == 0) {
            swhere += column + " " + comp + " ";
        } else {
            swhere += " and " + column + " " + comp + " ";
        }
        switch (type) {
            case BIGINT:
            case TINYINT:
            case INT:
            case DECIMAL:
                swhere += " " + value + " ";

                break;
            case VARCHAR:
            case LONGTEXT:
                swhere += " '" + value + "' ";              
                break;
        }
        return this;
    }

    @Override
    public String toString() {
        String sentence = ssentence;
        switch (ssentence) {
            case "SELECT":
                if (scolumns.length()==0)
                    scolumns = " * "; 
                sentence += scolumns + " FROM " + table;
                if (swhere.length() > 0) {
                    sentence += " " + " WHERE " + swhere;
                }
                break;
            case "INSERT":
                sentence += " INTO " + table + " (" + scolumns + ") " + " VALUES (" + svalues+")";
                break;
            case "UPDATE":
                sentence += " " + table + " SET " + spairs + " " + " WHERE  " + swhere;
                break;
            case "DELETE":
                sentence += "  FROM " + table + " WHERE  " + swhere;
                break;
        }        
        return sentence;
    }
}
