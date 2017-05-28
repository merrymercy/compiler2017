package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.InstructionEmitter;
import com.mercy.compiler.INS.Operand.Operand;

/**
 * Created by mercy on 17-3-30.
 */
public class CJump extends IR {
    private Expr cond;
    private Label trueLabel, falseLabel;

    public CJump(Expr condition, Label trueLabel, Label falseLabel) {
        this.cond = condition;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }

    public Expr cond() {
        return cond;
    }

    public Label trueLabel() {
        return trueLabel;
    }

    public Label falseLabel() {
        return falseLabel;
    }

    @Override
    public Operand accept(InstructionEmitter emitter) {
        return emitter.visit(this);
    }
}
