package com.mercy.compiler.Entity;

import com.mercy.compiler.AST.ExprNode;
import com.mercy.compiler.AST.Location;
import com.mercy.compiler.Type.Type;
import com.mercy.compiler.Utility.InternalError;

/**
 * Created by mercy on 17-3-18.
 */
abstract public class Entity {
    protected Location location;
    protected String name;
    protected Type type;

    public Entity(Location loc, Type type, String name) {
        this.location = loc;
        this.type = type;
        this.name = name;
    }

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

    public long allocSize() {
        return type.allocSize();
    }
    public long aligment() {
        return type.alignment();
    }

    abstract public <T> T accept(EntityVisitor<T> visitor);
}
