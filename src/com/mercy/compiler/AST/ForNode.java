package com.mercy.compiler.AST;

import com.mercy.compiler.FrontEnd.ASTVisitor;

/**
 * Created by mercy on 17-3-18.
 */
public class ForNode extends  StmtNode {
    private ExprNode init, cond, incr;
    private StmtNode body;

    public ForNode(Location loc, ExprNode init, ExprNode cond, ExprNode incr, StmtNode body) {
        super(loc);
        this.init = init;
        this.cond = cond;
        this.incr = incr;
        this.body = BlockNode.wrapBlock(body);
    }

    public ExprNode init() {
        return init;
    }

    public ExprNode cond() {
        return cond;
    }

    public ExprNode incr() {
        return incr;
    }

    public StmtNode body() {
        return body;
    }

    @Override
    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
