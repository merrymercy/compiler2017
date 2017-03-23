package com.mercy.compiler.Type;

/**
 * Created by mercy on 17-3-18.
 */
public class IntegerType extends Type {
    static final long DEFAULT_SIZE = 4;

    @Override
    public boolean isInteger() {
        return true;
    }

    @Override
    public boolean isScalar() {
        return true;
    }

    @Override
    public long size() {
        return DEFAULT_SIZE;
    }

    @Override
    public String toString() {
        return "int";
    }
}
