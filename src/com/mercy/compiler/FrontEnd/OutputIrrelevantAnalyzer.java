package com.mercy.compiler.FrontEnd;

import com.mercy.compiler.AST.*;
import com.mercy.compiler.Entity.ClassEntity;
import com.mercy.compiler.Entity.Entity;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Entity.Scope;
import com.mercy.compiler.Type.ArrayType;
import com.mercy.compiler.Type.ClassType;
import com.mercy.compiler.Type.Type;
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
    Set<DependenceEdge> dependenceEdgeSet = new HashSet<>();

    public OutputIrrelevantAnalyzer(AST ast) {
        globalScope = ast.scope();
        ast.scope().lookup("print").setOutputIrrelevant(false);
        ast.scope().lookup("println").setOutputIrrelevant(false);
    }

    @Override
    public Void visit(ClassDefNode node) {
        visitStmts(node.entity().memberFuncs());
        visitStmts(node.entity().memberVars());
        return null;
    }

    @Override
    public Void visit(FunctionDefNode node) {
        currentFunction = node.entity();
        visitStmt(node.entity().body());

        node.entity().setOutputIrrelevant(node.entity().body().outputIrrelevant());
        node.setOutputIrrelevant(node.entity().body().outputIrrelevant());

        if (currentFunction.name().equals("main"))
            currentFunction.setOutputIrrelevant(false);
        return null;
    }

    Stack<Set<Entity>> collectSetStack = new Stack<>();

    // for entity - entity dependency
    Stack<Set<Entity>> reliedEntityStack = new Stack<>();

    // for node - entity dependency
    Stack<Boolean> irrelevantStack = new Stack<>();

    @Override
    public Void visit(BlockNode node) {
        if (!collectSetStack.empty()) { // in collect mode
            super.visit(node);
        } else {
            irrelevantStack.push(true);
            visitStmts(node.stmts());
            node.setOutputIrrelevant(irrelevantStack.peek());
            irrelevantStack.pop();
        }
        return null;
    }


    private Entity getBaseEntity(ExprNode node) {
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
        node.setOutputIrrelevant(false);
        if (!collectSetStack.empty()) { // in collect mode
            super.visit(node);
        } else {
            ExprNode lhs = node.lhs();
            if ((lhs.type() instanceof ArrayType || lhs.type() instanceof ClassType) && !(node.rhs() instanceof CreatorNode)) { // don't do
                // mark all the entity to save
                collectSetStack.push(new HashSet<>());

                visitExpr(node.lhs());
                visitExpr(node.rhs());

                for (Entity entity : collectSetStack.peek()) {
                    entity.setOutputIrrelevant(false);
                }
                collectSetStack.pop();

                visitExpr(node.lhs());
                visitExpr(node.rhs());
            } else {
                // add dependency edge
                Entity base = getBaseEntity(lhs);

                reliedEntityStack.push(new HashSet<>());
                visitExpr(node.lhs());
                visitExpr(node.rhs());

                for (Entity entity : reliedEntityStack.peek()) {
                    dependenceEdgeSet.add(new DependenceEdge(base, entity));
                    base.addDependence(entity);
                }
                reliedEntityStack.pop();

                if (base.outputIrrelevant()) {
                    node.setOutputIrrelevant(true);
                }
            }
        }
        return null;
    }

    @Override
    public Void visit(VariableDefNode n) {
        if (n.entity().initializer() != null) {
            visit(new AssignNode(new VariableNode(n.entity(), n.location()), n.entity().initializer()));
        }
        return null;
    }

    @Override
    public Void visit(VariableNode node) {
        if (currentFunction == null) // ignore global defs
            return null;

        if (!collectSetStack.empty()) {  // in collect mode
            for (Set<Entity> entities : collectSetStack) {
                entities.add(node.entity());
            }
        } else {
            // add entity-entity edge
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
            // mark above node to save
            if (!node.entity().outputIrrelevant()) {
                for (int i = 0; i< irrelevantStack.size(); i++)
                    irrelevantStack.set(i, false);
            }
        }
        return null;
    }

    @Override
    public Void visit(MemberNode node) {
        visitExpr(node.expr());
        visitExpr(new VariableNode(node.entity()));
        return null;
    }

    @Override
    public Void visit(FuncallNode node) {
        if (!collectSetStack.empty()) { // in collect mode
            super.visit(node);
        } else {
            currentFunction.addDependence(node.functionType().entity()); // FIXME
            if (!node.functionType().entity().outputIrrelevant()) {
                collectSetStack.push(new HashSet<>()); // collect source
                visitExpr(node.expr());
                visitExprs(node.args());

                for (Entity entity : collectSetStack.peek()) {
                    entity.setOutputIrrelevant(false);
                }
                collectSetStack.pop();

                // mark above block to save
                for (int i = 0; i< irrelevantStack.size(); i++)
                    irrelevantStack.set(i, false);
            } else {
                visitExpr(node.expr());
                visitExprs(node.args());
            }
        }
        return null;
    }

    @Override
    public Void visit(ForNode n) {
        if (!collectSetStack.empty()) { // in collect mode
            super.visit(n);
        } else {
            if (n.init() != null)
                visitExpr(n.init());
            if (n.cond() != null)
                visitExpr(n.cond());
            if (n.incr() != null)
                visitExpr(n.incr());
            if (n.body() != null)
                visitStmt(n.body());

            if (n.body() != null && !n.body().outputIrrelevant()) {  // node - entity iteration
                collectSetStack.push(new HashSet<>());

                if (n.init() != null)
                    visitExpr(n.init());
                if (n.cond() != null)
                    visitExpr(n.cond());
                if (n.incr() != null)
                    visitExpr(n.incr());

                for (Entity entity : collectSetStack.peek()) {
                    entity.setOutputIrrelevant(false);
                }

                collectSetStack.pop();
                n.setOutputIrrelevant(false);
            } else {
                n.setOutputIrrelevant(true);
            }
        }
        return null;
    }

    @Override
    public Void visit(WhileNode n) {
        if (!collectSetStack.empty()) { // in collect mode
            super.visit(n);
        } else {
            if (n.cond() != null)
                visitExpr(n.cond());
            if (n.body() != null)
                visitStmt(n.body());


            if (n.body() != null && !n.body().outputIrrelevant()) {  // node - entity iteration
                collectSetStack.push(new HashSet<>());

                if (n.cond() != null)
                    visitExpr(n.cond());

                for (Entity entity : collectSetStack.peek()) {
                    entity.setOutputIrrelevant(false);
                }

                collectSetStack.pop();
                n.setOutputIrrelevant(false);
            } else {
                n.setOutputIrrelevant(true);
            }
        }
        return null;
    }

    @Override
    public Void visit(IfNode n) {
        if (!collectSetStack.empty()) { // in collect mode
            super.visit(n);
        } else {
            visitExpr(n.cond());
            if (n.thenBody() != null) {
                visitStmt(n.thenBody());
            }
            if (n.elseBody() != null) {
                visitStmt(n.elseBody());
            }
            if ((n.thenBody() != null && !n.thenBody().outputIrrelevant()) ||
                (n.elseBody() != null && n.elseBody().outputIrrelevant())) { // node - entity iteration

                collectSetStack.push(new HashSet<>());

                if (n.cond() != null)
                    visitExpr(n.cond());
                for (Entity entity : collectSetStack.peek()) {
                    entity.setOutputIrrelevant(false);
                }

                collectSetStack.pop();
                n.setOutputIrrelevant(false);
            } else {
                n.setOutputIrrelevant(true);
            }
        }
        return null;
    }

    @Override
    public Void visit(ReturnNode node) {
        if (!collectSetStack.empty()) { // in collect mode
            super.visit(node);
        } else {
            if (node.expr() != null) {
                collectSetStack.push(new HashSet<>());
                visitExpr(node.expr());

                for (Entity entity : collectSetStack.peek()) {
                    dependenceEdgeSet.add(new DependenceEdge(currentFunction, entity));
                    currentFunction.addDependence(entity);
                }

                collectSetStack.pop();
            }
        }
        return null;
    }

    @Override
    public Void visit(CreatorNode node) {
        FunctionEntity constructor = null;
        if (node.type() instanceof ArrayType) {
            Type deepType = ((ArrayType) node.type()).deepType();
            if (node.exprs().size() == node.total() && deepType instanceof ClassType)
                constructor = ((ClassType)deepType).entity().constructor();
        } else {
            ClassEntity entity = ((ClassType) node.type()).entity();
            constructor = entity.constructor();
        }

        if (constructor != null) {
            visitExpr(new VariableNode(constructor));
        }

        if (node.exprs() != null)
            visitExprs(node.exprs());
        return null;
    }


    @Override
    public void visitExpr(ExprNode node) {
        node.accept(this);
    }

    public Set<DependenceEdge> dependenceEdgeSet() {
        return dependenceEdgeSet;
    }
}
