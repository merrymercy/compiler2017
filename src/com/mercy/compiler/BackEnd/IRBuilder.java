package com.mercy.compiler.BackEnd;

import com.mercy.compiler.AST.*;
import com.mercy.compiler.Entity.*;
import com.mercy.compiler.FrontEnd.ASTVisitor;
import com.mercy.compiler.IR.*;
import com.mercy.compiler.Type.*;
import com.mercy.compiler.Utility.InternalError;

import static com.mercy.compiler.IR.Binary.BinaryOp.*;
import static com.mercy.compiler.IR.Unary.UnaryOp.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Created by mercy on 17-3-30.
 */

public class IRBuilder implements ASTVisitor<Void, Expr> {
    public final int ALIGNMENT = 4;
    private List<IR> stmts = new LinkedList<>();

    private AST ast;
    private int exprDepth = 0;

    private Stack<Scope> scopeStack = new Stack<>();
    private Scope currentScope;

    private List<IR> globalInitializer;

    // some constant
    private final IntConst constPointerSize = new IntConst(4);
    private final IntConst constLengthSize = new IntConst(4);
    private final IntConst constOne = new IntConst(1);
    private final IntConst constZero = new IntConst(0);
    private final FunctionEntity malloc;

    public IRBuilder(AST abstractSemanticTree) {
        this.ast = abstractSemanticTree;
        malloc = (FunctionEntity) ast.scope().find("__malloc");
    }

    public void generateIR() {
        // calc offset in class
        for (ClassEntity entity : ast.classEntitsies()) {
            entity.initOffset(ALIGNMENT);
        }

        // gather global variable initialization
        for (DefinitionNode node : ast.definitionNodes()) {
            if (node instanceof VariableDefNode) { // TODO: constructor !!!!
                visit((VariableDefNode)node);
            }
        }
        globalInitializer = fetchStmts();

        // generate global functions
        for (FunctionEntity entity : ast.functionEntities()) {
            compileFunction(entity);
            if (entity.name().equals("main")) {
                ; // insert global initializer ?
            }
        }

        // generate in-class functions
        for (ClassEntity entity : ast.classEntitsies()) {
            for (FunctionDefNode node : entity.memberFuncs()) {
                compileFunction(node.entity());
            }
        }

        // generate built-in functions ?
    }

    public void compileFunction(FunctionEntity entity) {
        // body
        scopeStack.push(null);
        visit(entity.body());
        entity.setIR(fetchStmts());
        assert (scopeStack.size() == 1);
    }

    @Override
    public Void visit(FunctionDefNode node) {
        throw new InternalError("invalid call to visit(FunctionDefNode node) in IRBuilder.");
    }
    @Override
    public Void visit(ClassDefNode node) {
        throw new InternalError("invalid call to visit(ClassDefNode node) in IRBuilder.");
    }

    @Override
    public Void visit(VariableDefNode node) {
        ExprNode init = node.entity().initializer();
        if (init != null)
            addAssign(new Var(node.entity()), visitExpr(init));
        return null;
    }

    public Void visit(BlockNode node) {
        currentScope = node.scope();
        scopeStack.push(currentScope);
        for (StmtNode stmt : node.stmts()) {
            stmt.accept(this);
        }
        scopeStack.pop();
        currentScope = scopeStack.peek();
        return null;
    }

    public Expr visitExpr(ExprNode node) {
        exprDepth++;
        Expr expr = node.accept(this);
        exprDepth--;
        return expr;
    }

    public void visitStmt(StmtNode node) {
        node.accept(this);
    }

    @Override
    public Void visit(IfNode node) {
        Label thenLable = new Label();
        Label elseLable = new Label();
        Label endLable  = new Label();

        if (node.elseBody() == null) {  // can be optimized here, see Lequn Chen's slide
            stmts.add(new CJump(visitExpr(node.cond()), thenLable, endLable));
            addLabel(thenLable, "if_then");
            visitStmt(node.thenBody());
            addLabel(endLable, "if_end");
        } else {
            stmts.add(new CJump(visitExpr(node.cond()), thenLable, elseLable));
            addLabel(thenLable, "if_then");
            visitStmt(node.thenBody());
            stmts.add(new Jump(endLable));
            addLabel(elseLable, "if_else");
            visitStmt(node.elseBody());
            addLabel(endLable, "if_end");
        }
        return null;
    }

    Stack<Label> testLabelStack = new Stack<>();
    Stack<Label> endLabelStack  = new Stack<>();

    private void visitLoop(ExprNode init, ExprNode cond, ExprNode incr, StmtNode body) {
        if (init != null) {
            visitExpr(init);
        }
        Label testLabel = new Label();
        Label beginLabel = new Label();
        Label endLabel = new Label();

        stmts.add(new Jump(testLabel));
        addLabel(beginLabel, "loop_begin");

        testLabelStack.push(testLabel);
        endLabelStack.push(endLabel);
        visitStmt(body);
        if (incr != null) {
            visitExpr(incr);
        }
        endLabelStack.pop();
        testLabelStack.pop();

        addLabel(testLabel, "loop_test");
        stmts.add(new CJump(visitExpr(cond), beginLabel, endLabel));
        addLabel(endLabel, "loop_end");
    }

    @Override
    public Void visit(WhileNode node) {
        visitLoop(null, node.cond(), null, node.body());
        return null;
    }

    @Override
    public Void visit(ForNode node) {
        visitLoop(node.init(), node.cond(), node.incr(), node.body());
        return null;
    }

    @Override
    public Void visit(ContinueNode node) {
        stmts.add(new Jump(testLabelStack.peek()));
        return null;
    }

    @Override
    public Void visit(BreakNode node) {
        stmts.add(new Jump(endLabelStack.peek()));
        return null;
    }

    @Override
    public Void visit(ReturnNode node) {
        stmts.add(new Return(
                node.expr() == null ? null : visitExpr(node.expr())));
        return null;
    }

    @Override
    public Void visit(ExprStmtNode node) {
        node.expr().accept(this);
        return null;
    }

    @Override
    public Expr visit(AssignNode node) {
        Expr lhs = visitExpr(node.lhs());
        Expr rhs = visitExpr(node.rhs());
        if (exprDepth == 0) { // is statement, which is always true in Mx* 2017
            addAssign(lhs, rhs);
        } else {
            throw new InternalError(node.location(), "undefined behavior in nested assign expression (a = b = c)");
        }

        return null;
    }

    @Override
    public Expr visit(BinaryOpNode node) {
        Expr lhs = visitExpr(node.left()), rhs = visitExpr(node.right());

        if (exprDepth == 0)
            return null;

        // simple constant folding for integer
        if (lhs instanceof IntConst && rhs instanceof IntConst) {
            int lvalue = ((IntConst)lhs).value(), rvalue = ((IntConst)rhs).value();
            switch (node.operator()) {
                case ADD: return new IntConst(lvalue + rvalue);
                case SUB: return new IntConst(lvalue - rvalue);
                case MUL: return new IntConst(lvalue * rvalue);
                case DIV: return new IntConst(lvalue / rvalue);
                case MOD: return new IntConst(lvalue % rvalue);
                case LSHIFT:  return new IntConst(lvalue << rvalue);
                case RSHIFT:  return new IntConst(lvalue >> rvalue);
                case BIT_AND: return new IntConst(lvalue & rvalue);
                case BIT_XOR: return new IntConst(lvalue ^ rvalue);
                case BIT_OR:  return new IntConst(lvalue | rvalue);
                case GT: return new IntConst(lvalue >  rvalue ? 1 : 0);
                case LT: return new IntConst(lvalue <  rvalue ? 1 : 0);
                case GE: return new IntConst(lvalue >= rvalue ? 1 : 0);
                case LE: return new IntConst(lvalue <= rvalue ? 1 : 0);
                case EQ: return new IntConst(lvalue == rvalue ? 1 : 0);
                case NE: return new IntConst(lvalue != rvalue ? 1 : 0);
                default:
                    throw new InternalError(node.location(),
                            "unsupported operator for integer : " + node.operator());
            }
        }
        // simple constant folding for string
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
                case EQ:
                    return new IntConst(lvalue.strValue().compareTo(rvalue.strValue()) == 0 ? 1 : 0);
                case NE:
                    return new IntConst(lvalue.strValue().compareTo(rvalue.strValue()) != 0 ? 1 : 0);
                case GT:
                    return new IntConst(lvalue.strValue().compareTo(rvalue.strValue()) >  0 ? 1 : 0);
                case LT:
                    return new IntConst(lvalue.strValue().compareTo(rvalue.strValue()) <  0 ? 1 : 0);
                case GE:
                    return new IntConst(lvalue.strValue().compareTo(rvalue.strValue()) >= 0 ? 1 : 0);
                case LE:
                    return new IntConst(lvalue.strValue().compareTo(rvalue.strValue()) <= 0 ? 1 : 0);
                default:
                    throw new InternalError(node.location(),
                            "unsupported operator for string : " + node.operator());
            }
        }

        // convert string operator to function call
        if (node.left().type().isString()) {
            ExprNode trans;
            switch (node.operator()) {   // can be optimized here (use inline call)
                case ADD:
                    return new Call(StringType.operatorADD, new LinkedList<Expr>(){{ add(lhs); add(rhs); }});
                case EQ:
                    return new Call(StringType.operatorEQ, new LinkedList<Expr>(){{ add(lhs); add(rhs); }});
                case NE:
                    return new Call(StringType.operatorNE, new LinkedList<Expr>(){{ add(lhs); add(rhs); }});
                case GT:
                    return new Call(StringType.operatorGT, new LinkedList<Expr>(){{ add(lhs); add(rhs); }});
                case LT:
                    return new Call(StringType.operatorLT, new LinkedList<Expr>(){{ add(lhs); add(rhs); }});
                case LE:
                    return new Call(StringType.operatorLE, new LinkedList<Expr>(){{ add(lhs); add(rhs); }});
                case GE:
                    return new Call(StringType.operatorGE, new LinkedList<Expr>(){{ add(lhs); add(rhs); }});
                default:
                    throw new InternalError(node.location(), "invalid operator " + node.operator());
            }
        } else {  // convert the type of operator (AST.BinaryOp -> IR.BinaryOP), can be optimized here
            Binary.BinaryOp op;
            switch (node.operator()) {
                case ADD: op = ADD; break;
                case SUB: op = SUB; break;
                case MUL: op = MUL; break;
                case DIV: op = DIV; break;
                case MOD: op = MOD; break;
                case LSHIFT:  op = LSHIFT;  break;
                case RSHIFT:  op = RSHIFT;  break;
                case BIT_AND: op = BIT_AND; break;
                case BIT_XOR: op = BIT_XOR; break;
                case BIT_OR:  op = BIT_OR;  break;
                case LOGIC_AND: op = LOGIC_AND; break;
                case LOGIC_OR:  op = LOGIC_OR;  break;
                case GT: op = GT; break;
                case LT: op = LT; break;
                case GE: op = GE; break;
                case LE: op = LE; break;
                case EQ: op = EQ; break;
                case NE: op = NE; break;
                default:
                    throw new InternalError(node.location(),
                            "unsupported operator for int : " + node.operator());
            }
            return new Binary(lhs, op, rhs);
        }
    }

    @Override
    public Expr visit(LogicalAndNode node) {
        Label goon = new Label();
        Label end = new Label();

        Var tmp = newIntTemp();
        addAssign(tmp, visitExpr(node.left()));
        stmts.add(new CJump(tmp, goon, end));
        addLabel(goon, "goon");
        addAssign(tmp, visitExpr(node.right()));
        addLabel(end, "end");
        return exprDepth == 0 ? null : tmp;
    }

    @Override
    public Expr visit(LogicalOrNode node) {
        Label goon = new Label();
        Label end = new Label();

        Var tmp = newIntTemp();
        addAssign(tmp, visitExpr(node.left()));
        stmts.add(new CJump(tmp, end, goon));
        addLabel(goon, "goon");
        addAssign(tmp, visitExpr(node.right()));
        addLabel(end, "end");
        return exprDepth == 0 ? null : tmp;
    }

    @Override
    public Expr visit(FuncallNode node) {
        FunctionEntity entity = node.functionType().entity();

        List<Expr> args = new LinkedList<>();
        for (ExprNode exprNode : node.args())
            args.add(visitExpr(exprNode));

        if (exprDepth == 0) {
            stmts.add(new Call(entity, args));
            return null;
        } else {
            Var tmp = newIntTemp();
            addAssign(tmp, new Call(entity, args));
            return tmp;
        }
    }

    @Override
    public Expr visit(IntegerLiteralNode node) {
        return new IntConst((int) node.value());
    }

    @Override
    public Expr visit(StringLiteralNode node) {
        if (ast.scope().lookup(node.entity().name()) == null) {
            ast.scope().insert(node.entity());
        }
        return new StrConst(node.entity());
    }

    @Override
    public Expr visit(BoolLiteralNode node) {
        return new IntConst(node.value() ? 1 : 0);
    }

    @Override
    public Expr visit(ArefNode node) {
        Expr base = visitExpr(node.expr());
        Expr index = visitExpr(node.index());
        int sizeof = ((ArrayType)(node.expr().type())).baseType().size();

        return new Mem(new Binary(base, ADD, new Binary(
                                    new IntConst(sizeof), MUL, index)));
    }

    @Override
    public Expr visit(VariableNode node) {
        if (node.isMember()) {  // add "this" pointer
            Expr base = new Var(node.getThisPointer());
            int offset = node.entity().offset();
            return new Mem(new Binary(base, ADD, new IntConst(offset)));
        } else {
            return new Var(node.entity());
        }
    }

    @Override
    public Expr visit(MemberNode node) {
        Expr base = visitExpr(node.expr());
        int offset = node.entity().offset();
        return new Mem(new Binary(base, ADD, new IntConst(offset)));
    }

    private void expandCreator(List<ExprNode> exprs, Expr base, int now, Type type, FunctionEntity constructor) {
        Var tmpS = newIntTemp();
        Var tmpI = newIntTemp();
        // new int a[5][4][];
        /* s = expr[now];
         * p = malloc(s * pointerSize + lengthSize);
         * *p = s;
         * p = p + 4;
         * for (int i = 0; i < s; i++) {  /-----
         *     s2 = expr[now + 1]
         *     p[i] = malloc(s2 * pointerSize + lengthSize);
         *     *(p[i]) = s2;
         *     p[i] = p[i] + 4;
         *     for (int i2 = 0; i2 < s2; i++) { /-----
         *         s3 = expr[now + 2];
         *         p[i][i2] = malloc(s3 * elementSize + lengthSize);
         *         p[i][i2][-1] = s3;
         *         // None or Constructor for class
         *         for (int i3 = 0; i3 < s3; i3++)  /-----
         *             constructor(p[i][i2][i3])
         *     }
         * }
         */
        IntConst sizeof = new IntConst(type.size());

        addAssign(tmpS, visitExpr(exprs.get(now)));
        addAssign(base, new Call(malloc, new LinkedList<Expr>(){{add(new Binary(
                new Binary(tmpS, MUL, sizeof), ADD, constLengthSize));}}));
        addAssign(new Mem(base), tmpS);
        addAssign(base, new Binary(base, ADD, constLengthSize));

        if (exprs.size() > now + 1) {
            addAssign(tmpI, constZero);
            Label testLabel = new Label();
            Label beginLabel = new Label();
            Label endLabel = new Label();

            stmts.add(new Jump(testLabel));
            addLabel(beginLabel, "creator_loop_begin");
            expandCreator(exprs, new Mem(new Binary(base, ADD, new Binary(tmpI, MUL, sizeof))), now + 1, ((ArrayType)type).baseType(), constructor);
            addAssign(tmpI, new Binary(tmpI, ADD, constOne));

            addLabel(testLabel, "creator_loop_test");
            stmts.add(new CJump(new Binary(tmpI, LT, tmpS), beginLabel, endLabel));
            addLabel(endLabel, "creator_loop_end");
        } else if (exprs.size() == now + 1 && constructor != null) {
            addAssign(tmpI, constZero);
            Label testLabel = new Label();
            Label beginLabel = new Label();
            Label endLabel = new Label();

            stmts.add(new Jump(testLabel));
            addLabel(beginLabel, "creator_loop_begin");
            stmts.add(new Call(constructor, new LinkedList<Expr>() {{add( new Mem(new Binary(base, ADD, new Binary(tmpI, MUL, sizeof))));}}));
            addAssign(tmpI, new Binary(tmpI, ADD, constOne));

            addLabel(testLabel, "creator_loop_test");
            stmts.add(new CJump(new Binary(tmpI, LT, tmpS), beginLabel, endLabel));
            addLabel(endLabel, "creator_loop_end");
        }
    }

    @Override
    public Expr visit(CreatorNode node) {
        if (node.type() instanceof ArrayType) {
            Type baseType = ((ArrayType) node.type()).baseType();
            Type deepType = ((ArrayType) node.type()).deepType();
            Var pointer = newIntTemp();
            FunctionEntity constructor = null;
            if (node.exprs().size() == node.total() && deepType instanceof ClassType)
                constructor = ((ClassType)deepType).entity().constructor();
            expandCreator(node.exprs(), pointer, 0, baseType, constructor);
            return exprDepth == 0 ? null : pointer;
        } else {
            ClassEntity entity = ((ClassType) node.type()).entity();
            Var tmp = newIntTemp();
            addAssign(tmp, new Call(malloc, new LinkedList<Expr>(){{ add(new IntConst(entity.size())); }}));
            if (entity.constructor() != null)
                stmts.add(new Call(entity.constructor(), new LinkedList<Expr>(){{ add(tmp); }}));
            return exprDepth == 0 ? null : tmp;
        }
    }

    @Override
    public Expr visit(UnaryOpNode node) {
        switch (node.operator()) {
            case ADD:
                if (node.expr() instanceof IntegerLiteralNode)
                    return new IntConst((int)((IntegerLiteralNode)node.expr()).value());
                else
                    return visitExpr(node.expr());
            case MINUS:
                if (node.expr() instanceof IntegerLiteralNode)
                    return new IntConst(-(int)((IntegerLiteralNode)node.expr()).value());
                else
                    return new Unary(MINUS, visitExpr(node.expr()));
            case BIT_NOT:
                if (node.expr() instanceof IntegerLiteralNode)
                    return new IntConst(~(int)((IntegerLiteralNode)node.expr()).value());
                else
                    return new Unary(BIT_NOT, visitExpr(node.expr()));
            case LOGIC_NOT:
                if (node.expr() instanceof BoolLiteralNode)
                    return new IntConst(((BoolLiteralNode)node.expr()).value() ? 1 : 0);
                else
                    return new Unary(LOGIC_NOT, visitExpr(node.expr()));
            case PRE_INC: case PRE_DEC: {
                Binary.BinaryOp op = node.operator() == UnaryOpNode.UnaryOp.PRE_INC ? ADD : SUB;
                if (true || node.expr() instanceof VariableNode) { // cont(++i); -> i = i + 1; cont(i);
                    Expr expr = visitExpr(node.expr());
                    addAssign(expr, new Binary(expr, op, constOne));
                    return exprDepth == 0 ? null : expr;
                }
                //} else {                                   // cont(++i) -> p = &i; *p = *p + 1; cont(*p);
                //    Expr p = getAddress(visitExpr(node.expr()));
                //    addAssign(new Mem(p), new Binary(new Mem(p), op, constOne));
                //    return exprDepth == 0 ? null : new Mem(p);
                //}
            }
            case SUF_INC: case SUF_DEC: {
                Binary.BinaryOp op = node.operator() == UnaryOpNode.UnaryOp.SUF_INC ? ADD : SUB;
                Expr expr = visitExpr(node.expr());
                if (true || node.expr() instanceof VariableNode) { // cont(i++); -> v = i; i = i + 1; cont(v)
                    if (exprDepth != 0) {
                        Var tmp = newIntTemp();
                        addAssign(expr, new Binary(expr, op, constOne));
                        return tmp;
                    } else {
                        addAssign(expr, new Binary(expr, op, constOne));
                    }
                }
                //} else {                                   // cont(i++) -> p = &i; t = *p; *p = t + 1; cont(t);
                //    Expr p = getAddress(expr);
                //    if (exprDepth != 0) {
                //        Var tmp = newIntTemp();
                //        addAssign(tmp, new Mem(p));
                //        addAssign(expr, new Binary(tmp, op, constOne));
                //        return tmp;
                //    } else {
                //        addAssign(expr, new Binary(new Mem(p), op, constOne));
                //        return null;
                //    }
                //}
            }
            default:
                throw new InternalError(node.location(), "invalid operator " + node.operator());
        }
    }

    @Override
    public Expr visit(PrefixOpNode node) {
        return visit((UnaryOpNode)node);
    }

    @Override
    public Expr visit(SuffixOpNode node) {
        return visit((UnaryOpNode)node);
    }

    /*
     * private utility
     */
    private List<IR> fetchStmts() {
        List<IR> ret = stmts;
        stmts = new LinkedList<>();
        return ret;
    }

    private Expr getAddress(Expr expr) {
        if (expr instanceof Var) {
            return new Addr(((Var) expr).entity());
        } else if (expr instanceof Mem) {
            return ((Mem) expr).expr();
        } else {
            throw new InternalError("get address on an invalid type " + expr);
        }
    }

    private void addAssign(Expr lhs, Expr rhs) {
        stmts.add(new Assign(getAddress(lhs), rhs));
    }

    static int labelCounter = 0;
    private void addLabel(Label label, String name) {
        label.setName(name + "_" + labelCounter);
        stmts.add(label);
        labelCounter++;
    }

    static int tmpCounter = 0;
    private Var newIntTemp() {
        VariableEntity tmp = new VariableEntity(null, new IntegerType(),
                "tmp" + tmpCounter, null);
        currentScope.insert(tmp);
        tmpCounter += 1;
        return new Var(tmp);
    }

    private VariableEntity newTemp(Type type) {
        return new VariableEntity(null, type, "tmp" + tmpCounter, null);
    }


    /*
     * getter
     */
    public List<IR> globalInitializer() {
        return globalInitializer;
    }

    public Scope globalScope() {
        return ast.scope();
    }

    public List<FunctionEntity> functionEntities() {
        return ast.functionEntities();
    }
}

//public class IRBuilder {
//    public IRBuilder(AST abstractSemanticTree) {}
//    public void generateIR() {
//
//    }
//}



//    private BlockNode expandCreator(List<ExprNode> exprs, ExprNode base, int now, FunctionEntity constructor) {
//        VariableNode tmpS = new VariableNode(newIntTemp().entity());
//        VariableNode tmpI = new VariableNode(newIntTemp().entity());
//        // new int a[5][4][];
//        /* s = expr[now];
//         * p = malloc(s * pointerSize + lengthSize);
//         *  = s;
//         * for (int i = 0; i < s; i++) {  /-----
//         *     s2 = expr[now + 1]
//         *     p[i] = malloc(s2 * pointerSize + lengthSize);
//         *     p[-1] = s2
//         *     for (int i2 = 0; i2 < s2; i++) { /-----
//         *         s3 = expr[now + 2];
//         *         p[i][i2] = malloc(s3 * elementSize + lengthSize);
//         *         p[i][i2][-1] = s3;
//         *         // None or Constructor for class
//         *         for (int i3 = 0; i3 < s3; i3++)  /-----
//         *             constructor(p[i][i2][i3])
//         *     }
//         * }
//         * p
//         */
//Type baseType = ((ArrayType)base.type()).baseType();
//    ExprStmtNode calcSize = new ExprStmtNode(null,
//            new AssignNode(tmpS, exprs.get(now)));
//    ExprStmtNode alloc = new ExprStmtNode(null,
//            new AssignNode(base, new FuncallNode(new VariableNode(malloc),
//                    new LinkedList<ExprNode>() {{
//                        add(new BinaryOpNode(tmpS, BinaryOpNode.BinaryOp.Mul, constPointerSizeNode));
//                    }})));
//    ExprStmtNode storeLen = new ExprStmtNode(null,
//            new AssignNode(new ArefNode(base, constMinusOneNode, baseType), tmpS));
//
//    ForNode forNode = null;
//
//        if (exprs.size() > now + 1) {
//                forNode = new ForNode(null, new AssignNode(tmpI, constZeroNode),
//                new BinaryOpNode(tmpI, BinaryOpNode.BinaryOp.LT, tmpS), new PrefixOpNode(UnaryOpNode.UnaryOp.PRE_INC, tmpI),
//                expandCreator(exprs, new ArefNode(base, tmpI, baseType), now + 1, constructor));
//                } else if (constructor != null) {
//                forNode = new ForNode(null, new AssignNode(tmpI, constZeroNode),
//                new BinaryOpNode(tmpI, BinaryOpNode.BinaryOp.LT, tmpS), new PrefixOpNode(UnaryOpNode.UnaryOp.PRE_INC, tmpI),
//                new ExprStmtNode(null, new FuncallNode(new VariableNode(constructor),
//                new LinkedList<ExprNode>(){{add(new ArefNode(base, tmpI, baseType));}})));
//        }
//
//        LinkedList<StmtNode> rets = new LinkedList<>();
//        rets.add(calcSize);
//        rets.add(alloc);
//        rets.add(storeLen);
//        if (forNode != null)
//        rets.add(forNode);
//
//        return new BlockNode(null, rets);
//        }
