package com.mercy.compiler.AbstractSyntaxTree;

/**
 * Created by mercy on 17-3-18.
 */
public class LogicalAndNode extends BinaryOpNode {
    public LogicalAndNode(ExprNode left, ExprNode right) {
        super(left, BinaryOp.LOGIC_AND, right);
    }

    @Override
    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
