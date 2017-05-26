package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;

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

    @Override
    public void replaceUse(Reference from, Reference to) {
        right = right.replace(from, to);
        if (left != from)
            left = left.replace(from, to);
    }

    @Override
    public void replaceDef(Reference from, Reference to) {
        left = left.replace(from, to);
    }

    @Override
    public void replaceAll(Reference from, Reference to) {
        left = left.replace(from, to);
        right = right.replace(from, to);
    }


    public Operator operator() {
        return operator;
    }

    @Override
    public void calcDefAndUse() {
        if (left instanceof  Reference) {
            def.addAll(left.getAllRef());
        }
        use.addAll(left.getAllRef());
        use.addAll(right.getAllRef());
        allref.addAll(use);
        allref.addAll(def);
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
