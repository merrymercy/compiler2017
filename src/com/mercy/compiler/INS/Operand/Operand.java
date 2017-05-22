package com.mercy.compiler.INS.Operand;

/**
 * Created by mercy on 17-4-26.
 */
public class Operand {
    public String toNASM() {
        return "base";
    }

    public boolean isRegister() {
        return false;
    }
}
