package com.mercy.compiler.Entity;

import com.mercy.compiler.AST.ExprNode;
import com.mercy.compiler.AST.Location;
import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-20.
 */
public class VariableEntity extends Entity {
    private ExprNode initializer;

    public VariableEntity(Location loc, Type type, String name, ExprNode init) {
        super(loc, type, name);
        initializer = init;
    }

    public ExprNode initializer() {
        return initializer;
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
