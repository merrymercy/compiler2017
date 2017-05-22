package com.mercy.compiler.Entity;

import com.mercy.compiler.AST.Location;
import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-24.
 */
public class ParameterEntity extends Entity {
    public ParameterEntity(Location loc, Type type, String name) {
        super(loc, type, name);
    }

    @Override
    public <T> T accept(EntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
