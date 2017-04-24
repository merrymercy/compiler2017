package com.mercy.compiler.IR;

/**
 * Created by mercy on 17-3-30.
 */
public class Jump extends IR {
    Label label;

    public Jump(Label label) {
        this.label = label;
    }
}
