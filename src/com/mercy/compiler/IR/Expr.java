package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.InstructionEmitter;
import com.mercy.compiler.INS.Operand.Operand;

/**
 * Created by mercy on 17-3-30.
 */
abstract public class Expr extends IR {
    @Override
    abstract public Operand accept(InstructionEmitter emitter);
}
