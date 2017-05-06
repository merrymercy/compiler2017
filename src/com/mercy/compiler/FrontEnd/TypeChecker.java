package com.mercy.compiler.FrontEnd;

import com.mercy.compiler.AST.*;
import com.mercy.compiler.Entity.ClassEntity;
import com.mercy.compiler.Entity.Entity;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Entity.ParameterEntity;
import com.mercy.compiler.Type.*;
import com.mercy.compiler.Utility.InternalError;
import com.mercy.compiler.Utility.SemanticError;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by mercy on 17-3-24.
 */
public class TypeChecker extends Visitor {
    Logger logger = Logger.getGlobal();
    static final Type boolType = new BoolType();
    static final Type integerType = new IntegerType();
    static final Type stringType = new StringType();

    private int loopDepth = 0;
    private FunctionEntity currentFunction;

    static private void checkCompatibility(Location loc, Type real, Type expect, boolean isExpected) {
        if (!real.isCompatible(expect)) {
            String message;
            if (isExpected) {
                message = "Invalid type " + real + ", expecting " + expect;
            } else
                message = "Incompatible Type : " + real + " and " + expect;
            throw new SemanticError(loc, message);
        }
    }

    @Override
    public Void visit(FunctionDefNode node) {
        currentFunction = node.entity();
        if (!currentFunction.isConstructor() && currentFunction.returnType() == null)
            throw new SemanticError(node.location(), "expect a return type");
        visitStmt(node.entity().body());
        currentFunction = null;
        return null;
    }

    @Override
    public Void visit(VariableDefNode node) {
        ExprNode init = node.entity().initializer();
        if (init != null) {
            visitExpr(init);
            checkCompatibility(node.location(), init.type(), node.entity().type(), false);
        }
        if (node.entity().type().isVoid())
            throw new SemanticError(node.location(), "Cannot set void type for variable");
        return null;
    }

    @Override
    public Void visit(IfNode node) {
        visitExpr(node.cond());
        if (node.thenBody() != null) {
            visitStmt(node.thenBody());
        }
        if (node.elseBody() != null) {
            visitStmt(node.elseBody());
        }
        checkCompatibility(node.location(), node.cond().type(), boolType, true);
        return null;
    }

    @Override
    public Void visit(WhileNode node) {
        visitExpr(node.cond());
        if (node.body() != null) {
            loopDepth++;
            visitStmt(node.body());
            loopDepth--;
        }
        checkCompatibility(node.location(), node.cond().type(), boolType, true);
        return null;
    }

    @Override
    public Void visit(ForNode node) {
        if (node.init() != null)
            visitExpr(node.init());
        if (node.cond() != null) {
            visitExpr(node.cond());
            checkCompatibility(node.location(), node.cond().type(), boolType, true);
        }
        if (node.init() != null)
            visitExpr(node.incr());
        if(node.body() != null) {
            loopDepth++;
            visitStmt(node.body());
            loopDepth--;
        }
        return null;
    }


    @Override
    public Void visit(BreakNode node) {
        if (loopDepth <= 0)
            throw new SemanticError(node.location(), "unexpected break");
        return null;
    }


    @Override
    public Void visit(ContinueNode node) {
        if (loopDepth <= 0)
            throw new SemanticError(node.location(), "unexpected continue");
        return null;
    }


    @Override
    public Void visit(ReturnNode node) {
        if (currentFunction == null)
            throw new SemanticError(node.location(), "cannot return outside function");

        if (currentFunction != null && currentFunction.isConstructor()) {
            if (node.expr() != null) {
                throw new SemanticError(node.location(), "cannot return in constructor");
            }
        } else {
            if (node.expr() != null) {
                visitExpr(node.expr());
                checkCompatibility(node.location(), node.expr().type(), currentFunction.returnType(), true);
            } else {
                if (!currentFunction.returnType().isVoid()) {
                    throw new SemanticError(node.location(), "cannot return to void");
                }
            }
        }
        return null;
    }

    @Override
    public Void visit(AssignNode node) {
        visitExpr(node.lhs());
        visitExpr(node.rhs());
        if (!node.lhs().isAssignable()) {
            throw new SemanticError(node.location(), "LHS of '=' is not assignable");
        }
        // note !! swap left and right for null
        checkCompatibility(node.location(), node.rhs().type(), node.lhs().type(), false);
        return null;
    }

    @Override
    public Void visit(UnaryOpNode node) {
        visitExpr(node.expr());
        Type expect;
        switch (node.operator()) {
            case PRE_INC: case PRE_DEC: case SUF_INC: case SUF_DEC:
            case MINUS: case ADD: case BIT_NOT:
                expect = integerType; break;
            case LOGIC_NOT:
                expect = boolType; break;
            default:
                throw new InternalError("Invalid operator" + node.operator());
        }
        checkCompatibility(node.location(), node.expr().type(), expect, true);
        return null;
    }

    @Override
    public Void visit(PrefixOpNode node) {
        visit((UnaryOpNode)node);
        if (node.operator() == UnaryOpNode.UnaryOp.PRE_INC ||
                node.operator() == UnaryOpNode.UnaryOp.PRE_DEC) {
            /*System.out.println("haha\n");
            System.out.println(node);*/
            node.setAssignable(true);
        }
        return null;
    }

    @Override
    public Void visit(SuffixOpNode node) {
        visit((UnaryOpNode)node);
        if (!node.expr().isAssignable()) {
            throw new SemanticError(node.location(), "lvalue is needed");
        }
        return null;
    }

    @Override
    public Void visit(BinaryOpNode node) {
        visitExpr(node.left());
        visitExpr(node.right());

        Type ltype = node.left().type(), rtype = node.right().type();
        switch(node.operator()) {
            case MUL : case DIV :case MOD : case SUB:
            case LSHIFT: case RSHIFT:
            case BIT_AND: case BIT_XOR:case BIT_OR:
                checkCompatibility(node.left().location(), ltype, integerType, true);
                checkCompatibility(node.right().location(), rtype, integerType, true);
                node.setType(ltype);
                break;
            case GT: case LE: case GE: case LT:
                checkCompatibility(node.left().location(), ltype, rtype, false);
                if (!ltype.isFullComparable() && !rtype.isFullComparable()) { // ugly, for "null"
                    throw new SemanticError(node.location(), "Cannot compare two " + ltype);
                }
                node.setType(boolType);
                break;
            case EQ: case NE:
                checkCompatibility(node.location(), ltype, rtype, true);
                if (!ltype.isHalfComparable() && !rtype.isHalfComparable()) { // ugly, for "null"
                    throw new SemanticError(node.location(), "Cannot compare two " + ltype);
                }
                node.setType(boolType);
                break;
            case LOGIC_AND: case LOGIC_OR:
                checkCompatibility(node.left().location(), ltype, boolType, true);
                checkCompatibility(node.right().location(), rtype, boolType, true);
                node.setType(ltype);
                break;
            case ADD:
                checkCompatibility(node.location(), ltype, rtype, true);
                if (!ltype.isInteger() && !ltype.isString()) {
                    throw new SemanticError(node.location(), "Cannot add two " + ltype);
                }
                node.setType(ltype);
                break;
            default:
                throw new InternalError("Invalid operator " + node.operator());
        }

        return null;
    }

    @Override
    public Void visit(LogicalOrNode node) {
        visit((BinaryOpNode)node);
        return null;
    }

    @Override
    public Void visit(LogicalAndNode node) {
        visit((BinaryOpNode)node);
        return null;
    }

    @Override
    public Void visit(FuncallNode node) {
        visitExpr(node.expr());

        Type type = node.expr().type();
        if (!type.isFunction())
            throw new SemanticError(node.location(), "Invalid type : " + type
                    + " expecting function");
        FunctionEntity entity = ((FunctionType)type).entity();
        List<ParameterEntity> params = entity.params();
        List<ExprNode> exprs = node.args();

        // count for "this" pointer
        int base = 0;
        if (node.expr() instanceof MemberNode || (node.expr() instanceof VariableNode &&
                                                 ((VariableNode)node.expr()).isMember())) {
            base = 1;
        }

        // check number
        if (params.size() - base != exprs.size()) {
            throw new SemanticError(node.location(), "Incompatible parameter number : "
                    + exprs.size() + ", expecting " + (params.size()-base));
        }

        // check type of parameters
        for (int i = base; i < params.size(); i++) {
            ExprNode expr = exprs.get(i - base);
            visitExpr(expr);
            checkCompatibility(expr.location(),
                    expr.type(), params.get(i).type(), true);
        }

        // add "this" pointer
        if (base != 0) {
            if (node.expr() instanceof MemberNode) {   // A.func(...) -> func(A, ...)
                node.addThisPointer(((MemberNode)node.expr()).expr());
            } else {                                   // memberFunc(...) -> memberFunc(this, ...)
                node.addThisPointer(new VariableNode(params.get(0)));
            }
        }

        return null;
    }

    @Override
    public Void visit(ArefNode node) {
        visitExpr(node.expr());
        visitExpr(node.index());
        if (!node.expr().type().isArray()) {
            throw new SemanticError(node.location(), "Invalid reference of "
                    + node.expr().type() + ", expecting array");
        }
        checkCompatibility(node.index().location(), node.index().type(), integerType, true);
        node.setType(((ArrayType)(node.expr().type())).baseType());
        return null;
    }

    @Override
    public Void visit(CreatorNode node) {
        // check index
        if (node.exprs() != null) {
            for (ExprNode expr : node.exprs()) {
                visitExpr(expr);
                checkCompatibility(expr.location(), expr.type(), integerType, true);
            }
        }
        return null;
    }

    @Override
    public Void visit(MemberNode node) {
        visitExpr(node.expr());
        Type type = node.expr().type();

        if (type.isClass()) {
            ClassEntity entity = ((ClassType) type).entity();
            Entity member = entity.scope().find(node.member());
            if (member == null)
                throw new SemanticError(node.location(), "Cannot resolve member : "
                        + node.member());
            node.setEntity(member);
            node.setType(member.type());
        } else if (type.isArray() || type.isString()){
            Entity member;
            if (type.isArray())
                member = ArrayType.scope().find(node.member());
            else
                member = StringType.scope().find(node.member());

            if (member == null)
                throw new SemanticError(node.location(), "Cannot resolve member : "
                        + node.member());
            node.setEntity(member);
            node.setType(member.type());
        } else {
            throw new SemanticError(node.location(), "Invalid get member operation : "
                    + node.expr().type() + ", expecting class, array or string");
        }
        return null;
    }
}
