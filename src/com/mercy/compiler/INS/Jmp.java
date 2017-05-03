package com.mercy.compiler.INS;

/**
 * Created by mercy on 17-4-25.
 */
public class Jmp extends Instruction {
    Label dest;
    public Jmp(Label dest) {
        this.dest = dest;
    }
}
