package com.mercy.compiler.IR;

import com.mercy.compiler.Entity.Entity;

/**
 * Created by mercy on 17-3-30.
 */
public class Var extends Expr {
    Entity var;

    public Var(Entity var) {
        this.var = var;
    }
}
