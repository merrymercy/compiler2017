package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.IRVisitor;

/**
 * Created by mercy on 17-3-30.
 */
public class Return extends IR {
    Expr expr;

    public Return(Expr expr) {
        this.expr = expr;
    }

    public Expr expr() {
        return expr;
    }

    @Override
    public <T> T accept(IRVisitor<T> emitter) {
        return emitter.visit(this);
    }
}
