package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;

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

    public void setLeft(Operand left) {
        this.left = left;
    }
    public void setRight(Operand right) {
        this.right = right;
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

    abstract public String name();
    abstract public void accept(Translator translator);

    @Override
    public String toString() {
        return this.name() + " " + left + ", " + right;
    }
}
