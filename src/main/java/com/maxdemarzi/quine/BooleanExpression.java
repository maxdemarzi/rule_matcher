package com.maxdemarzi.quine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BooleanExpression {

    public static final long maxVal = -1;
    public static final String alphabet = "abcdefghijklmnopqrstuvwxyz";
    private static long tempMSB;
    private static long tempLSB;
    private static int bitCountMSB;
    private static int bitCountLSB;
    private List<Implicant> implicantList;
    private List<Implicant> dontcareList;
    private List<Long> mintermsNeededToCover;
    private List<Long> dontcaresList;
    private int myNumVars;
    private ArrayList<BitVector> row;
    private ArrayList<BitVector> col;
    private ArrayList<Implicant> primeImplicant;
    private ArrayList<ArrayList<BitVector>> petrickList;
    private ArrayList<Implicant> nessesaryImplicant;
    private List<Implicant> tempImplicantList;
    private Map<Integer, String> varMapping;

    public BooleanExpression(String formula) {
        ExpressionedTruthTable ett = new ExpressionedTruthTable(formula);
        ett.compute();
        this.varMapping = ett.getMapping();
        initBooleanExpression(ett.variables());
        for (Long minterm : ett.minTerms()) {
            implicantList.add(new Implicant(minterm, ett.variables(), false));
            mintermsNeededToCover.add(minterm);
        }
    }

    public BooleanExpression(ArrayList<Long> minterms, ArrayList<Long> dontcares, int numVars) {
        initBooleanExpression(numVars);
        for (Long minterm : minterms) {
            implicantList.add(new Implicant(minterm, numVars, false));
            mintermsNeededToCover.add(minterm);
        }

        for (Long dontcare : dontcares) {
            dontcaresList.add(dontcare);
            dontcareList.add(new Implicant(dontcare, numVars, true));
        }
    }

    private void init(ArrayList<Long> minterms, int numVars) {
        initBooleanExpression(numVars);
        for (Long minterm : minterms) {
            implicantList.add(new Implicant(minterm, numVars, false));
            mintermsNeededToCover.add(minterm);
        }
    }

    private void initBooleanExpression(int numVars) {
        implicantList = new ArrayList<>();
        dontcareList = new ArrayList<>();
        myNumVars = numVars;
        mintermsNeededToCover = new ArrayList<>();
        dontcaresList = new ArrayList<>();
        primeImplicant = new ArrayList<>();
        petrickList = new ArrayList<>();
        nessesaryImplicant = new ArrayList<>();
        tempImplicantList = new ArrayList<>();
    }

    public List<Implicant> getImplicantList() {
        return implicantList;
    }


    public List<String> getPathExpressions() {
        List<String> paths = new ArrayList<>();
        for (Implicant implicant: this.implicantList) {
            StringBuilder expr = new StringBuilder("");

            boolean first = true;
            for (int i = 0; i < myNumVars; i++) {
                long tempMSB = implicant.getMSB() & (1 << i);
                long tempLSB = implicant.getLSB() & (1 << i);

                if (Long.bitCount(tempMSB) == 1 && Long.bitCount(tempLSB) == 0) {
                    if (first) {
                        first = false;
                    } else {
                        expr.append("!");
                    }
                    expr.append(varMapping.get(i));
                }
                if (Long.bitCount(tempMSB) == 0 && Long.bitCount(tempLSB) == 1) {
                    if (first) {
                        first = false;
                    } else {
                        expr.append("&");
                    }
                    expr.append(varMapping.get(i));
                }
            }
            paths.add(expr.toString());
        }
        return paths;
    }

    public boolean differBySingleVariable(Implicant imp1, Implicant imp2) {
        tempMSB = imp1.getMSB() ^ imp2.getMSB(); // XOR together MSB to get 1 at a certain place
        tempLSB = imp1.getLSB() ^ imp2.getLSB(); // XOR together LSB to get 1 at a certain place
        bitCountMSB = Long.bitCount(tempMSB); // Get number of 1's in MSB
        bitCountLSB = Long.bitCount(tempLSB); // Get number of 1's in LSB
        return (bitCountMSB == 1 && bitCountLSB == 1 && tempMSB == tempLSB); //Compare and return that MSB and LSB only contain one 1
    }

    public Implicant merge(Implicant imp1, Implicant imp2) {
        tempMSB = imp1.getMSB() | imp2.getMSB(); // XOR together MSB
        tempLSB = imp1.getLSB() | imp2.getLSB(); // XOR together LSB
        Implicant newImp = new Implicant(tempMSB, tempLSB, myNumVars); //create new Implicant list
        newImp.mergeMinterms(imp1.getMinterms(), imp2.getMinterms(), imp1.getdontcares(), imp2.getdontcares()); //create new implicant for next group using minterms and Don't cares
        return newImp;
    }

    /**
     * Used to check if list contains implicant
     *
     * @param impList: List to look through
     * @param imp:     Variable to check if it's in list
     */
    public boolean containsImplicant(ArrayList<Implicant> impList, Implicant imp) {
        for (int i = 0; i < impList.size(); i++) {
            if (imp.equals(impList.get(i)))
                return true;
        }
        return false;
    }

    /**
     * Method to replace implicants with prime implicants using minterms and don't cares
     */
    public void doTabulationMethod() {
        ArrayList<ArrayList<ArrayList<Implicant>>> tabulationList = new ArrayList<>(myNumVars + 1); //create new Arraylist to store implicants
        //Creating a list within a list within a list to represent group, subcubes and implicants within both, respectively
        for (int i = 0; i < myNumVars + 1; i++) {
            tabulationList.add(new ArrayList<ArrayList<Implicant>>());
            for (int j = 0; j < myNumVars + 1; j++) {
                tabulationList.get(i).add(new ArrayList<Implicant>());
            }
        }
        boolean completed;
        Implicant prev;
        Implicant next;
        int bitCount;

        for (int i = 0; i < mintermsNeededToCover.size(); i++) {
            bitCount = Long.bitCount(mintermsNeededToCover.get(i));
            tabulationList.get(0).get(bitCount).add(implicantList.get(i));
        }

        //Counting and displaying number of cubes
        Implicant temp;
        for (int i = 0; i < tabulationList.size() - 1; i++) {
            completed = true;
            //Select subcube based on how many ones there are
            for (int j = 0; j < tabulationList.get(i).size() - 1; j++) {
                //Select Subcube within group to compare
                for (int k = 0; k < tabulationList.get(i).get(j).size(); k++) {
                    prev = tabulationList.get(i).get(j).get(k);
                    //Compare subcube with subcubes of adjacent group
                    for (int l = 0; l < tabulationList.get(i).get(j + 1).size(); l++) {
                        next = tabulationList.get(i).get(j + 1).get(l);
                        //check to make sure that numbers do not differ by more than one bit
                        if (differBySingleVariable(prev, next)) {
                            completed = false;
                            temp = merge(prev, next);
                            implicantList.remove(prev);
                            implicantList.remove(next);
                            if (!containsImplicant(tabulationList.get(i + 1).get(j), temp)) {
                                if (!temp.getMinterms().isEmpty()) {
                                    implicantList.add(temp);
                                }
                                tabulationList.get(i + 1).get(j).add(temp);
                            }
                        }
                    }
                }
            }
            if (completed)
                break;
        }
    }

    //Method to perform the QuineMcCluskey operation on a list of implicants
    public void doQuineMcCluskey() {
        tempImplicantList = implicantList;
        row = new ArrayList<>(implicantList.size());
        col = new ArrayList<>(mintermsNeededToCover.size());
        Implicant impTemp;
        BitVector bitVectorTemp;
        List<Long> minterms;

        //add bitvectors to row's with number of minterms
        for (int i = 0; i < implicantList.size(); i++) {
            row.add(new BitVector(mintermsNeededToCover.size()));
        }

        //add bitvectors to columns with size of implicants
        for (int i = 0; i < mintermsNeededToCover.size(); i++) {
            col.add(new BitVector(implicantList.size()));
        }
        //loop through implicantList to populate the comlumns and rows with where minterms and implicants are located
        for (int i = 0; i < implicantList.size(); i++) {
            impTemp = implicantList.get(i);
            minterms = impTemp.getMinterms();
            for (int j = 0; j < minterms.size(); j++) {
                row.get(i).set(mintermsNeededToCover.indexOf(minterms.get(j)));
                col.get(mintermsNeededToCover.indexOf(minterms.get(j))).set(i);
            }
        }

        int index;
        int count = 0;
        while (true) {
            count++;
            /*
			 * STEP 1!!!!!!!
			 * Find column lone implicants
			 */
            for (int i = 0; i < col.size(); i++) {
                if (col.get(i).getCardinality() == 1) {
                    count = 0;
                    index = col.get(i).findNeededImplicant();
                    col.get(i).unset(index);
                    if (!containsImplicant(primeImplicant, implicantList.get(index)))
                        primeImplicant.add(implicantList.get(index));
                    for (int j = 0; j < col.size(); j++) {
                        if (row.get(index).exists(j)) {
                            row.get(index).unset(j);
                            for (int k = 0; k < row.size(); k++) {
                                if (col.get(j).exists(k)) {
                                    col.get(j).unset(k);
                                    row.get(k).unset(j);
                                }
                            }

                        }
                    }
                }
            }
            if (count == 3)
                break;
            count++;
			/*
			 * STEP 2!!!!!!
			 * Find implicants in their own row and see if there are others in that column
			 */
            for (int i = 0; i < row.size() - 1; i++) {
                bitVectorTemp = row.get(i);
                for (int j = i + 1; j < row.size(); j++) {
                    if (!row.get(j).isZero() && !row.get(i).isZero()) {
                        if (bitVectorTemp.union(row.get(j)).equals(bitVectorTemp)) {
                            count = 0;
                            for (int k = 0; k < col.size(); k++) {
                                if (row.get(j).exists(k)) {
                                    row.get(j).unset(k);
                                    col.get(k).unset(j);
                                }
                            }
                        } else if (bitVectorTemp.union(row.get(j)).equals(row.get(j))) {
                            count = 0;
                            for (int k = 0; k < col.size(); k++) {
                                if (row.get(i).exists(k)) {
                                    row.get(i).unset(k);
                                    col.get(k).unset(i);
                                }
                            }

                        }
                    }

                }
            }
            if (count == 3)
                break;
            count++;
			/*
			 * STEP 3!!!!!!!!!
			 * Find implicants in their own column and see if there are others in that row
			 */
            for (int i = 0; i < col.size() - 1; i++) {
                bitVectorTemp = col.get(i);
                for (int j = i + 1; j < col.size(); j++) {
                    if (!col.get(j).isZero() && !col.get(i).isZero()) {
                        if (bitVectorTemp.union(col.get(j)).equals(col.get(j))) {
                            count = 0;
                            for (int k = 0; k < row.size(); k++) {
                                if (col.get(j).exists(k)) {
                                    col.get(j).unset(k);
                                    row.get(k).unset(j);
                                }
                            }
                        } else if (bitVectorTemp.union(col.get(j)).equals(bitVectorTemp)) {
                            count = 0;
                            for (int k = 0; k < row.size(); k++) {
                                if (col.get(i).exists(k)) {
                                    col.get(i).unset(k);
                                    row.get(k).unset(i);
                                }
                            }
                        }
                    }

                }
            }
            if (count == 3)
                break;
        }

		/*
		 * Adding Stuff
		 */
        for (int i = 0; i < primeImplicant.size(); i++)
            nessesaryImplicant.add(primeImplicant.get(i));
        for (int i = 0; i < row.size(); i++) {
            if (!row.get(i).isZero()) {
                primeImplicant.add(implicantList.get(i));
            }
        }
        implicantList = primeImplicant;
        for (int i = 0; i < col.size(); i++) {
            if (!col.get(i).isZero()) {
                ArrayList<BitVector> tempList = new ArrayList<BitVector>();
                for (int j = 0; j < col.size(); j++) {
                    if (col.get(i).exists(j)) {
                        BitVector tempVector = new BitVector(row.size());
                        tempVector.set(j);
                        tempList.add(tempVector);
                    }
                }
                petrickList.add(tempList); //Prepare list for Petrick Method
            }
        }
    }

    /**
     * Method to absorb sub-answers of an answer together
     */
    public ArrayList<BitVector> doAbsorption(ArrayList<BitVector> answers) {
        ArrayList<BitVector> absorved = new ArrayList<BitVector>();
        for (int i = 0; i < answers.size() - 1; i++) {
            for (int j = i + 1; j < answers.size(); j++) {
                if (answers.get(i).union(answers.get(j)).equals(answers.get(i))) {
                    if (!absorved.contains(answers.get(i))) {
                        absorved.add(answers.get(i));
                    }
                } else if (answers.get(i).union(answers.get(j)).equals(answers.get(j))) {
                    if (!absorved.contains(answers.get(j))) {
                        absorved.add(answers.get(j));
                    }
                }
            }
        }
        for (int i = 0; i < absorved.size(); i++)
            answers.remove(absorved.get(i));
        return answers;
    }

    public ArrayList<BitVector> multiply(ArrayList<BitVector> multiplicand, ArrayList<BitVector> multiplier) {
        ArrayList<BitVector> temp = new ArrayList<>();
        for (int i = 0; i < multiplicand.size(); i++) {
            for (int j = 0; j < multiplier.size(); j++) {
                temp.add(multiplicand.get(i).union(multiplier.get(j)));
            }
        }

        return temp;
    }

    /**
     * Method to perform Petrick's method to get the minimum product of sums solution from a list
     */
    public void doPetricksMethod() {
        if (petrickList.isEmpty())
            return; //end if list is empty
        ArrayList<BitVector> answers = new ArrayList<BitVector>(); //array to hold answers

        //Add answers from Petricks list to answers list that are Xor'ed
        for (int j = 0; j < petrickList.get(0).size(); j++) {
            for (int k = 0; k < petrickList.get(1).size(); k++) {
                answers.add(petrickList.get(0).get(j).union(petrickList.get(1).get(k)));
            }
        }

        //absorb answers together that are already covered
        for (int i = 2; i < petrickList.size(); i++) {
            answers = multiply(answers, petrickList.get(i));
            answers = doAbsorption(answers);

        }
        int min = 0;
        int index = 0;
        for (int i = 0; i < answers.size(); i++) {
            if (i == 0)
                min = answers.get(i).getCardinality();
            if (min > answers.get(i).getCardinality()) {
                min = answers.get(i).getCardinality();
                index = i;
            }
        }
        //add answers to list for minimal representation
        for (int i = 0; i < answers.get(index).getSize(); i++) {
            if (answers.get(index).exists(i))
                nessesaryImplicant.add(tempImplicantList.get(i));
        }
        implicantList = nessesaryImplicant;
    }

}
