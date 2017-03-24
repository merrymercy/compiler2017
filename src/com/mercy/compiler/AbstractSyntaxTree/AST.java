package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Entity.ClassEntity;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Entity.Scope;
import com.mercy.compiler.Utility.InternalError;

import java.util.List;

/**
 * Created by mercy on 17-3-18.
 */
public class AST {
    private Scope scope;
    private List<DefinitionNode> definitionNodes;
    private List<ClassEntity> classEntitsies;
    private List<FunctionEntity> functionEntities;

    public AST(List<DefinitionNode> definitionNodes,  List<ClassEntity> definedClass, List<FunctionEntity> definedFunction) {
        super();
        this.definitionNodes = definitionNodes;
        this.classEntitsies = definedClass;
        this.functionEntities = definedFunction;
        this.scope = new Scope(true);
    }

    public void loadLiabrary() {
        String[] name = {"print", "println", "getString",
        "getInt", "toString", "length", "substring", "parseInt", "ord"};

        for (String s : name) {
            scope.insert(new FunctionEntity(null, null, s, null, null));
        }
    }

    public void resolveSymbol() {
        // DEBUG ONLY
        loadLiabrary();

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
}
