package com.mercy.compiler.Type;

import com.mercy.compiler.Entity.Scope;
import com.mercy.compiler.Utility.LibFunction;

import static com.mercy.compiler.Utility.LibFunction.LIB_PREFIX;

/**
 * Created by mercy on 17-3-18.
 */
public class ArrayType extends Type {
    private Type baseType;
    static final int DEFAULT_POINTER_SIZE = 8;

    static private Scope scope;
    static private ArrayType magicArray = new ArrayType(nullType); // for matching "this" pointer in function call

    static public void initializeBuiltinFunction() {
        scope = new Scope(true);
        scope.insert(new LibFunction(integerType, "size", LIB_PREFIX + "array_size", new Type[]{magicArray}).getEntity());
    }

    static public Scope scope() {
        return scope;
    }

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

    public Type deepType() {
        return baseType instanceof ArrayType ? ((ArrayType) baseType).deepType() : baseType;
    }

    @Override
    public boolean isCompatible(Type other) {
        if (other.isNull())    return true;
        if (!other.isArray() ) return false;
        return baseType.isCompatible(((ArrayType)other).baseType);
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isHalfComparable() {
        return true;
    }

    @Override
    public int size() {
        return DEFAULT_POINTER_SIZE;
    }

    @Override
    public String toString() {
        return baseType.toString() + "[]";
    }
}