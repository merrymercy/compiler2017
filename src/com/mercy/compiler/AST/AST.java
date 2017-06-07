package com.mercy.compiler.AST;

import com.mercy.compiler.Entity.*;
import com.mercy.compiler.FrontEnd.OutputIrrelevantMaker;
import com.mercy.compiler.FrontEnd.SymbolResolver;
import com.mercy.compiler.FrontEnd.TypeChecker;
import com.mercy.compiler.Utility.SemanticError;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.System.err;

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
    public void eliminateOutputIrrelevantNode() {
        if (classEntitsies().size() != 0) {  // don't analyze class type
            return;
        }

        // gather all entity, mark irrelevant default
        Set<Entity> allEntity = scope.gatherAll();
        for (Entity entity : allEntity) {
            entity.setOutputIrrelevant(true);
        }

        // begin iteration
        int before = 0, after = -1;
        OutputIrrelevantMaker analyzer = new OutputIrrelevantMaker(this);
        while (before != after) {
            analyzer.visitDefinitions(definitionNodes);

            // print dependence edge
            err.println("========== EDGE ==========");
            for (Entity entity : allEntity) {
                err.print(entity.name() + " :");
                for (Entity rely : entity.dependence()) {
                    err.print("    " + rely.name());
                }
                err.println();
            }

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
        analyzer.visitDefinitions(definitionNodes);

        // print result
        err.println("========== RES ==========");
        for (Entity entity : allEntity) {
            err.println(entity.name() + ": " + entity.outputIrrelevant());
        }
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
