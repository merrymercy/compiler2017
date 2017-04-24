package com.mercy.compiler.IR;

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
}
