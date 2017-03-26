package com.mercy.compiler.AST;

/**
 * Created by mercy on 17-3-18.
 */
public class WhileNode extends StmtNode {
    private StmtNode body;
    private ExprNode cond;

    public WhileNode(Location loc, ExprNode cond, StmtNode body) {
        super(loc);
        this.cond = cond;
        this.body = BlockNode.wrapBlock(body);
    }

    public StmtNode body() {
        return body;
    }

    public ExprNode cond() {
        return cond;
    }

    @Override
    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
