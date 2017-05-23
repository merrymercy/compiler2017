package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;

/**
 * Created by mercy on 17-4-25.
 */
public class Neg extends Instruction {
    private Operand operand;

    public Neg(Operand operand) {
        this.operand = operand;
    }

    public Operand operand() {
        return operand;
    }

    @Override
    public void calcDefAndUse() {
        if (operand instanceof Reference) {
            def.addAll(operand().getAllRef());
        }
        use.addAll(operand().getAllRef());
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        return "neg " + operand;
    }
}
