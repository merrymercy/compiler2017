package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-23.
 */
public class ParameterDefNode extends DefinitionNode {
    private Type type;

    public ParameterDefNode(Location loc, Type type, String name) {
        super(loc, name);
        this.type = type;
    }

    public Type type() {
        return type;
    }

    @Override
    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
