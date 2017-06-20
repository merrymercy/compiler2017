package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.IRVisitor;

/**
 * Created by mercy on 17-3-30.
 */
public class Label extends IR {
    private String name;

    public Label(String name) {
        this.name = name;
    }

    public Label() {
        this.name = null;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public <T> T accept(IRVisitor<T> emitter) {
        return emitter.visit(this);
    }
}
