package com.mercy.compiler.Entity;

import com.mercy.compiler.AST.ExprNode;
import com.mercy.compiler.AST.Location;
import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-20.
 */
public class ConstantEntity extends Entity {
    private ExprNode value;

    public ConstantEntity(Location loc, Type type, String name, ExprNode value) {
        super(loc, type, name);
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
