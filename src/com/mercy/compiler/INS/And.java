package com.mercy.compiler.INS;


import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;

/**
 * Created by mercy on 17-4-25.
 */
public class And extends Bin {
    public And(Operand left, Operand right) {
        super(left, right);
    }

    @Override
    public String name() {
        return "and";
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }
}
