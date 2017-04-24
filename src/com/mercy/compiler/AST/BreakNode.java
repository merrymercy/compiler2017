package com.mercy.compiler.AST;

import com.mercy.compiler.FrontEnd.ASTVisitor;

/**
 * Created by mercy on 17-3-18.
 */
public class BreakNode extends  StmtNode  {
    public BreakNode(Location loc) {
        super(loc);
    }

    @Override
    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
