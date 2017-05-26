package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.INS.Operand.Register;

/**
 * Created by mercy on 17-5-26.
 */
public class Pop extends Instruction {
    private Operand operand;

    public Pop(Operand operand) {
        this.operand = operand;
    }

    public Operand operand() {
        return operand;
    }

    @Override
    public void replaceUse(Reference from, Reference to) {
        operand = (Register)operand.replace(from, to);
    }

    @Override
    public void replaceDef(Reference from, Reference to) {
        operand = (Register)operand.replace(from, to);
    }

    @Override
    public void replaceAll(Reference from, Reference to) {
        operand = (Register)operand.replace(from, to);
    }


    @Override
    public void calcDefAndUse() {
        def.addAll(operand().getAllRef());
        allref.addAll(use);
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        return "pop " + operand;
    }
}
