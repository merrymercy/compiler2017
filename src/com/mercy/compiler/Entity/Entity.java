package com.mercy.compiler.Entity;

import com.mercy.compiler.AST.ExprNode;
import com.mercy.compiler.AST.Location;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.Type.Type;
import com.mercy.compiler.Utility.InternalError;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mercy on 17-3-18.
 */
abstract public class Entity {
    protected Location location;
    protected String name;
    protected Type type;
    protected int offset;
    protected Reference reference;

    protected Set<Entity> dependence = new HashSet<>();
    boolean isOutputIrrelevant = false;

    public Entity(Location loc, Type type, String name) {
        this.location = loc;
        this.type = type;
        this.name = name;
    }

    // getter and setter
    public String name() {
        return name;
    }

    public Type type() {
        return type;
    }

    public Location location() {
        return location;
    }

    public ExprNode value() {
        throw new InternalError("Entity#value called");
    }

    public int size() {
        return type.size();
    }

    public int offset() {
        return this.offset;
    }
    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Reference reference() {
        return reference;
    }
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public boolean outputIrrelevant() {
        return isOutputIrrelevant;
    }
    public void setOutputIrrelevant(boolean outputIrrelevant) {
        isOutputIrrelevant = outputIrrelevant;
    }

    public Set<Entity> dependence() {
        return dependence;
    }
    public void addDependence(Entity entity) {
        if (entity != this)
            dependence.add(entity);
    }
}
