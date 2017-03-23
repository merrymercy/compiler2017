package com.mercy.compiler.AbstractSyntaxTree;

/**
 * Created by mercy on 17-3-18.
 */
abstract public class StmtNode extends Node {
    protected Location location;

    public StmtNode(Location loc) {
        this.location = loc;
    }

    @Override
    public Location location() {
        return location;
    }

    abstract public <S,E> S accept(ASTVisitor<S,E> visitor);
}
