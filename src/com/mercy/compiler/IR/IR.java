package com.mercy.compiler.IR;

import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Operand;
import com.mercy.compiler.INS.Register;

import java.util.List;

/**
 * Created by mercy on 17-3-30.
 */
abstract public class IR {
    abstract public Operand emit(List<Instruction> ins);
}
