package com.mercy.compiler.AST;

import com.mercy.compiler.Entity.Scope;
import com.mercy.compiler.FrontEnd.ASTVisitor;

import java.util.LinkedList;
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

    public static BlockNode wrapBlock(StmtNode node) {
        if (node == null)
            return null; //new BlockNode(new Location(0,0), new LinkedList<>());

        if (node instanceof BlockNode) {
            return (BlockNode) node;
        } else {
            return new BlockNode(node.location(),
                    new LinkedList<StmtNode>() {{
                        add(node);
                    }});
        }
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
