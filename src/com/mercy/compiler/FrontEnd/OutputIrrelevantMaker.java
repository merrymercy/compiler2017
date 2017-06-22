package com.mercy.compiler.FrontEnd;

import com.mercy.compiler.AST.*;
import com.mercy.compiler.Entity.*;
import com.mercy.compiler.Option;
import com.mercy.compiler.Type.ArrayType;
import com.mercy.compiler.Type.ClassType;
import com.mercy.compiler.Utility.InternalError;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import static java.lang.System.err;

/**
 * Created by mercy on 17-5-22.
 */
public class OutputIrrelevantMaker extends com.mercy.compiler.FrontEnd.Visitor {
    private Scope globalScope;
    private Set<Entity> globalVariables = new HashSet<>();

    // collect mode
    private Set<Entity> collectSet;

    // dependency
    private Stack<Entity>      assignDependenceStack = new Stack<>();
    private Stack<Set<Entity>> controlDependenceStack = new Stack<>();
    private FunctionEntity     currentFunction;
    private FunctionEntity     mainFunction;

    public OutputIrrelevantMaker(AST ast) {
        globalScope = ast.scope();
        for (Entity entity : ast.scope().entities().values()) {
            if (entity instanceof VariableEntity)
                globalVariables.add(entity);
            if (entity instanceof FunctionEntity && entity.name().equals("main"))
                mainFunction = (FunctionEntity)entity;
        }
        currentFunction = mainFunction;
    }

    /*
     * Elimination Iteration
     */
    public class DependenceEdge {
        Entity base, rely;
        DependenceEdge (Entity base, Entity rely) {
            this.base = base;
            this.rely = rely;
        }

        @Override
        public int hashCode() {
            return base.hashCode() + rely.hashCode();
        }
        @Override
        public boolean equals(Object o) {
            return o instanceof DependenceEdge
                    && base == ((DependenceEdge)o).base
                    && rely == ((DependenceEdge)o).rely;
        }
    }

    private Set<DependenceEdge> visited = new HashSet<>();
    private void propaOutputIrrelevant(Entity entity) {
        if (entity.outputIrrelevant())
            return;

        for (Entity rely : entity.dependence()) {
            DependenceEdge edge = new DependenceEdge(entity, rely);
            if (!visited.contains(edge)) {
                visited.add(edge);
                rely.setOutputIrrelevant(false);
                propaOutputIrrelevant(rely);
            }
        }
    }

    @Override
    public void visitDefinitions(List<? extends DefinitionNode> defs) {
        // gather all entity, mark irrelevant default
        Set<Entity> allEntity = globalScope.gatherAll();
        for (Entity entity : allEntity) {
            entity.setOutputIrrelevant(true);
        }

        // init
        globalScope.lookup("print").setOutputIrrelevant(false);
        globalScope.lookup("println").setOutputIrrelevant(false);
        mainFunction.setOutputIrrelevant(false);

        // begin iteration
        int before = 0, after = -1;
        while (before != after) {
            for (DefinitionNode definitionNode : defs)
                visitDefinition(definitionNode);

            before = after;
            after = 0;
            for (Entity entity : allEntity) {
                propaOutputIrrelevant(entity);
            }
            for (Entity entity : allEntity) {
                if (!entity.outputIrrelevant())
                    after++;
            }

        }
        // final iteration to confirm irrelevant information of node
        for (DefinitionNode definitionNode : defs)
            visitDefinition(definitionNode);
        visited.clear();

        if (Option.printIrrelevantMarkInfo) {
            // print dependence edge
            err.println("========== EDGE ==========");
            for (Entity entity : allEntity) {
                err.print(entity.name() + " :");
                for (Entity rely : entity.dependence()) {
                    err.print("    " + rely.name());
                }
                err.println();
            }

            // print result
            err.println("========== RES ==========");
            for (Entity entity : allEntity) {
                err.println(entity.name() + ": " + entity.outputIrrelevant());
            }
        }
    }

    /*
     * Visitors
     */
    @Override
    public Void visit(ClassDefNode node) {
        visitStmts(node.entity().memberFuncs());
        visitStmts(node.entity().memberVars());
        return null;
    }

    @Override
    public Void visit(FunctionDefNode node) {
        currentFunction = node.entity();

        for (ParameterEntity param : currentFunction.params()) {
            currentFunction.addDependence(param);
        }

        visitStmt(currentFunction.body());
        currentFunction = mainFunction;
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
    public Void visit(AssignNode node) {
        if (isInCollectMode()) { // in collect mode
            super.visit(node);
        } else {
            ExprNode lhs = node.lhs();
            if ((lhs.type() instanceof ArrayType || lhs.type() instanceof ClassType) && !(node.rhs() instanceof CreatorNode)) {
                beginCollect();
                visitExpr(node.lhs());
                visitExpr(node.rhs());
                for (Entity entity : fetchCollect()) {
                    entity.setOutputIrrelevant(false); // set source
                }
                if (currentFunction != null)
                    currentFunction.setOutputIrrelevant(false);
            } else {
                int backupSideEffect = sideEffect;

                Entity base = getBaseEntity(lhs);
                assignDependenceStack.push(base);
                visitExpr(node.lhs());
                visitExpr(node.rhs());
                assignDependenceStack.pop();

                if (currentFunction != null && globalVariables.contains(base)) {
                    base.addDependence(currentFunction);
                }

                if (base.outputIrrelevant() && sideEffect == backupSideEffect)
                    node.setOutputIrrelevant(true);
                else
                    node.setOutputIrrelevant(false);

                sideEffect = backupSideEffect;
            }
        }
        return null;
    }

    @Override
    public Void visit(VariableNode node) {
        if (isInCollectMode()) {  // in collect mode
            collectSet.add(node.entity());
        } else {
            Entity entity = node.entity();
            // add global dependency edge
            if (currentFunction != null && globalVariables.contains(entity)) {
                currentFunction.addDependence(entity);
            }
            // add assign dependency edge
            for (Entity base : assignDependenceStack) { // add to all above
                base.addDependence(entity);
            }
            // add control dependency edge
            for (Entity control : getAllControlVars()) {
                entity.addDependence(control);
            }
        }
        return null;
    }

    @Override
    public Void visit(FuncallNode node) {
        if (isInCollectMode()) { // in collect mode
            super.visit(node);
        } else {
            if (!node.functionType().entity().outputIrrelevant()) {
                beginCollect();
                visitExpr(node.expr());
                visitExprs(node.args());

                for (Entity entity : fetchCollect()) {
                    entity.setOutputIrrelevant(false); // set source
                }
                for (Entity entity : getAllControlVars()) {
                    entity.setOutputIrrelevant(false);
                }
                currentFunction.setOutputIrrelevant(false);
            } else {
                visitExpr(node.expr());
                visitExprs(node.args());
            }
        }

        return null;
    }

    @Override
    public Void visit(ReturnNode node) {
        if (isInCollectMode()) { // in collect mode
            super.visit(node);
        } else {
            if (node.expr() != null) {
                beginCollect();
                visitExpr(node.expr());
                for (Entity entity : fetchCollect()) {
                    currentFunction.addDependence(entity);
                }
                for (Entity entity : getAllControlVars()) {
                    currentFunction.addDependence(entity);
                }
            }
        }
        return null;
    }

    @Override
    public Void visit(ForNode n) {
        if (isInCollectMode()) { // in collect mode
            super.visit(n);
        } else {
            beginCollect();
            if (n.init() != null)
                visitExpr(n.init());
            if (n.cond() != null)
                visitExpr(n.cond());
            if (n.incr() != null)
                visitExpr(n.incr());

            Set<Entity> controlVars = new HashSet<>();
            controlVars.addAll(fetchCollect());

            controlDependenceStack.push(controlVars);
            if (n.init() != null)
                visitExpr(n.init());
            if (n.cond() != null)
                visitExpr(n.cond());
            if (n.incr() != null)
                visitExpr(n.incr());
            if (n.body() != null)
                visitStmt(n.body());
            controlDependenceStack.pop();

            markNode(n, controlVars);
        }
        return null;
    }

    @Override
    public Void visit(WhileNode n) {
        if (isInCollectMode()) { // in collect mode
            super.visit(n);
        } else {
            beginCollect();
            visitExpr(n.cond());

            Set<Entity> controlVars = new HashSet<>();
            controlVars.addAll(fetchCollect());

            controlDependenceStack.push(controlVars);
            visitExpr(n.cond());
            if (n.body() != null)
                visitStmt(n.body());
            controlDependenceStack.pop();

            markNode(n, controlVars);
        }
        return null;
    }

    @Override
    public Void visit(IfNode n) {
        if (isInCollectMode()) { // in collect mode
            super.visit(n);
        } else {
            beginCollect();
            visitExpr(n.cond());
            Set<Entity> controlVars = new HashSet<>();
            controlVars.addAll(fetchCollect());


            controlDependenceStack.push(controlVars);
            visitExpr(n.cond());
            if (n.thenBody() != null) {
                visitStmt(n.thenBody());
            }
            if (n.elseBody() != null) {
                visitStmt(n.elseBody());
            }
            controlDependenceStack.pop();

            markNode(n, controlVars);
        }
        return null;
    }

    private int sideEffect = 0;
    @Override
    public Void visit(PrefixOpNode node) {
        if (isInCollectMode()) {
            return super.visit(node);
        } else {
            visitExpr(node.expr());
            if (node.operator() == UnaryOpNode.UnaryOp.PRE_DEC || node.operator() == UnaryOpNode.UnaryOp.PRE_INC) {
                sideEffect++;
            }
            return null;
        }
    }

    @Override
    public Void visit(SuffixOpNode node) {
        if (isInCollectMode()) {
            return super.visit(node);
        } else {
            visitExpr(node.expr());
            if (node.operator() == UnaryOpNode.UnaryOp.SUF_DEC || node.operator() == UnaryOpNode.UnaryOp.SUF_INC) {
                sideEffect++;
            }
            return null;
        }
    }

    /*
     * Utility
     */
    private boolean isInCollectMode() {
        return collectSet != null;
    }
    private void beginCollect() {
        collectSet = new HashSet<>();
    }

    private Set<Entity> fetchCollect() {
        Set<Entity> ret = collectSet;
        collectSet = null;
        return ret;
    }

    private Set<Entity> getAllControlVars() {
        Set<Entity> ret = new HashSet<>();
        for (Set<Entity> entitySet : controlDependenceStack) {
            ret.addAll(entitySet);
        }
        return ret;
    }

    private Entity getBaseEntity(ExprNode node) {
        if (node instanceof ArefNode) {
            return getBaseEntity(((ArefNode) node).baseExpr());
        } else if (node instanceof MemberNode) {
            return getBaseEntity(((MemberNode) node).expr());
        } else if (node instanceof VariableNode) {
            return ((VariableNode) node).entity();
        }
        throw new InternalError("something cannot happen happened in getBaseEntity " + node);
    }

    private void markNode(Node node, Set<Entity> controlVars) {
        if (controlVars.size() == 0) {      // case like while(true) ,whil
            node.setOutputIrrelevant(false);
        } else {
            boolean irrelevant = true;
            for (Entity controlVar : controlVars) {
                if (!controlVar.outputIrrelevant())
                    irrelevant = false;
            }
            node.setOutputIrrelevant(irrelevant);
        }
    }
}