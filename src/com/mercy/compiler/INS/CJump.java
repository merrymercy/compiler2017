package com.mercy.compiler.INS;

import com.mercy.compiler.IR.*;

/**
 * Created by mercy on 17-4-25.
 */
public class CJump extends Instruction {
    Operand cond;
    Label trueLabel, falseLabel;

    public CJump(Operand cond, Label trueLabel, Label falseLabel) {
        this.cond = cond;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }
}
