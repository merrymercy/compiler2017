package com.mercy.compiler.IR;

import com.mercy.compiler.Entity.Entity;

/**
 * Created by mercy on 17-3-30.
 */
public class Addr extends Expr {
    Entity entity;

    public Addr(Entity entity) {
        super();
        this.entity = entity;
    }
}
