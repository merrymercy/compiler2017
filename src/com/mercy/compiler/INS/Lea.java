package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Address;
import com.mercy.compiler.INS.Operand.Operand;

/**
 * Created by mercy on 17-4-25.
 */
public class Lea extends Instruction {
    Operand dest;
    Address addr;

    public Lea (Operand dest, Address addr) {
        this.dest = dest;
        this.addr = addr;
    }

    public Operand dest() {
        return dest;
    }

    public Address addr() {
        return addr;
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }


    @Override
    public String toString() {
        return "lea " + dest + ", " + addr;
    }
}
