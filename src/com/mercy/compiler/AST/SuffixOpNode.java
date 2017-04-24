package com.mercy.compiler.AST;

import com.mercy.compiler.FrontEnd.ASTVisitor;

/**
 * Created by mercy on 17-3-18.
 */
public class SuffixOpNode extends UnaryOpNode {
    public SuffixOpNode(UnaryOp op, ExprNode expr) {
        super(op, expr);
    }

    @Override
    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
