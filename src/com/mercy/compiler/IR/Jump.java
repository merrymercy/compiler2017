package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.IRVisitor;

/**
 * Created by mercy on 17-3-30.
 */
public class Jump extends IR {
    Label label;

    public Jump(Label label) {
        this.label = label;
    }

    public Label label() {
        return label;
    }

    @Override
    public <T> T accept(IRVisitor<T> emitter) {
        return emitter.visit(this);
    }
}
