package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Reference;

/**
 * Created by mercy on 17-5-4.
 */
public class Comment extends Instruction {
    String comment;

    public Comment(String comment) {
        this.comment = comment;
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
        return comment;
    }
}
