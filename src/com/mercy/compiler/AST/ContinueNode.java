package com.mercy.compiler.AST;

/**
 * Created by mercy on 17-3-18.
 */
public class ContinueNode extends StmtNode  {
    public ContinueNode(Location loc) {
        super(loc);
    }

    @Override
    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
