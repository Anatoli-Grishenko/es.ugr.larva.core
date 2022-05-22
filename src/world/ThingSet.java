/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import data.Transform;
import geometry.Point3D;
import java.util.ArrayList;
import java.util.HashMap;
import ontology.Ontology;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class ThingSet {

    ThingIndex indexName, indexType, indexBelong, indexPosition;
    Ontology ontology;
    String typeFilter;

    public ThingSet() {
        super();
        typeFilter = "";
        indexType = new ThingIndex().setField("type");
        indexName = new ThingIndex().setField("name");
        indexBelong = new ThingIndex().setField("belong");
        indexPosition = new ThingIndex().setField("position");
    }

    public int size() {
        return indexName.size();
    }

    public void clear() {
        indexType.clear();
        indexName.clear();
        indexBelong.clear();
        indexPosition.clear();
    }

    public Ontology getOntology() {
        return ontology;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    public String getTypeFilter() {
        return typeFilter;
    }

    public ThingSet setTypeFilter(String typeFilter) {
        if (getOntology().isType(typeFilter)) {
            this.typeFilter = typeFilter;
        }
        return this;
    }

    public ArrayList<String> getAllNames() {
        return indexName.getKeys();
    }

    public ArrayList<String> getAllTypes() {
        return indexType.getKeys();
    }

    public ArrayList<String> getAllBelong() {
        return indexBelong.getKeys();
    }

    public ArrayList<String> getAllPosition() {
        return indexPosition.getKeys();
    }

    public ThingSet fromJson(JsonArray reading) {
        for (JsonValue jsv : reading) {
            Thing t = new Thing("");
            t.fromJson(jsv.asObject());
            addThing(t);
        }
        return this;
    }

    public ThingSet addThing(JsonObject jsothing) {
        Thing t = new Thing("");
        t.fromJson(jsothing);
        this.addThing(t);
        return this;
    }

    public ThingSet addThing(Thing t) {
        if (this.getTypeFilter().length() == 0 || getOntology().matchTypes(t.getType(), getTypeFilter())) {
            indexName.addIndexTo(t);
            indexType.addIndexTo(t);
            indexBelong.addIndexTo(t);
            indexPosition.addIndexTo(t);
        }
        return this;
    }

    public ThingSet removeThing(Thing t) {
        indexName.removeIndexTo(t);
        indexType.removeIndexTo(t);
        indexBelong.removeIndexTo(t);
        indexPosition.removeIndexTo(t);
        return this;
    }

    public ThingSet reIndexThing(Thing t) {
        removeThing(t);
        addThing(t);
        return this;
    }

    public boolean belongs(Thing t) {
        return indexName.getKeys().contains(t.getName());
    }

    public Thing getThing(String name) {
        if (indexName.getKeys().contains(name)) {
            return indexName.getValues(name).get(0);
        } else {
            return null;
        }
    }

    public ArrayList<Thing> getAllThings() {
        return indexName.getAllValues();
    }

    public ArrayList <Thing> splitListByType(String type)  {
        ArrayList<Thing> res = new ArrayList();
        for (String stype : indexType.getKeys()){
            if (getOntology().matchTypes(stype, type))
                res.addAll(this.indexType.getValues(stype));
        }
        return res;
    }
    
    public ArrayList <Thing> splitListByBelong(String type)  {
        return this.indexBelong.getValues(type);
    }
    
    public ThingSet splitSetByType(String type) {
        ThingSet res = new ThingSet();
        res.setOntology(this.getOntology());
        res.setTypeFilter(type);
        for (Thing t : indexName.getAllValues()) {
            res.addThing(t);
        }
        return res;
    }

    public JsonObject toJson() {
        JsonObject jsc;
        JsonArray jsares = new JsonArray();
        for (Thing t : indexName.getAllValues()) {
            jsares.add(t.toJson());
        }
        if (getTypeFilter().length() > 0) {
            return new JsonObject().add(getTypeFilter(), jsares);
        } else {
            return new JsonObject().add(getOntology().getRootType(), jsares);
        }
    }
}
