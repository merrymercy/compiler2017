package com.mercy.compiler.INS.Operand;

import java.util.List;

/**
 * Created by mercy on 17-4-26.
 */
public class Operand {
    public String toNASM() {
        return "operand";
    }

    public boolean isRegister() {
        return false;
    }
}
