package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Address;
import com.mercy.compiler.INS.Operand.Operand;

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

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        return "mov " + dest + ", " + src;
    }
}
