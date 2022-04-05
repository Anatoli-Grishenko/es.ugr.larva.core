/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import map2D.Map2DColor;
import world.Perceptor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class PolarSurface {

    protected SimpleVector3D center;
    protected int radius;
    protected HashMap<Integer, ArrayList<SimpleVector3D>> pSurface;

    public PolarSurface(SimpleVector3D c) {
        center = c.clone();
        pSurface = new HashMap();
        radius = 1;
        pSurface.put(radius, new ArrayList());
        pSurface.get(radius).add(center);
    }

    public PolarSurface setRadius(int n) {
        SimpleVector3D svAux;
        ArrayList<SimpleVector3D> previousLevel;
        if (n == 1) {
            return this;
        } else {
            setRadius(n - 1);
            pSurface.put(n, new ArrayList());
            previousLevel = pSurface.get(n - 1);
            svAux = previousLevel.get(0);
            pSurface.get(n).add(svAux.myLeft());
            for (int i = 0; i < previousLevel.size(); i++) {
                svAux = previousLevel.get(i);
                if (i < previousLevel.size() / 2) {
                    pSurface.get(n).add(svAux.myFrontLeft());
                } else if (i == previousLevel.size() / 2) {
                    pSurface.get(n).add(svAux.myFrontLeft());
                    pSurface.get(n).add(svAux.myFront());
                    pSurface.get(n).add(svAux.myFrontRight());
                } else {
                    pSurface.get(n).add(svAux.myFrontRight());
                }
            }
            pSurface.get(n).add(svAux.myRight());
            return this;
        }
    }

    public int getNLevels() {
        return pSurface.keySet().size();
    }

    public ArrayList<SimpleVector3D> getLevel(int level) {
        if (1 <= level && level <= getNLevels()) {
            return pSurface.get(level);
        } else {
            return null;
        }
    }

    public Map2DColor applyRelativeTo(Map2DColor m) {
        SimpleVector3D sv;
        Map2DColor res = new Map2DColor(this.getNLevels(), this.getNLevels());
        for (int level = 1; level <= this.getNLevels(); level++) {
            for (int x = 0; x < this.getLevel(level).size(); x++) {
                sv = this.getLevel(level).get(x);
                res.setColor(sv.getSource().getXInt() - center.getSource().getXInt() + getNLevels() / 2,
                        sv.getSource().getYInt() - center.getSource().getYInt() + getNLevels() / 2,
                        m.getColor(sv));
            }
        }
        return res;
    }

    public Map2DColor applyAbsoluteTo(Map2DColor m) {
        SimpleVector3D svRes, svFrom;
        ArrayList<SimpleVector3D> thisLevel;
        Map2DColor res = new Map2DColor(2 * this.getNLevels() - 1, this.getNLevels());
        for (int level = 0; level < this.getNLevels(); level++) {
            thisLevel = pSurface.get(level + 1);
            svRes = new SimpleVector3D(getNLevels() - level - 1, getNLevels() - 1, SimpleVector3D.N);
//            svFrom = thisLevel.get(0);
//            res.setColor(svRes, m.getColor(svFrom));
//            System.out.println("Level: "+(level+1)+"  "+thisLevel.size()+" points");
            for (int x = 0; x < thisLevel.size(); x++) {
                svFrom = thisLevel.get(x);
//                System.out.println(svFrom.getSource()+"->"+svRes.getSource()+"...");
                res.setColor(svRes, m.getColor(svFrom));
                if (x < level) {
                    svRes = svRes.myFront();
                } else if (x < thisLevel.size() - level - 1) {
                    svRes = svRes.myRight();
                } else {
                    svRes = svRes.myRear();
                }
            }
//            System.out.println();
        }
        return res;
    }

    public Map2DColor applyPolarTo(Map2DColor m) {
        SimpleVector3D svRes, svFrom;
        ArrayList<SimpleVector3D> thisLevel;
        Map2DColor res = new Map2DColor((this.getNLevels() - 1) * 4 + 1, this.getNLevels());
        for (int level = 0; level < this.getNLevels(); level++) {
            thisLevel = pSurface.get(level + 1);
            for (int x = 0; x < thisLevel.size(); x++) {
                svFrom = thisLevel.get(x);
                res.setLevel(x, level, m.getRawLevel(svFrom));
            }
        }
        return res;
    }

    public int[][] applyPolarTo(int m[][]) {
        SimpleVector3D svRes, svFrom;
        ArrayList<SimpleVector3D> thisLevel;
        int res[][] = new int[(this.getNLevels() - 1) * 4 + 1][this.getNLevels()];
        for (int[] row : res) {
            Arrays.fill(row, Perceptor.NULLREAD);
        }
        for (int level = 0; level < this.getNLevels(); level++) {
            thisLevel = pSurface.get(level + 1);
            for (int x = 0; x < thisLevel.size(); x++) {
                svFrom = thisLevel.get(x);
                res[x][level] = m[svFrom.getSource().getXInt()][svFrom.getSource().getYInt()];
            }
        }
        return res;
    }

    public int[][] applyAbsoluteTo(int m[][]) {
        SimpleVector3D sv;
        int res[][] = new int[2 * this.getNLevels() - 1][2 * this.getNLevels() - 1];
        for (int[] row : res) {
            Arrays.fill(row, Perceptor.NULLREAD);
        }
        for (int level = 0; level < this.getNLevels(); level++) {
            for (int x = 0; x < this.getLevel(level + 1).size(); x++) {
                sv = this.getLevel(level + 1).get(x);
                res[sv.getSource().getXInt()][sv.getSource().getYInt()]
                        = m[sv.getSource().getXInt()][sv.getSource().getYInt()];
            }
        }
        return res;
    }

    public int[][] applyRelativeTo(int m[][]) {
        SimpleVector3D svRes, svFrom;
        ArrayList<SimpleVector3D> thisLevel;
        int res[][] = new int[2 * this.getNLevels() - 1][this.getNLevels()];
        for (int[] row : res) {
            Arrays.fill(row, Perceptor.NULLREAD);
        }
        for (int level = 0; level < this.getNLevels(); level++) {
            thisLevel = pSurface.get(level + 1);
            svRes = new SimpleVector3D(getNLevels() - level - 1, getNLevels() - 1, SimpleVector3D.N);
//            svFrom = thisLevel.get(0);
//            res.setColor(svRes, m.getColor(svFrom));
//            System.out.println("Level: "+(level+1)+"  "+thisLevel.size()+" points");
            for (int x = 0; x < thisLevel.size(); x++) {
                svFrom = thisLevel.get(x);
//                System.out.println(svFrom.getSource()+"->"+svRes.getSource()+"...");
                res[svRes.getSource().getXInt()][svRes.getSource().getYInt()]
                        = m[svFrom.getSource().getXInt()][svFrom.getSource().getYInt()];
                if (x < level) {
                    svRes = svRes.myFront();
                } else if (x < thisLevel.size() - level - 1) {
                    svRes = svRes.myRight();
                } else {
                    svRes = svRes.myRear();
                }
            }
//            System.out.println();
        }
        return res;
    }

}
