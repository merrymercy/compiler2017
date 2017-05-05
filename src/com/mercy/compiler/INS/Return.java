package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;

/**
 * Created by mercy on 17-4-25.
 */
public class Return extends Instruction {
    private Operand ret;
    public Return (Operand ret) {
        this.ret = ret;
    }

    public Operand ret() {
        return ret;
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        return "ret " + ret;
    }
}
