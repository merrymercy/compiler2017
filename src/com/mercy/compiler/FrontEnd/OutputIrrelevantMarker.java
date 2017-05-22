package com.mercy.compiler.FrontEnd;

import com.mercy.compiler.AST.*;
import com.mercy.compiler.Entity.Entity;
import com.mercy.compiler.Entity.FunctionEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Created by mercy on 17-5-22.
 */
public class OutputIrrelevantMarker extends com.mercy.compiler.AST.Visitor {
    static Set<Entity> outputRelevant = new HashSet<>();
    FunctionEntity currentFunction;

    public OutputIrrelevantMarker(AST ast) {
        outputRelevant.add(ast.scope().lookup("print"));
        outputRelevant.add(ast.scope().lookup("println"));
    }

    Stack<Boolean> irrelevantStack = new Stack<>();
    int inRelevant = 0;

    @Override
    public Void visit(FuncallNode node) {
        if (outputRelevant.contains(node.functionType().entity())) {
            for (int i = 0; i< irrelevantStack.size(); i++)
                irrelevantStack.set(i, false);
        }
        visitExpr(node.expr());
        visitExprs(node.args());

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
        boolean save = true;
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
        if (n.init() != null)
            visitExpr(n.incr());
        visitStmt(n.body());
        if (n.body().outputIrrelevant() == false) {
            // remap en
        }
        return null;
    }


    @Override
    public Void visit(BlockNode node) {
        irrelevantStack.push(true);
        visitStmts(node.stmts());
        node.setOutputIrrelevant(irrelevantStack.peek());
        if (irrelevantStack.peek().equals(true)) {
            System.out.println("can be deleted");
        } else {
            System.out.println("save");
        }

        irrelevantStack.pop();
        return null;
    }

    @Override
    public Void visit(VariableNode node) {
        if (!node.entity().outputIrrelevant()) {
            for (int i = 0; i< irrelevantStack.size(); i++)
                irrelevantStack.set(i, false);
        }
        return null;
    }

    @Override
    public Void visit(ReturnNode node) {
        if (node.expr() != null)
            visitExpr(node.expr());
        return null;
    }

    @Override
    public void visitExpr(ExprNode node) {
        node.accept(this);
    }
}
