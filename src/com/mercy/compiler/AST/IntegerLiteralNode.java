package com.mercy.compiler.AST;

import com.mercy.compiler.Type.IntegerType;

/**
 * Created by mercy on 17-3-18.
 */
public class IntegerLiteralNode extends LiteralNode {
    private long value;

    public IntegerLiteralNode(Location loc, long value) {
        super(loc, new IntegerType());
        this.value = value;
    }

    public long value() {
        return value;
    }

    @Override
    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
