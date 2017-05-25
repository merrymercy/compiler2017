package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;

/**
 * Created by mercy on 17-4-25.
 */
public class Move extends Instruction {
    Operand dest, src;
    public Move(Operand dest, Operand src) {
        this.dest = dest;
        this.src = src;
    }

    public Operand dest() {
        return dest;
    }

    public Operand src() {
        return src;
    }

    public boolean isRefMove() {
        return dest instanceof Reference && src instanceof Reference;
    }

    @Override
    public void calcDefAndUse() {
        if (dest instanceof Reference) {
            def.add((Reference)dest);
            use.addAll(src.getAllRef());
        } else {
            use.addAll(dest.getAllRef());
            use.addAll(src.getAllRef());
        }
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        return "mov " + dest + ", " + src;
    }
}
