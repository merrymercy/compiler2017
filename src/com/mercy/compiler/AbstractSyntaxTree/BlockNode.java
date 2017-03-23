package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Entity.Scope;
import com.mercy.compiler.Entity.VariableEntity;

import java.util.List;

/**
 * Created by mercy on 17-3-18.
 */
public class BlockNode extends StmtNode {
    private List<StmtNode> stmts;
    private Scope scope;

    public BlockNode(Location loc, List<StmtNode> stmts) {
        super(loc);
        this.stmts = stmts;
    }

    public List<StmtNode> stmts() {
        return stmts;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Scope scope() {
        return scope;
    }

    @Override
    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
