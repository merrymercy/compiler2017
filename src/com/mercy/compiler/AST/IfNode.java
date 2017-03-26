package com.mercy.compiler.AST;

/**
 * Created by mercy on 17-3-18.
 */
public class IfNode extends  StmtNode  {
    private ExprNode cond;
    private StmtNode thenBody, elseBody;

    public IfNode(Location loc, ExprNode c, StmtNode t, StmtNode e) {
        super(loc);
        this.cond = c;
        this.thenBody = BlockNode.wrapBlock(t);
        this.elseBody = BlockNode.wrapBlock(e);
    }

    public ExprNode cond() {
        return cond;
    }

    public StmtNode thenBody() {
        return thenBody;
    }

    public StmtNode elseBody() {
        return elseBody;
    }

    @Override
    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
