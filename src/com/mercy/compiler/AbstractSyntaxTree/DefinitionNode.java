package com.mercy.compiler.AbstractSyntaxTree;

/**
 * Created by mercy on 17-3-23.
 */
abstract public class DefinitionNode extends StmtNode {
    protected String name;

    public DefinitionNode(Location loc, String name) {
        super(loc);
        this.name = name;
    }

    public String name() {
        return name;
    }

    abstract public <S,E> S accept(ASTVisitor<S,E> visitor);
}
