package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-18.
 */
abstract public class LiteralNode extends ExprNode {
    protected Location location;
    protected Type type;

    public LiteralNode(Location loc, Type type) {
        super();
        this.location = loc;
        this.type = type;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public Location location() {
        return location;
    }

    @Override
    public Type type() {
        return type;
    }
}
