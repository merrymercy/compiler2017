package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.InstructionEmitter;
import com.mercy.compiler.INS.Operand.Operand;

/**
 * Created by mercy on 17-3-30.
 */
public class Unary extends Expr {
    public enum UnaryOp {
        MINUS, LOGIC_NOT, BIT_NOT
    }

    private Expr expr;
    private UnaryOp operator;

    public Unary(UnaryOp op, Expr expr) {
        super();
        this.operator = op;
        this.expr = expr;
    }

    public Expr expr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    public UnaryOp operator() {
        return operator;
    }

    public void setOperator(UnaryOp operator) {
        this.operator = operator;
    }


    @Override
    public Operand accept(InstructionEmitter emitter) {
        return emitter.visit(this);
    }
}
