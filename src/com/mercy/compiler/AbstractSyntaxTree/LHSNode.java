package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-18.
 */
abstract public class LHSNode extends ExprNode {
    protected Type type;

    @Override
    public Type type() {
        return type;
    }

    public void setType(Type t) {
        this.type = t;
    }

    public long allocSize() { return type.allocSize(); }

    @Override
    public boolean isLvalue() { return true; }

    @Override
    public boolean isAssignable() { return true; }
}
