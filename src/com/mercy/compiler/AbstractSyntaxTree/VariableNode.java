package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Entity.Entity;
import com.mercy.compiler.Entity.VariableEntity;
import com.mercy.compiler.Type.Type;
import com.mercy.compiler.Utility.InternalError;

/**
 * Created by mercy on 17-3-18.
 */

public class VariableNode extends LHSNode {
    private Location location;
    private String name;
    private Entity entity;

    public VariableNode(Location loc, String name) {
        this.location = loc;
        this.name = name;
    }

    public VariableNode(VariableEntity var) {
        this.entity = var;
        this.name = var.name();
    }

    public String name() {
        return name;
    }

    public Entity entity() {
        if (entity == null) {
            throw new InternalError("Vairable.entity == null");
        }
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public boolean isResolved() {
        return entity != null;
    }

    @Override
    public Type type() {
        return entity.type();
    }

    @Override
    public Location location() {
        return location;
    }

    @Override
    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}