package com.mercy.compiler.INS;

import com.mercy.compiler.Entity.Scope;

/**
 * Created by mercy on 17-4-26.
 */
public class Label extends Instruction {
    String name;
    public Label(String name) {
        this.name = name;
    }
}
