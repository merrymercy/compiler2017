package com.mercy.compiler.IR;

import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Move;
import com.mercy.compiler.INS.Operand;
import com.mercy.compiler.INS.Register;

import java.util.List;

/**
 * Created by mercy on 17-3-30.
 */
public class Assign extends IR {
    Expr left, right;

    public Assign(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Operand emit(List<Instruction> ins) {
        Operand rhs = right.emit(ins);
        Operand lhs = left.emit(ins);
        ins.add(new Move(lhs, rhs));
        return null;
    }
}
