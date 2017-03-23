package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Type.Type;
import jdk.nashorn.internal.ir.UnaryNode;

/**
 * Created by mercy on 17-3-18.
 */
public class UnaryOpNode extends ExprNode {
    public enum UnaryOp {
        PRE_INC, PRE_DEC, SUF_INC, SUF_DEC,
        MINUS, ADD, LOGIC_NOT, BIT_NOT
    }

    private UnaryOp operator;
    private ExprNode expr;
    private Type type;
    private long amount;

    public UnaryOpNode(UnaryOp op, ExprNode expr) {
        this.operator = op;
        this.expr = expr;
        amount = 1;
    }

    public UnaryOp operator() {
        return operator;
    }

    @Override
    public Type type() {
        return expr.type();
    }

    public ExprNode expr() {
        return expr;
    }
    public void setExpr(ExprNode expr) {
        this.expr = expr;
    }

    public long amount() {
        return amount;
    }
    public void setAmount(long amount) {
        this.amount = amount;
    }

    @Override
    public Location location() {
        return expr.location();
    }

    @Override
    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
