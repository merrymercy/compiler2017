package com.mercy.compiler.FrontEnd;

import com.mercy.compiler.AST.*;
import com.mercy.compiler.Entity.*;
import com.mercy.compiler.Type.*;
import com.mercy.compiler.Utility.SemanticError;

import java.util.Stack;

/**
 * Created by mercy on 17-3-24.
 */
public class SymbolResolver extends Visitor {
    private Stack<Scope> stack = new Stack<>();
    private Scope currentScope;
    private ClassEntity currentClass = null;
    private boolean firstBlockInFunction = false;

    public SymbolResolver(Scope toplevelScope) {
        currentScope = toplevelScope;
        stack.push(currentScope);
    }

    private void enterScope() {
        currentScope = new Scope(currentScope);
        stack.push(currentScope);
    }

    private void exitScope() {
        stack.pop();
        currentScope = stack.peek();
    }

    private void enterClass(ClassEntity entity) {
        currentClass = entity;
        enterScope();
        entity.setScope(currentScope);
    }

    private void exitClass() {
        exitScope();
        currentClass = null;
    }

    // set entity for type
    private boolean resolveType(Type type) {
        if (type instanceof ClassType) {
            ClassType t = (ClassType) type;
            Entity entity = currentScope.lookup(t.name());
            if (entity == null || !(entity instanceof ClassEntity))
                return false;
            t.setEntity((ClassEntity)entity);
        } else if (type instanceof FunctionType) {
            FunctionType t = (FunctionType) type;
            Entity entity = currentScope.lookup(t.name());
            if (entity == null || !(entity instanceof FunctionEntity))
                return false;
            t.setEntity((FunctionEntity)entity);
        } else if (type instanceof ArrayType) {
            ArrayType t = (ArrayType) type;
            return resolveType(t.baseType());
        }
        return true;
    }

    @Override
    public Void visit(FunctionDefNode node) {
        FunctionEntity entity = node.entity();
        enterScope();
        entity.setScope(currentScope);
        if (!resolveType(entity.returnType())) {
            throw new SemanticError(node.location(), "Cannot resolve symbol : " + entity.returnType());
        }

        // if it is a member function, add "this" pointer parameter
        if (currentClass != null) {
            entity.addThisPointer(node.location(), currentClass);
        }
        // add parameters into scope
        for (ParameterEntity param : entity.params()) {
            currentScope.insert(param);
            if (!resolveType(param.type())) {
                throw new SemanticError(node.location(), "Cannot resolve symbol : " + param.type());
            }
        }
        firstBlockInFunction = true;
        visit(entity.body());

        exitScope();
        return null;
    }

    @Override
    public Void visit(ClassDefNode node) {
        ClassEntity entity = node.entity();
        enterClass(entity);

        // add members into scope
        for (VariableDefNode memberVar : entity.memberVars()) {
            currentScope.insert(new MemberEntity(memberVar.entity()));
        }
        for (FunctionDefNode memberFunc : entity.memberFuncs()) {
            currentScope.insert(memberFunc.entity());
        }

        // visit members
        visitStmts(entity.memberVars());
        visitStmts(entity.memberFuncs());

        exitClass();
        return null;
    }

    @Override
    public Void visit(VariableDefNode node) {
        VariableEntity entity = node.entity();
        if (!resolveType(entity.type())) {
            throw new SemanticError(node.location(), "Cannot resolve symbol : " + entity.type());
        }
        if (currentClass == null || currentClass.scope() != currentScope) {
            if (entity.initializer() != null)
                visitExpr(entity.initializer());
            currentScope.insert(entity);
        }
        return null;
    }

    @Override
    public Void visit(StringLiteralNode node) {
        Entity entity = currentScope.lookup(StringType.STRING_CONSTANT_PREFIX + node.value());
        if (entity == null) {
            entity = new StringConstantEntity(node.location(), new StringType(), node.value(), node);
            currentScope.insertConstant(entity);
        }
        node.setEntity((StringConstantEntity) entity);
        return null;
    }

    @Override
    public Void visit(CreatorNode node) {
        if (!resolveType(node.type())) {
            throw new SemanticError(node.location(), "Cannot resolve symbol : " + node.type());
        }
        if (node.exprs() != null)
            visitExprs(node.exprs());
        return null;
    }

    @Override
    public Void visit(BlockNode node) {
        if (firstBlockInFunction) {
            firstBlockInFunction = false;
            node.setScope(currentScope);
            visitStmts(node.stmts());
        } else {
            enterScope();
            node.setScope(currentScope);
            visitStmts(node.stmts());
            exitScope();
        }
        return null;
    }

    @Override
    public Void visit(VariableNode node) {
        Entity entity = currentScope.lookup(node.name());
        if (entity == null)
            throw new SemanticError(node.location(), "cannot resolve symbol : " + node.name());
        node.setEntity(entity);

        if (currentClass != null && currentClass.scope().find(node.name()) != null) {
            node.setMember(true);
        }

        return null;
    }
}
