package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;

/**
 * Created by mercy on 17-4-25.
 */
public class Cmp extends Instruction {
    public enum Operator {
        EQ, NE, GE, GT, LE, LT
    }
    private Operand left, right;
    private Operator operator;

    public Cmp(Operand left, Operand right, Operator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public Operand left() {
        return left;
    }

    public Operand right() {
        return right;
    }

    public Operator operator() {
        return operator;
    }

    @Override
    public void calcDefAndUse() {
        use.addAll(left().getAllRef());
        use.addAll(right().getAllRef());
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        return "cmp " + left + ", " + right;
    }
}
