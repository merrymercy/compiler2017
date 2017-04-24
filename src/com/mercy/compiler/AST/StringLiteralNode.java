package com.mercy.compiler.AST;

import com.mercy.compiler.Entity.StringConstantEntity;
import com.mercy.compiler.FrontEnd.ASTVisitor;
import com.mercy.compiler.Type.StringType;

/**
 * Created by mercy on 17-3-18.
 */
public class StringLiteralNode extends  LiteralNode {
    private String value;
    private StringConstantEntity entity;

    public StringLiteralNode(Location loc, String value) {
        super(loc, new StringType());
        this.value = value;
    }

    public String value() {
        return value;
    }

    public StringConstantEntity entity() {
        return entity;
    }

    public void setEntity(StringConstantEntity entity) {
        this.entity = entity;
    }

    @Override
    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
