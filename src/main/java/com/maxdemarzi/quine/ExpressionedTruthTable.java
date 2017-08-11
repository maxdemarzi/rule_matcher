package com.maxdemarzi.quine;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Literal;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

import java.util.*;
import java.util.regex.Pattern;

public class ExpressionedTruthTable {
    private static final Pattern p = Pattern.compile("\\d+");
    private final int numVariables;
    public boolean[] variables;
    private List<Boolean> rows;
    private int numRows;
    private Expression<String> expression;
    private Map<Integer, String> varMapping;
    private List<Integer> terms;

    public ExpressionedTruthTable(String formula) {
        Set<String> mySet = new HashSet<>(Arrays.asList(formula.replaceAll("[^-?0-9a-zA-Z]+", " ").split(" ")));
        mySet.remove("");

        varMapping = new HashMap<>();
        Iterator<String> it = mySet.iterator();
        int count = 0;
        while (it.hasNext()) {
            varMapping.put(count++, it.next());
        }

        numVariables = mySet.size();
        variables = new boolean[numVariables];
        numRows = ((int) Math.pow(2, variables.length));
        rows = new ArrayList<>(numRows);
        expression = ExprParser.parse(formula);
    }

    public void compute() {
        rows.clear();
        for (int i = 0; i < numRows; i++) {
            Map<String, Boolean> map = new HashMap<>();
            for (int j = variables.length - 1; j >= 0; j--) {
                variables[j] = (i / (int) Math.pow(2, j)) % 2 == 1;
                map.put(varMapping.get(j), (i / (int) Math.pow(2, j)) % 2 == 1);
            }
            Expression<String> assigned = RuleSet.assign(expression, map);
            Expression<String> isTrue = Literal.getTrue();
            boolean rowOutput = assigned.equals(isTrue);
            rows.add(rowOutput);
        }
    }

    public ArrayList<Long> minTerms() {
        ArrayList<Long> terms = new ArrayList<>();
        for (int i = 0; i < numRows; i++) {
            if (rows.get(i)) {
                terms.add((long) i);
            }
        }
        return terms;
    }

    public int variables() {
        return this.numVariables;
    }

    public Map<Integer, String> getMapping() {
        return this.varMapping;
    }
}
