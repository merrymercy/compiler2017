package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.InstructionEmitter;
import com.mercy.compiler.Entity.StringConstantEntity;
import com.mercy.compiler.INS.Operand.Operand;

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
    public Operand accept(InstructionEmitter emitter) {
        return emitter.visit(this);
    }
}
