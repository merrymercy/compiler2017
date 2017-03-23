package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-18.
 */
public class VariableDefNode extends DefinitionNode {
    private Type type;
    private ExprNode init;

    public VariableDefNode(Location loc, Type type, String name, ExprNode init) {
        super(loc, name);
        this.type = type;
        this.init = init;
    }

    public ExprNode init() {
        return init;
    }

    public Type type() {
        return type;
    }

    @Override
    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
