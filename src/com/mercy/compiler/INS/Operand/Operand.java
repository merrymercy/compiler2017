package com.mercy.compiler.INS.Operand;

import java.util.Set;

/**
 * Created by mercy on 17-4-26.
 */
abstract public class Operand {
    public String toNASM() {
        return "operand";
    }

    abstract public Set<Reference> getAllRef();

    abstract public Operand replace(Operand from, Operand to);

    public boolean isRegister() {
        return false;
    }

    public boolean isDirect() {
        return false;
    }

    public boolean isAddress() {
        return false;
    }
}
