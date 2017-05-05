package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.Entity.Scope;

import java.util.List;

/**
 * Created by mercy on 17-4-26.
 */
public class Label extends Instruction {
    String name;
    public Label(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }


    /*@Override
    public void toNASM(List<String> asm) {
        asm.add(name + ":");
    }*/

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        return name;
    }
}
