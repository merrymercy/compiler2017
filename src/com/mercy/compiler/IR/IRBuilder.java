package com.mercy.compiler.IR;

import com.mercy.compiler.AST.*;
import com.mercy.compiler.Entity.ClassEntity;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Entity.StringConstantEntity;
import com.mercy.compiler.Type.StringType;
import com.mercy.compiler.Type.Type;
import com.mercy.compiler.Utility.InternalError;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mercy on 17-3-30.
 */
public class IRBuilder {
    public IRBuilder(AST abstractSemanticTree) {}
    public void generateIR() {

    }
}

/*public class IRBuilder extends ASTVisitor<Void, Expr> {

    private List<IR> stmts = new LinkedList<>();
    private AST ast;
    private int exprDepth = 0;

    public IRBuilder(AST abstractSemanticTree) {
        this.ast = abstractSemanticTree;
    }

    public void generateIR() {
        List<IR> globalInitializer;

        // gather global variable initialization
        for (DefinitionNode node : ast.definitionNodes()) {
            if (node instanceof VariableDefNode) {
                visit((VariableDefNode)node);
            }
        }
        globalInitializer = fetchStmts();

        // generate global functions
        for (FunctionEntity entity : ast.functionEntities()) {
            compileFunction(entity);
            if (entity.name().equals("main")) {
            }
        }

        // generate in-class functions
        for (ClassEntity entity: ast.classEntitsies()) {
            for (FunctionDefNode node : entity.memberFuncs()) {
                compileFunction(node.entity());
            }
        }

        // generate built-in functions ?
    }

    public void compileFunction(FunctionEntity entity) {
        // body
        visit(entity.body());

        entity.setIR(fetchStmts());
    }

    public Void visit(BlockNode node) {
        for (StmtNode stmt : node.stmts()) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(VariableDefNode node) {
        ExprNode init = node.entity().initializer();
        if (init != null)
            addAssign(new Var(node.entity()), visitExpr(init)));
        return null;
    }

    public Expr visitExpr(ExprNode node) {
        return node.accept(this);
    }

    public void visitStmt(StmtNode node) {
        node.accept(this);
    }

    @Override
    public Expr visit(AssignNode node) {
        Expr lhs = visitExpr(node.lhs());
        Expr rhs = visitExpr(node.rhs());
        if (exprDepth == 0) { // is statement, which is always true in Mx* 2017
            if (lhs instanceof Mem) {
                addAssign(((Mem)lhs).expr(), rhs);
            } else if (lhs instanceof Var) {
                addAssign(lhs, rhs);
            } else {
                throw new InternalError(node.location(), "unhandled case");
            }
        } else {
            throw new InternalError(node.location(), "Undefined behavior in assign expression");
        }

        return null;
    }

    @Override
    public Expr visit(BinaryOpNode node) {
        Expr lhs = visitExpr(node.left()), rhs = visitExpr(node.right());
        // simple constant folding for integer and string
        if (lhs instanceof IntConst && rhs instanceof IntConst) {
            int lvalue = ((IntConst)lhs).value(), rvalue = ((IntConst)rhs).value();
            switch (node.operator()) {
                case ADD: return new IntConst(lvalue + rvalue); break;
                case SUB: return new IntConst(lvalue - rvalue); break;
                case MUL: return new IntConst(lvalue * rvalue); break;
                case DIV: return new IntConst(lvalue / rvalue); break;
                case MOD: return new IntConst(lvalue % rvalue); break;
                case LSHIFT:  return new IntConst(lvalue << rvalue); break;
                case RSHIFT:  return new IntConst(lvalue >> rvalue); break;
                case BIT_AND: return new IntConst(lvalue & rvalue);  break;
                case BIT_XOR: return new IntConst(lvalue ^ rvalue);  break;
                case BIT_OR:  return new IntConst(lvalue | rvalue);  break;
                case GT: return new IntConst(lvalue >  rvalue ? 1 : 0); break;
                case LT: return new IntConst(lvalue <  rvalue ? 1 : 0); break;
                case GE: return new IntConst(lvalue >= rvalue ? 1 : 0); break;
                case LE: return new IntConst(lvalue <= rvalue ? 1 : 0); break;
                case EQ: return new IntConst(lvalue == rvalue ? 1 : 0); break;
                case NE: return new IntConst(lvalue != rvalue ? 1 : 0); break;
                default:
                    throw new InternalError(node.location(),
                            "unsupported operator for integer : " + node.operator());
            }
        }
        if (lhs instanceof StrConst && rhs instanceof StrConst) {
            StringConstantEntity lvalue = ((StrConst)lhs).entity(), rvalue = ((StrConst)rhs).entity();
            switch (node.operator()) {
                case ADD: {
                        String join = lvalue.strValue() + rvalue.strValue();
                        StringConstantEntity entity = (StringConstantEntity) ast.scope().lookup(
                                StringType.STRING_CONSTANT_PREFIX + join);
                        if (entity == null) {
                            entity = new StringConstantEntity(node.location(), Type.stringType, join, null);
                            ast.scope().insert(entity);
                        }
                        return new StrConst(entity);
                    }
                    break;
                case EQ:
                    return new IntConst(lvalue.strValue().equals(rvalue.strValue()) ? 1 : 0); break;
                case LT:
                    return new IntConst(lvalue.strValue().compareTo(rvalue.strValue())); break;
                default:
                    throw new InternalError(node.location(),
                            "unsupported operator for string : " + node.operator());
            }
        }

        // convert string operator to function call
        if (node.left().type().isString()) {
            ExprNode trans;
            switch (node.operator()) {   // can be optimized here
                case ADD:
                    return new Call(StringType.operatorADD, new LinkedList<Expr>(){{ add(lhs); add(rhs); }});
                    break;
                case EQ:
                    return new Call(StringType.operatorEQ, new LinkedList<Expr>(){{ add(lhs); add(rhs); }});
                    break;
                case LT:
                    return new Call(StringType.operatorLT, new LinkedList<Expr>(){{ add(lhs); add(rhs); }});
                    break;
            }

            return visitExpr(null);
        } else {  // convert the type of operator
            Binary.BinaryOp op;
            switch (node.operator()) {   // can be optimized here
                case ADD: op = Binary.BinaryOp.ADD; break;
                case SUB: op = Binary.BinaryOp.SUB; break;
                case MUL: op = Binary.BinaryOp.MUL; break;
                case DIV: op = Binary.BinaryOp.DIV; break;
                case MOD: op = Binary.BinaryOp.MOD; break;
                case LSHIFT:  op = Binary.BinaryOp.LSHIFT;  break;
                case RSHIFT:  op = Binary.BinaryOp.RSHIFT;  break;
                case BIT_AND: op = Binary.BinaryOp.BIT_AND; break;
                case BIT_XOR: op = Binary.BinaryOp.BIT_XOR; break;
                case BIT_OR:  op = Binary.BinaryOp.BIT_OR;  break;
                case GT: op = Binary.BinaryOp.GT; break;
                case LT: op = Binary.BinaryOp.LT; break;
                case GE: op = Binary.BinaryOp.GE; break;
                case LE: op = Binary.BinaryOp.LE; break;
                case EQ: op = Binary.BinaryOp.EQ; break;
                case NE: op = Binary.BinaryOp.NE; break;
                default:
                    throw new InternalError(node.location(),
                            "unsupported operator for int : " + node.operator());
            }
            return new Binary(lhs, op, rhs);
        }
    }

    @Override
    public Expr visit(LogicalAndNode node) {
        return null;
    }

    @Override
    public Expr visit(LogicalOrNode node) {
        return null;
    }

    @Override
    public Expr visit(FuncallNode node) {
        visitExpr(node.expr());
        // !!!!!!!!!!!!!!!!!!!!!!! HERE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    @Override
    public Expr visit(IntegerLiteralNode node) {
        return new IntConst((int) node.value());
    }

    @Override
    public Expr visit(StringLiteralNode node) {
        return new StrConst(node.entity());
    }

    @Override
    public Expr visit(BoolLiteralNode node) {
        return new IntConst(node.value() ? 1 : 0);
    }

    @Override
    public Expr visit(ArefNode node) {
        return null;
    }

    @Override
    public Expr visit(VariableNode node) {
        return null;
    }

    @Override
    public Expr visit(MemberNode node) {
        return null;
    }

    @Override
    public Expr visit(CreatorNode node) {
        return null;
    }

    @Override
    public Expr visit(UnaryOpNode node) {
        return null;
    }

    @Override
    public Expr visit(PrefixOpNode node) {
        return null;
    }

    @Override
    public Expr visit(SuffixOpNode node) {
        return null;
    }




    private List<IR> fetchStmts() {
        List<IR> ret = stmts;
        stmts = new LinkedList<>();
        return ret;
    }

    private void addAssign(Expr lhs, Expr rhs) {
        stmts.add(new Assign(lhs, rhs));
    }
}*/