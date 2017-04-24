package com.mercy.compiler.IR;

import com.mercy.compiler.Entity.Entity;

/**
 * Created by mercy on 17-3-30.
 */
public class Var extends Expr {
    Entity entity;

    public Var(Entity entity) {
        this.entity = entity;
    }

    public Entity entity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
