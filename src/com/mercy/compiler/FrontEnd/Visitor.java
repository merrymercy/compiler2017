package com.mercy.compiler.FrontEnd;

import com.mercy.compiler.AST.*;

import java.util.List;

/**
 * Created by mercy on 17-3-24.
 */
abstract public class Visitor implements ASTVisitor<Void, Void> {
    public void visitStmt(StmtNode stmt) {
        stmt.accept(this);
    }

    public void visitStmts(List<? extends StmtNode> stmts) {
        for (StmtNode s : stmts) {
            visitStmt(s);
        }
    }

    public void visitExpr(ExprNode expr) {
        expr.accept(this);
    }

    public void visitExprs(List<? extends ExprNode> exprs) {
        for (ExprNode e : exprs) {
            visitExpr(e);
        }
    }

    public void visitDefinition(DefinitionNode def) {
        def.accept(this);
    }

    public void visitDefinitions(List<? extends DefinitionNode> defs) {
        for (DefinitionNode d : defs) {
            visitDefinition(d);
        }
    }

    //
    // Statements
    //
    @Override
    public Void visit(BlockNode node) {
        visitStmts(node.stmts());
        return null;
    }

    @Override
    public Void visit(ExprStmtNode node) {
        visitExpr(node.expr());
        return null;
    }

    @Override
    public Void visit(IfNode n) {
        visitExpr(n.cond());
        if (n.thenBody() != null) {
            visitStmt(n.thenBody());
        }
        if (n.elseBody() != null) {
            visitStmt(n.elseBody());
        }
        return null;
    }

    @Override
    public Void visit(WhileNode n) {
        visitExpr(n.cond());
        if (n.body() != null)
            visitStmt(n.body());
        return null;
    }

    @Override
    public Void visit(ForNode n) {
        if (n.init() != null)
            visitExpr(n.init());
        if (n.cond() != null)
            visitExpr(n.cond());
        if (n.incr() != null)
            visitExpr(n.incr());
        if (n.body() != null)
            visitStmt(n.body());
        return null;
    }

    @Override
    public Void visit(BreakNode n) {
        return null;
    }

    @Override
    public Void visit(ContinueNode n) {
        return null;
    }

    @Override
    public Void visit(ReturnNode n) {
        if (n.expr() != null) {
            visitExpr(n.expr());
        }
        return null;
    }

    @Override
    public Void visit(ClassDefNode n) {
        visitStmts(n.entity().memberVars());
        visitStmts(n.entity().memberFuncs());
        return null;
    }

    @Override
    public Void visit(FunctionDefNode n) {
        visitStmt(n.entity().body());
        return null;
    }

    @Override
    public Void visit(VariableDefNode n) {
        if (n.entity().initializer() != null) {
            visitExpr(n.entity().initializer());
        }
        return null;
    }

    //
    // Expressions
    //
    @Override
    public Void visit(AssignNode n) {
        visitExpr(n.lhs());
        visitExpr(n.rhs());
        return null;
    }

    @Override
    public Void visit(BinaryOpNode n) {
        visitExpr(n.left());
        visitExpr(n.right());
        return null;
    }

    @Override
    public Void visit(LogicalOrNode node) {
        visitExpr(node.left());
        visitExpr(node.right());
        return null;
    }

    @Override
    public Void visit(LogicalAndNode node) {
        visitExpr(node.left());
        visitExpr(node.right());
        return null;
    }

    @Override
    public Void visit(UnaryOpNode node) {
        visitExpr(node.expr());
        return null;
    }

    @Override
    public Void visit(PrefixOpNode node) {
        visitExpr(node.expr());
        return null;
    }

    @Override
    public Void visit(SuffixOpNode node) {
        visitExpr(node.expr());
        return null;
    }

    @Override
    public Void visit(FuncallNode node) {
        visitExpr(node.expr());
        visitExprs(node.args());
        return null;
    }

    @Override
    public Void visit(ArefNode node) {
        visitExpr(node.expr());
        visitExpr(node.index());
        return null;
    }

    @Override
    public Void visit(CreatorNode node) {
        if (node.exprs() != null)
            visitExprs(node.exprs());
        return null;
    }

    @Override
    public Void visit(MemberNode node) {
        visitExpr(node.expr());
        return null;
    }

    @Override
    public Void visit(VariableNode node) {
        return null;
    }

    @Override
    public Void visit(IntegerLiteralNode node) {
        return null;
    }

    @Override
    public Void visit(StringLiteralNode node) {
        return null;
    }

    @Override
    public Void visit(BoolLiteralNode node) {
        return null;
    }
}
