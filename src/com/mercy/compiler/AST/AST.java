package com.mercy.compiler.AST;

import com.mercy.compiler.Entity.*;
import com.mercy.compiler.FrontEnd.DependenceEdge;
import com.mercy.compiler.FrontEnd.OutputIrrelevantAnalyzer;
import com.mercy.compiler.FrontEnd.SymbolResolver;
import com.mercy.compiler.FrontEnd.TypeChecker;
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
        if (flag)
            return;
        DependenceEdge edge = new DependenceEdge(null, null);
        entity.setOutputIrrelevant(false);
        if (entity instanceof FunctionEntity) {
            for (ParameterEntity parameterEntity : ((FunctionEntity) entity).params()) {
                parameterEntity.setOutputIrrelevant(false);
            }
        }
        for (Entity base : entity.dependence()) {
            edge.base = base; edge.rely = entity;
            if (!visited.contains(edge)) {
                visited.add(new DependenceEdge(base, entity));
                propaOutputIrrelevant(base, false);
            }
        }
    }

    public void eliminateOutputIrrelevantNode() {
        if (classEntitsies().size() != 0) {
            return;
        } else {
            OutputIrrelevantAnalyzer analyzer = new OutputIrrelevantAnalyzer(this);
            analyzer.visitDefinitions(definitionNodes);

            // gather all entity, mark irrelevant default
            List<Entity> allEntity = scope.gatherAll();
            for (Entity entity : allEntity) {
                entity.setOutputIrrelevant(true);
            }

            // print dependence info
           /* HashSet<Entity> printed = new HashSet<>();
            for (DependenceEdge edge : analyzer.dependenceEdgeSet()) {
                if (printed.contains(edge.base))
                    continue;
                printed.add(edge.base);
                System.err.print(edge.base.name() + ": ");
                for (Entity entity : edge.base.dependence()) {
                    System.err.print("  " + entity.name());
                }
                System.err.println();
            }*/

            // print source info

            // begin iteration
            int before = 0, after = -1;

            while(before != after) {
                analyzer.visitDefinitions(definitionNodes);
                visited.clear();
                before = after;
                after = 0;
                for (Entity entity : allEntity) {
                    propaOutputIrrelevant(entity, entity.outputIrrelevant());
                }
                for (Entity entity : allEntity) {
                    if (!entity.outputIrrelevant())
                        after++;
                }
            }
            analyzer.visitDefinitions(definitionNodes);

            // print result
            /*for (Entity entity : allEntity) {
                if (entity instanceof FunctionEntity)
                    continue;
                System.err.println(entity.name() + ": " + entity.outputIrrelevant());
            }*/
        }
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
