package com.mercy.compiler.AST;

/**
 * Created by mercy on 17-3-18.
 */
abstract public class Node {
    public Node() {
    }

    protected boolean isOutputIrrevelant = false;

    public boolean outputIrrelevant() {
        return isOutputIrrevelant;
    }

    public void setOutputIrrelevant(boolean outputIrrelevant) {
        isOutputIrrevelant = outputIrrelevant;
    }

    abstract public Location location();
}

