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

    public boolean isRegister() {
        return false;
    }
}
