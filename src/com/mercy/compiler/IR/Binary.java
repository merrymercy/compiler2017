package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.InstructionEmitter;
import com.mercy.compiler.INS.Operand.Operand;

/**
 * Created by mercy on 17-3-30.
 */
public class Binary extends Expr {
    public enum BinaryOp {
        ADD, SUB, MUL, DIV, MOD,
        LSHIFT, RSHIFT, LT, GT, LE, GE, EQ, NE,
        BIT_AND, BIT_XOR, BIT_OR,
        LOGIC_AND, LOGIC_OR
    }

    private Expr left, right;
    private BinaryOp operator;

    public Binary(Expr left, BinaryOp operator, Expr right) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public Expr left() {
        return left;
    }
    public Expr right() {
        return right;
    }
    public BinaryOp operator() {
        return operator;
    }

    @Override
    public Operand accept(InstructionEmitter emitter) {
        return emitter.visit(this);
    }
}
