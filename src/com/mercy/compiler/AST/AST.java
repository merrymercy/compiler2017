package com.mercy.compiler.AST;

import com.mercy.compiler.Entity.*;
import com.mercy.compiler.FrontEnd.*;
import com.mercy.compiler.Utility.SemanticError;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mercy on 17-3-18.
 */
public class AST {
    private Scope scope;
    private List<DefinitionNode> definitionNodes;
    private List<ClassEntity> classEntitsies;
    private List<FunctionEntity> functionEntities;
    private List<VariableEntity> variableEntities;

    public AST(List<DefinitionNode> definitionNodes, List<ClassEntity> definedClass,
               List<FunctionEntity> definedFunction, List<VariableEntity> definedVariable) {
        super();
        this.definitionNodes = definitionNodes;
        this.classEntitsies = definedClass;
        this.functionEntities = definedFunction;
        this.variableEntities = definedVariable;
        this.scope = new Scope(true);
    }

    public void loadLibrary(List<Entity> entities) {
        for (Entity entity : entities) {
            scope.insert(entity);
        }
    }

    public void resolveSymbol() {
        // put function entity and class entity into scope
        for (ClassEntity entity : classEntitsies) {
            scope.insert(entity);
        }

        for (FunctionEntity entity : functionEntities) {
            scope.insert(entity);
        }

        // visit definitions
        SymbolResolver resolver = new SymbolResolver(scope);
        resolver.visitDefinitions(definitionNodes);
    }

    public void checkType() {
        TypeChecker checker = new TypeChecker();
        checker.visitDefinitions(definitionNodes);
        FunctionEntity mainFunc = (FunctionEntity)scope.lookup("main");
        if (mainFunc == null) {
            throw new SemanticError(new Location(0,0), "main undefined");
        }
        if (!mainFunc.returnType().isInteger()) {
            throw new SemanticError(new Location(0, 0), "main must return a integer");
        }
    }

    Set<DependenceEdge> visited = new HashSet<>();
    private void propaOutputIrrelevant(Entity entity, boolean flag) {
        DependenceEdge edge = new DependenceEdge(null, null);
        entity.setOutputIrrelevant(flag);
        for (Entity base : entity.dependence()) {
            edge.base = base; edge.rely = entity;
            if (!visited.contains(edge)) {
                visited.add(new DependenceEdge(base, entity));
                propaOutputIrrelevant(base, flag);
            }
        }
    }

    public void eliminateOutputIrrelevantNode() {
        OutputIrrelevantAnalyzer analyzer = new OutputIrrelevantAnalyzer(this);
        analyzer.visitDefinitions(definitionNodes);

        // print dependence info
        for (DependenceEdge edge : analyzer.dependenceEdgeSet()) {
            System.err.println(edge.base.name() + " <- " + edge.rely.name());
        }

        // propagate info
        System.err.print("source:");
        for (Entity entity : analyzer.source()) {
            System.err.print("  " + entity.name());
        }
        System.err.println("");

        for (Entity entity : analyzer.source()) {
            propaOutputIrrelevant(entity, false);
        }
        // print result
        for (VariableEntity entity : scope.allLocalVariables()) {
            System.err.println(entity.name() + ": " + entity.outputIrrelevant());
        }
        for (FunctionEntity entity : functionEntities) {
            System.err.println(entity.name() + ": " + entity.outputIrrelevant());
        }

        OutputIrrelevantMarker marker = new OutputIrrelevantMarker(this);
        marker.visitDefinitions(definitionNodes);
    }


    public Scope scope() {
        return scope;
    }

    public List<DefinitionNode> definitionNodes() {
        return definitionNodes;
    }

    public List<ClassEntity> classEntitsies() {
        return classEntitsies;
    }

    public List<FunctionEntity> functionEntities() {
        return functionEntities;
    }

    public List<VariableEntity> variableEntities() {
        return variableEntities;
    }
}
