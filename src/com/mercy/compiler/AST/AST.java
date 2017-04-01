package com.mercy.compiler.AST;

import com.mercy.compiler.Entity.*;
import com.mercy.compiler.FrontEnd.SymbolResolver;
import com.mercy.compiler.FrontEnd.TypeChecker;
import com.mercy.compiler.Type.*;
import com.mercy.compiler.Utility.LibFunction;
import com.mercy.compiler.Utility.SemanticError;

import java.util.List;

import static com.mercy.compiler.Type.Type.*;

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

    public void loadLibrary() {
        // lib function
        scope.insert(new LibFunction(voidType, "print", new Type[]{stringType}).getEntity());
        scope.insert(new LibFunction(voidType, "println", new Type[]{stringType}).getEntity());
        scope.insert(new LibFunction(stringType, "getString", null).getEntity());
        scope.insert(new LibFunction(integerType, "getInt", null).getEntity());
        scope.insert(new LibFunction(stringType, "toString", new Type[]{integerType}).getEntity());
        // null
        scope.insert(new VariableEntity(null, nullType, "null", null));
        // built in function for string and array
        Type.initializeBuiltinType();
    }

    public void resolveSymbol() {
        // DEBUG ONLY
        loadLibrary();

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
