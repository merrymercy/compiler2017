package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.InstructionEmitter;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.INS.Operand.Operand;

import java.util.List;

/**
 * Created by mercy on 17-3-30.
 */
public class Call extends Expr {
    private FunctionEntity entity;
    private List<Expr> args;

    public Call(FunctionEntity entity, List<Expr> args) {
        this.entity = entity;
        this.args = args;
    }

    public FunctionEntity entity() {
        return entity;
    }
    public List<Expr> args() {
        return args;
    }

    @Override
    public Operand accept(InstructionEmitter emitter) {
        return emitter.visit(this);
    }
}
