package com.mercy.compiler.AST;

import com.mercy.compiler.Entity.ClassEntity;
import com.mercy.compiler.FrontEnd.ASTVisitor;

/**
 * Created by mercy on 17-3-18.
 */
public class ClassDefNode extends DefinitionNode {
    private ClassEntity entity; // store all information in entity

    public ClassDefNode(ClassEntity entity) {
        super(entity.location(), entity.name());
        this.entity = entity;
    }

    public ClassEntity entity() {
        return entity;
    }

    @Override
    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
