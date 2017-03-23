package com.mercy.compiler.Entity;

import com.mercy.compiler.AbstractSyntaxTree.ExprNode;
import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-20.
 */
public class VariableEntity extends Entity {
    private ExprNode initializer;
    private long sequence;

    public VariableEntity(Type type, String name, ExprNode init) {
        super(type, name);
        initializer = init;
        sequence = -1;
    }

    static private long tmpSeq = 0;
    static public VariableEntity tmp(Type t) {
        return new VariableEntity(t, "@tmp" + tmpSeq++, null);
    }

    public ExprNode initializer() {
        return initializer;
    }

    public long sequence() {
        return sequence;
    }

    @Override
    public String toString() {
        return "variable entity : " + name;
    }

    @Override
    public <T> T accept(EntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
