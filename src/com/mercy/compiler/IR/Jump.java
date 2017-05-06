package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.InstructionEmitter;
import com.mercy.compiler.INS.Operand.Operand;

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
    public Operand accept(InstructionEmitter emitter) {
        return emitter.visit(this);
    }
}
