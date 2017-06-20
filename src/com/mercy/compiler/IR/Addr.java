package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.IRVisitor;
import com.mercy.compiler.Entity.Entity;

/**
 * Created by mercy on 17-3-30.
 */
public class Addr extends Expr {
    private Entity entity;

    public Addr(Entity entity) {
        super();
        this.entity = entity;
    }

    public Entity entity() {
        return entity;
    }

    @Override
    public <T> T accept(IRVisitor<T> emitter) {
        return emitter.visit(this);
    }
}
