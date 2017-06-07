package com.mercy.compiler.FrontEnd;

import com.mercy.compiler.AST.DefinitionNode;
import com.mercy.compiler.AST.Location;
import com.mercy.compiler.Entity.*;
import com.mercy.compiler.Utility.SemanticError;

import java.util.List;

/**
 * Created by mercy on 17-3-18.
 */
public class AST {
    private Scope scope;
    private List<DefinitionNode> definitionNodes;
    private List<ClassEntity> classEntities;
    private List<FunctionEntity> functionEntities;
    private List<VariableEntity> variableEntities;

    public AST(List<DefinitionNode> definitionNodes, List<ClassEntity> definedClass,
               List<FunctionEntity> definedFunction, List<VariableEntity> definedVariable) {
        super();
        this.definitionNodes  = definitionNodes;
        this.classEntities    = definedClass;
        this.functionEntities = definedFunction;
        this.variableEntities = definedVariable;
        this.scope = new Scope(true);
    }

    public void loadLibrary(List<Entity> entities) {
        for (Entity entity : entities) {
            scope.insert(entity);
        }
    }

    /*
     * semantic check
     */
    public void resolveSymbol() {
        // put function entity and class entity into scope
        for (ClassEntity entity : classEntities) {
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
        TypeChecker checker = new TypeChecker(scope);
        checker.visitDefinitions(definitionNodes);

        // ckeck main function
        FunctionEntity mainFunc = (FunctionEntity)scope.lookup("main");
        if (mainFunc == null) {
            throw new SemanticError(new Location(0,0), "main undefined");
        }
        if (!mainFunc.returnType().isInteger()) {
            throw new SemanticError(new Location(0, 0), "main must return a integer");
        }
    }

    /*
     * output irrelevant analyze
     */
    public void eliminateOutputIrrelevantNode() {
        if (classEntitsies().size() != 0) {  // don't analyze class type
            return;
        }

        OutputIrrelevantMaker analyzer = new OutputIrrelevantMaker(this);
        analyzer.visitDefinitions(definitionNodes);
    }

    public Scope scope() {
        return scope;
    }

    public List<DefinitionNode> definitionNodes() {
        return definitionNodes;
    }

    public List<ClassEntity> classEntitsies() {
        return classEntities;
    }

    public List<FunctionEntity> functionEntities() {
        return functionEntities;
    }

    public List<VariableEntity> variableEntities() {
        return variableEntities;
    }
}
