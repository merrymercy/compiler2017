package com.mercy.compiler.IR;

import com.mercy.compiler.INS.*;

import java.util.List;

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

    @Override
    public Operand emit(List<Instruction> ins) {
        ins.add(new com.mercy.compiler.INS.Label(name));
        return null;
    }
}
