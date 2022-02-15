/**
 * @file OleAnswer.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package data;

/**
 * Class devoted to wrap the answers of the servers to any request coming from
 * students
 */
public class OleQuery extends Ole {

    protected String sentence;

    /**
     * Basic constructor
     */
    public OleQuery() {
        super();
        InitQuery();
    }

    /**
     * Copy constructor
     *
     * @param o The object to be cloned
     */
    public OleQuery(Ole o) {
        super(o);
        InitQuery();
    }

    /**
     * Sets the default values of OleFile objects
     */
    private void InitQuery() {
        sentence = "";
        setType(oletype.OLEQUERY.name());
    }

    public OleQuery Pair(String field, Object o) {
        Condition(field, "=", o);
        return this;
    }

    public OleQuery Condition(String field, String comp, Object o) {
        if (o instanceof String) {
            setField(field, new Ole().setField("comp", comp).setField("value",(String) o));
        } else if (o instanceof Integer) {
            setField(field, new Ole().setField("comp", comp).setField("value",(Integer) o));
        } else if (o instanceof Double) {
            setField(field, new Ole().setField("comp", comp).setField("value",(Double) o));
        } else {
            setField(field, new Ole().setField("comp", comp).setField("value", o.toString()));
        }
        return this;
    }
}
