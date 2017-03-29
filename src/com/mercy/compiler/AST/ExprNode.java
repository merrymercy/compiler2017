package com.mercy.compiler.AST;

import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-18.
 */
abstract public class ExprNode extends Node {
    private boolean isAssignable = false;
    public ExprNode() {
        super();
    }

    abstract public Type type();

    public long allocSize() { return type().allocSize(); }

    public boolean isConstant() {
        return false;
    }
    public boolean isParameter() {
        return false;
    }

    public boolean isLvalue() {
        return false;
    }
    public boolean isAssignable() {
        return isAssignable;
    }
    public boolean isLoadable() {
        return false;
    }

    public void setAssignable(boolean isAssignable) {
        this.isAssignable = isAssignable;
    }

    abstract public <S,E> E accept(ASTVisitor<S,E> visitor);
}
