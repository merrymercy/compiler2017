package com.mercy.compiler.Type;

/**
 * Created by mercy on 17-3-18.
 */
public class ArrayType extends Type {
    private Type baseType;
    static final long DEFAULT_POINTER_SIZE = 4;

    public ArrayType(Type baseType) {
        this.baseType = baseType;
    }

    public ArrayType(Type baseType, int dimension) {
        if (dimension == 1) {
            this.baseType = baseType;
        } else {
            this.baseType = new ArrayType(baseType, dimension - 1);
        }
    }

    public Type baseType() {
        return baseType;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public long size() {
        return DEFAULT_POINTER_SIZE;
    }

    @Override
    public String toString() {
        return baseType.toString() + "[]";
    }
}