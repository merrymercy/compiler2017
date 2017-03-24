package com.mercy.compiler.Entity;

/**
 * Created by mercy on 17-3-24.
 */
public class MemberEntity extends VariableEntity {
    public MemberEntity(VariableEntity entity) {
        super(entity.location(), entity.type(), entity.name(), entity.initializer());
    }
}
