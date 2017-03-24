package com.mercy.compiler.Type;

/**
 * Created by mercy on 17-3-18.
 */
public abstract class Type {
    static final public long sizeUnknown = -1;
    protected boolean isResolved = false;

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

    // Ability methods (unary)
    public boolean isScalar() {
        return false;
    }
    public boolean isCallable() {
        return false;
    }

    public boolean isResolved() { return isResolved; }
    public void setResolved() { isResolved = true; }

    // Ability methods (binary)
    // abstract public boolean isCompatible(Type other);
    // abstract public boolean isCastableTo(Type target);

    abstract public long size();

    public long allocSize() {
        return size();
    }
    public long alignment() {
        return allocSize();
    }

    public FunctionType getFunctionType() {
        return (FunctionType)this;
    }
    public ClassType getClassType() {
        return (ClassType)this;
    }
}
