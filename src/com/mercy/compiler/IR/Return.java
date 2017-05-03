package com.mercy.compiler.IR;

import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Operand;
import com.mercy.compiler.INS.Register;

import java.util.List;

/**
 * Created by mercy on 17-3-30.
 */
public class Return extends IR {
    Expr expr;

    public Return(Expr expr) {
        this.expr = expr;
    }

    @Override
    public Operand emit(List<Instruction> ins) {
        Operand ret = expr.emit(ins);
        ins.add(new com.mercy.compiler.INS.Return(ret));
        return null;
    }
}
