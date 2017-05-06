package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.InstructionEmitter;
import com.mercy.compiler.INS.Operand.Operand;

/**
 * Created by mercy on 17-3-30.
 */
public class Mem extends Expr {
    Expr expr;

    public Mem(Expr expr) {
        this.expr = expr;
    }

    public Expr expr() {
        return expr;
    }

    @Override
    public Operand accept(InstructionEmitter emitter) {
        return emitter.visit(this);
    }
}
