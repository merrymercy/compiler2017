package com.mercy.compiler.IR;

import com.mercy.compiler.Entity.FunctionEntity;

import java.util.List;

/**
 * Created by mercy on 17-3-30.
 */
public class Call extends Expr {
    private FunctionEntity entity;
    List<Expr> args;

    public Call(FunctionEntity entity, List<Expr> args) {
        this.entity = entity;
        this.args = args;
    }
}
