package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;

import java.util.List;

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

    public Operand cond() {
        return cond;
    }

    public Label trueLabel() {
        return trueLabel;
    }

    public Label falseLabel() {
        return falseLabel;
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        return "Cjump " + cond + ", " + trueLabel + ", " + falseLabel;
    }
}
