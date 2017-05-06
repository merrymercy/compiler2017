package com.mercy.compiler.Type;

/**
 * Created by mercy on 17-3-18.
 */



public class IntegerType extends Type {
    static final int DEFAULT_SIZE = 8;

    @Override
    public boolean isInteger() {
        return true;
    }

    @Override
    public boolean isScalar() {
        return true;
    }

    @Override
    public boolean isCompatible(Type other) {
        return other.isInteger();
    }

    @Override
    public boolean isFullComparable() {
        return true;
    }

    @Override
    public boolean isHalfComparable() {
        return true;
    }

    @Override
    public int size() {
        return DEFAULT_SIZE;
    }

    @Override
    public String toString() {
        return "int";
    }
}
