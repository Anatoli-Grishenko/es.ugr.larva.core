/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import world.Thing;
import world.ThingSet;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Scheduler {

    protected Layout2D Map;
    protected ThingSet Deliveries, Resources, Order;

    public Scheduler() {
        this.Map = new Layout2D();
        this.Deliveries = new ThingSet();
        this.Resources = new ThingSet();
        this.Order = new ThingSet();
    }

    public Layout2D getMap() {
        return Map;
    }

    public void setMap(Layout2D Map) {
        this.Map = Map;
    }

    public ThingSet getDeliveries() {
        return Deliveries;
    }

    public void setDeliveries(ThingSet Deliveries) {
        this.Deliveries = Deliveries;
    }

    public ThingSet getResources() {
        return Resources;
    }

    public void setResources(ThingSet Resources) {
        this.Resources = Resources;
    }

    public ThingSet getOrder() {
        return Order;
    }

    public void setOrder(ThingSet Order) {
        this.Order = Order;
    }

    public TimeTable solveQuick() {
        TimeTable res = new TimeTable();
        String csource, cdestination;
        Assignment aux;
        int minslack=25;
        boolean change = false;
        for (Thing tship : getResources().getAllThings()) {
            if (tship.isAvailable()) {
                aux = new Assignment(
                        tship.getName(),
                        (String) null,
                        "STARTING " + tship.getBelongsTo(),
                        tship.getBelongsTo(),
                        0,
                        1,0);
                res.addAssignment(aux);
            }
        }
        System.out.println(res.getSolution(null));
        Thing tship, mttship = null;
        int ncaptures, tcaptures;
        for (Thing tjedi : getOrder().getAllThings()) {
            change = true;
            ncaptures = 0;
            tcaptures = 0;
            while (tjedi.getCapacity() > 0) {
                cdestination = tjedi.getBelongsTo();
                System.out.println("Dealing with " + tjedi.toString());
                tship = null;
                csource = "";
                for (Thing wship : getResources().getAllThings()) {
                    if (!wship.getType().equals("MTT")) {
                        if (res.getLast(wship.getName()) != null
                                && getMap().isCompatible(wship, wship.getBelongsTo(), cdestination)) {
//                            csource = wship.getBelongsTo();
                            if (tship == null) {
                                tship = wship;
                            } else if (res.getLast(wship.getName()).getTend() < res.getLast(tship.getName()).getTend()) {
                                tship = wship;
                            }
                        }
                    }
                }
                if (tship == null) {
                    return null;
                }
                csource = tship.getBelongsTo();
                ///tship = getResources().getThing(res.getEarlierAssignment().getAgent());
                aux = new Assignment(
                        tship.getName(),
                        (String) null,
                        "MOVEIN " + cdestination,
                        csource,
                        0,
                        getMap().getDistance(csource, cdestination),0);
                res.addAssignment(aux);
                if (tcaptures == 0) {
                    aux = new Assignment(
                            tship.getName(),
                            (String) null,
                            "REQUEST MTT " + cdestination,
                            csource,
                            0,
                            1,0);
                    res.addAssignment(aux);
                }
                tship.setBelongsTo(cdestination);
                ncaptures = (int) (Math.min(tjedi.getCapacity(), tship.getMaxCapacity() - tship.getCapacity()));
                tcaptures += ncaptures;
                aux = new Assignment(
                        tship.getName(),
                        (String) null,
                        "CAPTURE " + ncaptures + " JEDI " + tjedi.getBelongsTo(),
                        tship.getBelongsTo(),
                        0,
                        minslack,ncaptures);
                tjedi.setCapacity(tjedi.getCapacity() - ncaptures);
                tship.setCapacity(tship.getCapacity() + ncaptures);
                res.addAssignment(aux);
                if (tjedi.getCapacity() == 0) {
                    aux = new Assignment(
                            tship.getName(),
                            (String) null,
                            "CANCEL MTT " + cdestination,
                            csource,
                            0,
                            1,0);
                    res.addAssignment(aux);
                    tcaptures = 0;
                }

                if (tship.getCapacity() == tship.getMaxCapacity()) {
                    csource = cdestination;
                    cdestination = "Hartley";
                    aux = new Assignment(
                            tship.getName(),
                            (String) null,
                            "MOVEIN " + cdestination,
                            csource,
                            0,
                            getMap().getDistance(csource, cdestination),0);
                    res.addAssignment(aux);
                    tship.setBelongsTo(cdestination);
                    aux = new Assignment(
                            tship.getName(),
                            (String) null,
                            "TRANSFER DEST",
                            csource,
                            0,
                            minslack,(int)tship.getCapacity());
                    res.addAssignment(aux);
                    tship.setCapacity(0);
                }
            }
        }
        for (Thing wship
                : getResources()
                        .getAllThings()) {
            if (wship.getCapacity() > 0) {
                csource = wship.getBelongsTo();
                cdestination = "Hartley";
                aux = new Assignment(
                        wship.getName(),
                        (String) null,
                        "MOVEIN " + cdestination,
                        csource,
                        0,
                        getMap().getDistance(csource, cdestination),0);
                res.addAssignment(aux);
                wship.setBelongsTo(cdestination);
                aux = new Assignment(
                        wship.getName(),
                        (String) null,
                            "TRANSFER DEST",
                        csource,
                        0,
                        minslack,(int) (wship.getCapacity()));
                res.addAssignment(aux);
                wship.setCapacity(0);
            }
        }

        return res;
    }

    public TimeTable solveShortest() {
        TimeTable res = new TimeTable();
        String csource, cdestination;
        Assignment aux;
        int minslack=25;
        boolean change = false;
        for (Thing tship : getResources().getAllThings()) {
            if (tship.isAvailable()) {
                aux = new Assignment(
                        tship.getName(),
                        (String) null,
                        "STARTING " + tship.getBelongsTo(),
                        tship.getBelongsTo(),
                        0,
                        1,0);
                res.addAssignment(aux);
            }
        }
        System.out.println(res.getSolution(null));
        Thing tship, mttship = null;
        int ncaptures, tcaptures, wtbase, ttbase=0;
        for (Thing tjedi : getOrder().getAllThings()) {
            change = true;
            ncaptures = 0;
            tcaptures = 0;
            while (tjedi.getCapacity() > 0) {
                cdestination = tjedi.getBelongsTo();
//                System.out.println("Dealing with " + tjedi.toString());
                tship = null;
                csource = "";
                for (Thing wship : getResources().getAllThings()) {
                    if (!wship.getType().equals("MTT")) {
                        if (res.getLast(wship.getName()) != null
                                && getMap().isCompatible(wship, wship.getBelongsTo(), cdestination)) {
                            wtbase = res.getLast(wship.getName()).getTend();
//                            csource = wship.getBelongsTo();
                            if (tship == null) {
                                tship = wship;
                                ttbase = res.getLast(tship.getName()).getTend();
                            } else if (wtbase+getMap().getDistance(wship.getBelongsTo(), cdestination) < ttbase+getMap().getDistance(tship.getBelongsTo(), cdestination)) {
                                tship = wship;
                                ttbase = res.getLast(tship.getName()).getTend();
                            }
                        }
                    }
                }
                if (tship == null) {
                    return null;
                }
                csource = tship.getBelongsTo();
                ///tship = getResources().getThing(res.getEarlierAssignment().getAgent());
                aux = new Assignment(
                        tship.getName(),
                        (String) null,
                        "MOVEIN " + cdestination,
                        csource,
                        0,
                        getMap().getDistance(csource, cdestination),0);
                res.addAssignment(aux);
                if (tcaptures == 0) {
                    aux = new Assignment(
                            tship.getName(),
                            (String) null,
                            "REQUEST MTT " + cdestination,
                            csource,
                            0,
                            1,0);
                    res.addAssignment(aux);
                }
                tship.setBelongsTo(cdestination);
                ncaptures = (int) (Math.min(tjedi.getCapacity(), tship.getMaxCapacity() - tship.getCapacity()));
                tcaptures += ncaptures;
                aux = new Assignment(
                        tship.getName(),
                        (String) null,
                        "CAPTURE " + ncaptures + " JEDI " + tjedi.getBelongsTo(),
                        tship.getBelongsTo(),
                        0,
                        minslack,ncaptures);
                tjedi.setCapacity(tjedi.getCapacity() - ncaptures);
                tship.setCapacity(tship.getCapacity() + ncaptures);
                res.addAssignment(aux);
                if (tjedi.getCapacity() == 0) {
                    aux = new Assignment(
                            tship.getName(),
                            (String) null,
                            "CANCEL MTT " + cdestination,
                            csource,
                            0,
                            1,0);
                    res.addAssignment(aux);
                    tcaptures = 0;
                }

                if (tship.getCapacity() == tship.getMaxCapacity()) {
                    csource = cdestination;
                    cdestination = "Hartley";
                    aux = new Assignment(
                            tship.getName(),
                            (String) null,
                            "MOVEIN " + cdestination,
                            csource,
                            0,
                            getMap().getDistance(csource, cdestination),0);
                    res.addAssignment(aux);
                    tship.setBelongsTo(cdestination);
                    aux = new Assignment(
                            tship.getName(),
                            (String) null,
                            "TRANSFER DEST",
                            csource,
                            0,
                            minslack,(int)tship.getCapacity());
                    res.addAssignment(aux);
                    tship.setCapacity(0);
                }
            }
        }
        for (Thing wship
                : getResources()
                        .getAllThings()) {
            if (wship.getCapacity() > 0) {
                csource = wship.getBelongsTo();
                cdestination = "Hartley";
                aux = new Assignment(
                        wship.getName(),
                        (String) null,
                        "MOVEIN " + cdestination,
                        csource,
                        0,
                        getMap().getDistance(csource, cdestination),0);
                res.addAssignment(aux);
                wship.setBelongsTo(cdestination);
                aux = new Assignment(
                        wship.getName(),
                        (String) null,
                            "TRANSFER DEST",
                        csource,
                        0,
                        minslack,(int) (wship.getCapacity()));
                res.addAssignment(aux);
                wship.setCapacity(0);
            }
        }

        return res;
    }

    public TimeTable solveMTT() {
        TimeTable res = new TimeTable();
        String csource, cdestination;
        Assignment aux;
        boolean change = false;
        for (Thing tship : getResources().getAllThings()) {
            if (tship.isAvailable()) {
                aux = new Assignment(
                        tship.getName(),
                        (String) null,
                        "STARTING " + tship.getBelongsTo(),
                        tship.getBelongsTo(),
                        0,
                        1,0);
                res.addAssignmentMTT(aux);
            }
        }
        System.out.println(res.getSolution(null));
        Thing tship, mttship = null;
        int ncaptures, tcaptures;
        for (Thing tjedi : getOrder().getAllThings()) {
            change = true;
            ncaptures = 0;
            tcaptures = 0;
            while (tjedi.getCapacity() > 0) {
                cdestination = tjedi.getBelongsTo();
                System.out.println("Dealing with " + tjedi.toString());
                tship = null;
                csource = "";
                for (Thing wship : getResources().getAllThings()) {
                    if (!wship.getType().equals("MTT")) {
                        if (res.getLast(wship.getName()) != null
                                && getMap().isCompatible(wship, wship.getBelongsTo(), cdestination)) {
//                            csource = wship.getBelongsTo();
                            if (tship == null) {
                                tship = wship;
                            } else if (res.getLast(wship.getName()).getTend() < res.getLast(tship.getName()).getTend()) {
                                tship = wship;
                            }
                        }
                    }
                }
                if (tship == null) {
                    return null;
                }
                csource = tship.getBelongsTo();
                ///tship = getResources().getThing(res.getEarlierAssignment().getAgent());
                aux = new Assignment(
                        tship.getName(),
                        (String) null,
                        "MOVEIN " + cdestination,
                        csource,
                        0,
                        getMap().getDistance(csource, cdestination),0);
                res.addAssignmentMTT(aux);
                tship.setBelongsTo(cdestination);


                if (tcaptures == 0) {
                    aux = new Assignment(
                            tship.getName(),
                            (String) null,
                            "REQUEST MTT " + cdestination,
                            csource,
                            0,
                            1,0);
                    res.addAssignmentMTT(aux);
                }
//
                mttship=null;
                for (Thing wship : getResources().getAllThings()) {
                    if (wship.getType().equals("MTT")) {
                        if (res.getLast(wship.getName()) != null) {
//                            csource = wship.getBelongsTo();
                            if (mttship == null) {
                                mttship = wship;
                            } else if (res.getLast(wship.getName()).getTend() < res.getLast(mttship.getName()).getTend()) {
                                mttship = wship;
                            }
                        }
                    }
                }
                csource = mttship.getBelongsTo();
                ///tship = getResources().getThing(res.getEarlierAssignment().getAgent());
                aux = new Assignment(
                        mttship.getName(),
                        (String) null,
                        "MOVEIN " + cdestination,
                        csource,
                        0,
                        getMap().getDistance(csource, cdestination),0);
                res.addAssignmentMTT(aux);
                mttship.setBelongsTo(cdestination);



                ncaptures = (int) (Math.min(tjedi.getCapacity(), tship.getMaxCapacity() - tship.getCapacity()));
                tcaptures += ncaptures;
                aux = new Assignment(
                        tship.getName(),
                        mttship.getName(),
                        "CAPTURE " + ncaptures + " JEDI " + tjedi.getBelongsTo(),
                        tship.getBelongsTo(),
                        0,
                        20,ncaptures);
                tjedi.setCapacity(tjedi.getCapacity() - ncaptures);
                tship.setCapacity(tship.getCapacity() + ncaptures);
                res.addAssignmentMTT(aux);
                if (tjedi.getCapacity() == 0) {
                    aux = new Assignment(
                            tship.getName(),
                            (String) null,
                            "CANCEL MTT " + cdestination,
                            csource,
                            0,
                            1,0);
                    res.addAssignmentMTT(aux);
                    tcaptures = 0;
                }

                if (tship.getCapacity() == tship.getMaxCapacity()) {
                    csource = cdestination;
                    cdestination = "Hartley";
                    aux = new Assignment(
                            tship.getName(),
                            (String) null,
                            "MOVEIN " + cdestination,
                            csource,
                            0,
                            getMap().getDistance(csource, cdestination),0);
                    res.addAssignmentMTT(aux);
                    tship.setBelongsTo(cdestination);
                    aux = new Assignment(
                            tship.getName(),
                            (String) null,
                            "TRANSFER DEST",
                            csource,
                            0,
                            (int) (tship.getCapacity())*5,(int)tship.getCapacity());
                    res.addAssignmentMTT(aux);
                    tship.setCapacity(0);
                }
            }
        }
        for (Thing wship
                : getResources()
                        .getAllThings()) {
            if (wship.getCapacity() > 0) {
                csource = wship.getBelongsTo();
                cdestination = "Hartley";
                aux = new Assignment(
                        wship.getName(),
                        (String) null,
                        "MOVEIN " + cdestination,
                        csource,
                        0,
                        getMap().getDistance(csource, cdestination),0);
                res.addAssignmentMTT(aux);
                wship.setBelongsTo(cdestination);
                aux = new Assignment(
                        wship.getName(),
                        (String) null,
                            "TRANSFER DEST",
                        csource,
                        0,
                        (int) (wship.getCapacity()),(int) (wship.getCapacity()));
                res.addAssignmentMTT(aux);
                wship.setCapacity(0);
            }
        }

        return res;
    }
}
