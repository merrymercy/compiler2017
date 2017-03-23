package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Entity.ClassEntity;
import com.mercy.compiler.Entity.FunctionEntity;

import java.util.List;

/**
 * Created by mercy on 17-3-18.
 */
public class ClassDefNode extends DefinitionNode {
    private ClassEntity entity; // store all information in entity

    public ClassDefNode(Location loc, String name, ClassEntity entity) {
        super(loc, name);
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
