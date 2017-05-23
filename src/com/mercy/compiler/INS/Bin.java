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

    @Override
    public void calcDefAndUse() {
        if (left instanceof  Reference) {
            def.add((Reference) left);
        }
        use.addAll(left.getAllRef());
        use.addAll(right.getAllRef());
    }

    abstract public String name();
    abstract public void accept(Translator translator);

    @Override
    public String toString() {
        return this.name() + " " + left + ", " + right;
    }
}
