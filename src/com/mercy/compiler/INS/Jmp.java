package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;

import java.util.List;

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

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        return "jmp " + dest;
    }
}
