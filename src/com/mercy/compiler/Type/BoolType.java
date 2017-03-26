package com.mercy.compiler.Type;

/**
 * Created by mercy on 17-3-18.
 */
public class BoolType extends Type {
    static final long DEFAULT_SIZE = 1;

    @Override
    public boolean isBool() {
        return true;
    }

    @Override
    public boolean isCompatible(Type other) {
        return other.isBool();
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
        return "bool";
    }
}
