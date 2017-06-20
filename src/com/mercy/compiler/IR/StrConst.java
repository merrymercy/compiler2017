package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.IRVisitor;
import com.mercy.compiler.Entity.StringConstantEntity;

/**
 * Created by mercy on 17-3-30.
 */
public class StrConst extends Expr {
    private StringConstantEntity entity;

    public StrConst(StringConstantEntity entity) {
        this.entity = entity;
    }

    public StringConstantEntity entity() {
        return entity;
    }

    @Override
    public <T> T accept(IRVisitor<T> emitter) {
        return emitter.visit(this);
    }
}
