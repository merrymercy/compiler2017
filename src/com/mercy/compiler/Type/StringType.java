package com.mercy.compiler.Type;

/**
 * Created by mercy on 17-3-23.
 */
public class StringType extends Type {
    static final long DEFAULT_SIZE = 4;

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public long size() {
        return DEFAULT_SIZE;
    }

    @Override
    public String toString() {
        return "string";
    }
}
