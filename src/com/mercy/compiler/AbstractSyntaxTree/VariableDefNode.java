package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Entity.VariableEntity;
import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-18.
 */
public class VariableDefNode extends DefinitionNode {
    private VariableEntity entity;

    public VariableDefNode(VariableEntity entity) {
        super(entity.location(), entity.name());
        this.entity = entity;

    }

    public VariableEntity entity() {
        return entity;
    }

    @Override
    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
