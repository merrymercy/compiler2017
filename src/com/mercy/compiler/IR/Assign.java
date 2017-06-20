package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.IRVisitor;

/**
 * Created by mercy on 17-3-30.
 */
public class Assign extends IR {
    Expr left, right;

    public Assign(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }

    public Expr left() {
        return left;
    }
    public Expr right() {
        return right;
    }

    @Override
    public <T> T accept(IRVisitor<T> emitter) {
        return emitter.visit(this);
    }
}
