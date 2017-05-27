package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Reference;

/**
 * Created by mercy on 17-4-25.
 */
public class Jmp extends Instruction {
    Label dest;
    public Jmp(Label dest) {
        this.dest = dest;
    }

    public Label dest() {
        return dest;
    }

    public void setDest(Label dest) {
        this.dest = dest;
    }

    @Override
    public void replaceUse(Reference from, Reference to) {
    }

    @Override
    public void replaceDef(Reference from, Reference to) {

    }

    @Override
    public void replaceAll(Reference from, Reference to) {
    }


    @Override
    public void calcDefAndUse() {
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        return "jmp " + dest;
    }
}
