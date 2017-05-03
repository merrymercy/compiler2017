package com.mercy.compiler.IR;

import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Operand;
import com.mercy.compiler.INS.Register;

import java.util.List;

/**
 * Created by mercy on 17-3-30.
 */
public class CJump extends IR {
    Expr cond;
    Label trueLabel, falseLabel;

    public CJump(Expr condition, Label trueLabel, Label falseLabel) {
        this.cond = condition;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }

    @Override
    public Operand emit(List<Instruction> ins) {
        Operand tmp = this.cond.emit(ins);
        ins.add(new com.mercy.compiler.INS.CJump(tmp, new com.mercy.compiler.INS.Label(this.trueLabel.name()),
                                                      new com.mercy.compiler.INS.Label(this.falseLabel.name())));
        return null;
    }
}
