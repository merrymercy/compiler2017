package com.mercy.compiler.AST;

/**
 * Created by mercy on 17-3-18.
 */
abstract public class Node {
    public Node() {
    }

    protected boolean isOutputIrrelevant = false;

    public boolean outputIrrelevant() {
        return isOutputIrrelevant;
    }
    public void setOutputIrrelevant(boolean outputIrrelevant) {
        isOutputIrrelevant = outputIrrelevant;
    }

    abstract public Location location();
}

