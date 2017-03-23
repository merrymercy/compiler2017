package com.mercy.compiler.Type;

/**
 * Created by mercy on 17-3-18.
 */
public class VoidType extends Type {
    public VoidType() {
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public boolean isVoid() {
        return true;
    }

    public String toString() {
        return "void";
    }
}
