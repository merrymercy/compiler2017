package com.mercy.compiler.AST;

import com.mercy.compiler.Entity.ConstantEntity;
import com.mercy.compiler.Type.StringType;

/**
 * Created by mercy on 17-3-18.
 */
public class StringLiteralNode extends  LiteralNode {
    private String value;
    private ConstantEntity entry;

    public StringLiteralNode(Location loc, String value) {
        super(loc, new StringType());
        this.value = value;
    }

    public String value() {
        return value;
    }

    public ConstantEntity entry() {
        return entry;
    }

    public void setEntry(ConstantEntity entry) {
        this.entry = entry;
    }

    @Override
    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
