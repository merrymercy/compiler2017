package com.mercy.compiler.AST;

import com.mercy.compiler.FrontEnd.ASTVisitor;

/**
 * Created by mercy on 17-3-18.
 */
public class LogicalOrNode extends BinaryOpNode {
    public LogicalOrNode(ExprNode left, ExprNode right) {
        super(left, BinaryOp.LOGIC_OR, right);
    }

    @Override
    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
