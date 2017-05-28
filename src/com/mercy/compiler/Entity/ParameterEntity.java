package com.mercy.compiler.Entity;

import com.mercy.compiler.AST.Location;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-24.
 */
public class ParameterEntity extends Entity {
    private Reference source;

    public ParameterEntity(Location loc, Type type, String name) {
        super(loc, type, name);
    }

    public Reference source() {
        return source;
    }
    public void setSource(Reference source) {
        this.source = source;
    }

    @Override
    public <T> T accept(EntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
