package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;

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
    public void replaceUse(Reference from, Reference to) {
        if (ret != null)
            ret = ret.replace(from, to);
    }

    @Override
    public void replaceDef(Reference from, Reference to) {
    }

    @Override
    public void replaceAll(Reference from, Reference to) {
    }

    @Override
    public void calcDefAndUse() {
        if (ret != null) {
            use.addAll(ret.getAllRef());
            allref.addAll(use);
        }
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
