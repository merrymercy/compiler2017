package com.mercy.compiler.IR;

import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Jmp;
import com.mercy.compiler.INS.Operand;

import java.util.List;

/**
 * Created by mercy on 17-3-30.
 */
public class Jump extends IR {
    Label label;

    public Jump(Label label) {
        this.label = label;
    }

    @Override
    public Operand emit(List<Instruction> ins) {
        ins.add(new Jmp(new com.mercy.compiler.INS.Label(label.name())));
        return null;
    }
}
