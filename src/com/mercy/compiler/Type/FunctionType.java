package com.mercy.compiler.Type;

import com.mercy.compiler.Utility.InternalError;

import java.util.List;

/**
 * Created by mercy on 17-3-20.
 */
public class FunctionType extends Type {
    private Type returnType;
    private List<Type> paramTypes;

    public FunctionType(Type ret, List<Type> paramTypes) {
        returnType = ret;
        paramTypes = paramTypes;
    }

    @Override
    public long alignment() {
        throw new InternalError("FunctionType#alignment called");
    }

    @Override
    public long size() {
        throw new InternalError("FunctionType#size called");
    }

    public Type returnType() {
        return returnType;
    }

    public List<Type> paramTypes() {
        return paramTypes;
    }

    @Override
    public boolean isFunction() {
        return true;
    }

    @Override
    public boolean isCallable() {
        return true;
    }

    @Override
    public String toString() {
        String sep = "";
        StringBuilder buf = new StringBuilder();
        buf.append(returnType.toString());
        buf.append("(");
        for (Type t : paramTypes) {
            buf.append(sep);
            buf.append(t.toString());
            sep = ", ";
        }
        buf.append(")");
        return buf.toString();
    }
}
