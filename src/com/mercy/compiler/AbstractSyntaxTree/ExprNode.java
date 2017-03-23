package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-18.
 */
abstract public class ExprNode extends Node {
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
        return false;
    }
    public boolean isLoadable() {
        return false;
    }

    public boolean isCallable() {
        return type().isCallable();
    }

    abstract public <S,E> E accept(ASTVisitor<S,E> visitor);
}
