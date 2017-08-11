package com.maxdemarzi.quine;

import java.util.ArrayList;
import java.util.List;

/*
 * Stores implicants in the form where
 */
public class Implicant {

    private long myMSB;
    private long myLSB;
    private int myNumVars;
    private List<Long> minterms;
    private List<Long> dontcares;

    public Implicant(long newMSB, long newLSB, int numVars) {
        myMSB = newMSB;
        myLSB = newLSB;
        myNumVars = numVars;
        minterms = new ArrayList<Long>();
        dontcares = new ArrayList<Long>();
    }

    public Implicant(long minterm, int numVars, boolean dontcare) {
        myMSB = minterm ^ BooleanExpression.maxVal;
        myLSB = BooleanExpression.maxVal & (minterm | (BooleanExpression.maxVal << numVars));
        myNumVars = numVars;
        minterms = new ArrayList<>();
        dontcares = new ArrayList<>();
        if (dontcare)
            dontcares.add(minterm);
        else
            minterms.add(minterm);
    }

    public void printList() {
        System.out.print("Minterms: ");
        for (int i = 0; i < minterms.size(); i++) {
            System.out.print(Long.toString(minterms.get(i)) + ", ");
        }
        System.out.print("Dontcare: ");
        for (int i = 0; i < dontcares.size(); i++) {
            System.out.print(Long.toString(dontcares.get(i)) + ", ");
        }
        System.out.println("");
    }

    public void printSB() {
        System.out.println("MSB is " + myMSB + " LSB is " + myLSB);
    }

    public long getMSB() {
        return myMSB;
    }

    public long getLSB() {
        return myLSB;
    }

    public int getNumVars() {
        return myNumVars;
    }

    public List<Long> getMinterms() {
        return minterms;
    }

    public List<Long> getdontcares() {
        return dontcares;
    }

    public void mergeMinterms(List<Long> min1, List<Long> min2, List<Long> dont1, List<Long> dont2) {
        minterms.addAll(min1);
        minterms.addAll(min2);
        dontcares.addAll(dont1);
        dontcares.addAll(dont2);
    }

    public boolean equals(Implicant imp) {
        return (imp.getLSB() == this.myLSB) &&
                (imp.getNumVars() == this.myNumVars) &&
                (imp.getMSB() == this.myMSB);
    }

    public String getVerilogExpression() {
        StringBuilder expr = new StringBuilder("");

        expr.append("(");

        boolean first = true;
        for (int i = 0; i < myNumVars; i++) {
            long tempMSB = myMSB & (1 << i);
            long tempLSB = myLSB & (1 << i);
            char alphabetVal = BooleanExpression.alphabet.charAt(i);

            if (Long.bitCount(tempMSB) == 1 && Long.bitCount(tempLSB) == 0) {
                if (first) {
                    first = false;
                } else {
                    expr.append("&");
                }
                expr.append("(~" + alphabetVal + ")");
            }
            if (Long.bitCount(tempMSB) == 0 && Long.bitCount(tempLSB) == 1) {
                if (first) {
                    first = false;
                } else {
                    expr.append("&");
                }
                expr.append(alphabetVal);
            }
        }
        expr.append(")");
        return expr.toString();
    }

    public String getPathExpression() {
        StringBuilder expr = new StringBuilder("");

        boolean first = true;
        for (int i = 0; i < myNumVars; i++) {
            long tempMSB = myMSB & (1 << i);
            long tempLSB = myLSB & (1 << i);

            if (Long.bitCount(tempMSB) == 1 && Long.bitCount(tempLSB) == 0) {
                if (first) {
                    first = false;
                } else {
                    expr.append("&");
                }
                expr.append(i);
            }
            if (Long.bitCount(tempMSB) == 0 && Long.bitCount(tempLSB) == 1) {
                if (first) {
                    first = false;
                } else {
                    expr.append("&");
                }
                expr.append(i);
            }
        }
        return expr.toString();
    }

}