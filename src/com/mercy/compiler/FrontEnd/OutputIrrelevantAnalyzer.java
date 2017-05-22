package com.mercy.compiler.FrontEnd;

import com.mercy.compiler.AST.*;
import com.mercy.compiler.Entity.Entity;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Entity.Scope;
import com.mercy.compiler.Type.ArrayType;
import com.mercy.compiler.Type.ClassType;
import com.mercy.compiler.Utility.InternalError;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Created by mercy on 17-5-22.
 */
public class OutputIrrelevantAnalyzer extends com.mercy.compiler.AST.Visitor {
    static Set<Entity> outputRelevant = new HashSet<>();
    FunctionEntity currentFunction;
    Scope globalScope;
    Set<Entity> source = new HashSet<>();
    Set<DependenceEdge> dependenceEdgeSet = new HashSet<>();

    public OutputIrrelevantAnalyzer(AST ast) {
        globalScope = ast.scope();
        outputRelevant.add(ast.scope().lookup("print"));
        outputRelevant.add(ast.scope().lookup("println"));
    }

    @Override
    public Void visit(ClassDefNode node) {
        return null;
    }

    @Override
    public Void visit(FunctionDefNode node) {
        currentFunction = node.entity();
        visitStmt(node.entity().body());
        if (currentFunction.name().equals("main"))
            currentFunction.setOutputIrrelevant(false);
        return null;
    }

    int collectEntity = 0;
    Stack<Set<Entity>> reliedEntityStack = new Stack<>();

    Entity getBaseEntity(ExprNode node) {
        if (node instanceof ArefNode) {
            return getBaseEntity(((ArefNode) node).baseExpr());
        } else if (node instanceof MemberNode) {
            return getBaseEntity(((MemberNode) node).expr());
        } else if (node instanceof VariableNode) {
            return ((VariableNode) node).entity();
        }
        throw new InternalError("unhandled case in getBaseEntity " + node);
    }

    @Override
    public Void visit(AssignNode node) {
        ExprNode lhs = node.lhs();
        if ((lhs.type() instanceof ArrayType || lhs.type() instanceof ClassType) && !(node.rhs() instanceof CreatorNode)) { // don't do
            node.setOutputIrrelevant(false);
            reliedEntityStack.push(new HashSet<>());
            visitExpr(node.lhs());
            visitExpr(node.rhs());

            source.addAll(reliedEntityStack.peek());

            reliedEntityStack.pop();
        } else {
            Entity base = getBaseEntity(lhs);
            //System.err.print(base.name() + ":  ");

            reliedEntityStack.push(new HashSet<>());
            visitExpr(node.rhs());

            for (Entity entity : reliedEntityStack.peek()) {
                dependenceEdgeSet.add(new DependenceEdge(base, entity));
                base.addDependence(entity);
                //System.err.print(" " + entity.name());
            }
            reliedEntityStack.pop();
        }
        return null;
    }

    @Override
    public Void visit(VariableDefNode n) {
        if (n.entity().initializer() != null) {
            visit(new AssignNode(new VariableNode(n.entity()), n.entity().initializer()));
        }
        return null;
    }

    @Override
    public Void visit(VariableNode node) {
        if (!reliedEntityStack.empty()) {
            Entity entity = node.entity();
            if (globalScope.entities().values().contains(entity)) {
                dependenceEdgeSet.add(new DependenceEdge(currentFunction, entity));
                currentFunction.addDependence(entity);
            }
            for (Set<Entity> entities : reliedEntityStack) { // add to all above
                entities.add(entity);
            }
        }

        return null;
    }

    @Override
    public Void visit(FuncallNode node) {
        if (outputRelevant.contains(node.functionType().entity())) {
            reliedEntityStack.push(new HashSet<>());

            visitExpr(node.expr());
            visitExprs(node.args());

            for (Entity entity : reliedEntityStack.peek()) {
                source.add(entity);
            }

            reliedEntityStack.pop();
        } else {
            visitExpr(node.expr());
            visitExprs(node.args());
        }
        return null;
    }

    @Override
    public Void visit(ReturnNode node) {
        return null;
    }

    @Override
    public void visitExpr(ExprNode node) {
        node.accept(this);
    }

    /*
     * setter and getter
     */
    public Set<Entity> source() {
        return source;
    }

    public Set<DependenceEdge> dependenceEdgeSet() {
        return dependenceEdgeSet;
    }
}
