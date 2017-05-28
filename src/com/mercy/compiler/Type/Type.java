package com.mercy.compiler.Type;

/**
 * Created by mercy on 17-3-18.
 */
abstract public class Type {
    static final public long sizeUnknown = -1;

    static public BoolType boolType = new BoolType();
    static public IntegerType integerType = new IntegerType();
    static public VoidType voidType = new VoidType();
    static public StringType stringType = new StringType();
    static public NullType nullType = new NullType();

    static public void initializeBuiltinType() {
        StringType.initializeBuiltinFunction();
        ArrayType.initializeBuiltinFunction();
    }

    public boolean isVoid() {
        return false;
    }
    public boolean isBool() {
        return false;
    }
    public boolean isInteger() {
        return false;
    }
    public boolean isString() {
        return false;
    }
    public boolean isArray() {
        return false;
    }
    public boolean isClass() {
        return false;
    }
    public boolean isFunction() {
        return false;
    }
    public boolean isNull() {
        return false;
    }

    // Ability methods
    public boolean isFullComparable() {
        return false;
    }
    public boolean isHalfComparable() {
        return false;
    } // can comparable w.r.t. '==' and '!='

    abstract public boolean isCompatible(Type other);

    abstract public int size();

    public long allocSize() {
        return size();
    }
}
