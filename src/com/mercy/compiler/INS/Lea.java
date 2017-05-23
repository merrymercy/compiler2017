package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Address;
import com.mercy.compiler.INS.Operand.Reference;

/**
 * Created by mercy on 17-4-25.
 */
public class Lea extends Instruction {
    Reference dest;
    Address addr;

    public Lea (Reference dest, Address addr) {
        this.dest = dest;
        this.addr = addr;
    }

    public Reference dest() {
        return dest;
    }

    public Address addr() {
        return addr;
    }

    @Override
    public void calcDefAndUse() {
        def.add(dest);
        use.addAll(addr.getAllRef());
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
