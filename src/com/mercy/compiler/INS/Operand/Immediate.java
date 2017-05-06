package com.mercy.compiler.INS.Operand;

import java.util.List;

/**
 * Created by mercy on 17-5-4.
 */
public class Immediate extends Operand {
    private int value;
    public Immediate(int value) {
        this.value = value;
    }

    @Override
    public String toNASM() {
        return Integer.toString(value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
