package com.mercy.compiler.AST;


/**
 * Created by mercy on 17-3-18.
 */
public class PrefixOpNode extends UnaryOpNode {
    public PrefixOpNode(UnaryOp op, ExprNode expr) {
        super(op, expr);
    }

    @Override
    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
