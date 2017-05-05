package com.mercy.compiler.Entity;

import com.mercy.compiler.AST.ExprNode;
import com.mercy.compiler.AST.Location;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.Type.Type;
import com.mercy.compiler.Utility.InternalError;

/**
 * Created by mercy on 17-3-18.
 */
abstract public class Entity {
    protected Location location;
    protected String name;
    protected Type type;
    protected int offset;
    protected Reference reference;

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

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int offset() {
        return this.offset;
    }

    public Reference reference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    abstract public <T> T accept(EntityVisitor<T> visitor);
}
