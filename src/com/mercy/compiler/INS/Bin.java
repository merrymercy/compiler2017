package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;

/**
 * Created by mercy on 17-5-5.
 */
abstract public class Bin extends Instruction {
    protected Operand left, right;

    public Bin(Operand left, Operand right) {
        this.left = left;
        this.right = right;
    }

    public Operand left() {
        return left;
    }

    public Operand right() {
        return right;
    }

    abstract public String name();
    abstract public void accept(Translator translator);

    @Override
    public String toString() {
        return this.name() + " " + left + ", " + right;
    }
}
