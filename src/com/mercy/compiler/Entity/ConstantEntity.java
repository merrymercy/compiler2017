package com.mercy.compiler.Entity;

import com.mercy.compiler.AbstractSyntaxTree.ExprNode;
import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-20.
 */
public class ConstantEntity extends Entity {
    private ExprNode value;

    public ConstantEntity(Type type, String name, ExprNode value) {
        super(type, name);
        this.value = value;
    }

    @Override
    public ExprNode value() {
        return value;
    }

    @Override
    public <T> T accept(EntityVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "constant entity : " + name;
    }
}
