package com.mercy.compiler.AST;

import com.mercy.compiler.FrontEnd.ASTVisitor;
import com.mercy.compiler.Type.BoolType;

/**
 * Created by mercy on 17-3-23.
 */
public class BoolLiteralNode extends LiteralNode {
    private boolean value;

    public BoolLiteralNode(Location loc, boolean value) {
        super(loc, new BoolType());
        this.value = value;
    }

    public boolean value() {
        return value;
    }

    @Override
    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}