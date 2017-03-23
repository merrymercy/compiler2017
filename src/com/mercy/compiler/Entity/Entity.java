package com.mercy.compiler.Entity;

import com.mercy.compiler.AbstractSyntaxTree.ExprNode;
import com.mercy.compiler.AbstractSyntaxTree.Location;
import com.mercy.compiler.Type.Type;
import com.mercy.compiler.Utility.InternalError;

/**
 * Created by mercy on 17-3-18.
 */
abstract public class Entity {
    protected String name;
    protected Type type;

    public Entity(Type type, String name) {
        this.name = name;
        this.type = type;
    }

    public String name() {
        return name;
    }

    public Type type() {
        return type;
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
