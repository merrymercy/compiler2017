package com.mercy.compiler.Type;

import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Utility.InternalError;

/**
 * Created by mercy on 17-3-20.
 */
public class FunctionType extends Type {
    private String name;
    private FunctionEntity entity;

    public FunctionType(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public FunctionEntity entity() {
        return entity;
    }

    public void setEntity(FunctionEntity entity) {
        this.entity = entity;
        isResolved = true;
    }

    @Override
    public long alignment() {
        throw new InternalError("FunctionType#alignment called");
    }

    @Override
    public long size() {
        throw new InternalError("FunctionType#size called");
    }

    @Override
    public boolean isFunction() {
        return true;
    }

    @Override
    public boolean isCallable() {
        return true;
    }

    /*@Override
    public String toString() {
        String sep = "";
        StringBuilder buf = new StringBuilder();
        buf.append(entity.returnType().toString());
        buf.append("(");
        for (ParameterDefNode param : entity.params()) {
            buf.append(sep);
            buf.append(param.toString());
            sep = ", ";
        }
        buf.append(")");
        return buf.toString();
    }*/
}
