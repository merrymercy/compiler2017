package com.mercy.compiler.IR;

/**
 * Created by mercy on 17-3-30.
 */
public class IntConst extends Expr {
    private int value;

    public IntConst(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
