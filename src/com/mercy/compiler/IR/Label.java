package com.mercy.compiler.IR;

/**
 * Created by mercy on 17-3-30.
 */
public class Label extends IR {
    String name;

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
}
