package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.BasicBlock;
import com.mercy.compiler.BackEnd.Translator;

/**
 * Created by mercy on 17-4-26.
 */
public class Label extends Instruction {
    String name;
    BasicBlock basicBlock;
    public Label(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public BasicBlock basicBlock() {
        return basicBlock;
    }

    public void setBasicBlock(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
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
        return name;
    }
}
