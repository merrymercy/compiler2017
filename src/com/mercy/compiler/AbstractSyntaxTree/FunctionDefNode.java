package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-23.
 */
public class FunctionDefNode extends DefinitionNode {
    private FunctionEntity entity;

    public FunctionDefNode(FunctionEntity entity) {
        super(entity.location(), entity.name());
        this.entity = entity;
    }

    public FunctionEntity entity() {
        return entity;
    }

    @Override
    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
