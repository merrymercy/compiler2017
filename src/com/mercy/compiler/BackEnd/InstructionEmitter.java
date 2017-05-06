package com.mercy.compiler.BackEnd;

import com.mercy.compiler.Entity.*;
import com.mercy.compiler.INS.*;
import com.mercy.compiler.INS.CJump;
import com.mercy.compiler.INS.Call;
import com.mercy.compiler.INS.Label;
import com.mercy.compiler.INS.Operand.*;
import com.mercy.compiler.INS.Return;
import com.mercy.compiler.IR.*;
import com.mercy.compiler.Utility.InternalError;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mercy on 17-4-25.
 */
public class InstructionEmitter {
    private List<FunctionEntity> functionEntities;
    private Scope globalScope;
    private List<IR> globalInitializer;

    private List<Instruction> ins;
    FunctionEntity currentFunction;

    Register rax = new Register("rax");

    public InstructionEmitter(IRBuilder irBuilder) {
        this.globalScope = irBuilder.globalScope();
        this.functionEntities = irBuilder.functionEntities();
        this.globalInitializer = irBuilder.globalInitializer();
    }

    public void emit() {
        // emit functions
        for (FunctionEntity functionEntity : functionEntities) {
            currentFunction = functionEntity;
            tmpStack = new ArrayList<>();
            top = 0;
            functionEntity.setINS(emitFunction(functionEntity));
            functionEntity.setTmpStack(tmpStack);
        }
    }

    public List<Instruction> emitFunction(FunctionEntity entity) {
        ins = new LinkedList<>();
        for (IR ir : entity.IR()) {
            ir.accept(this);
        }
        return ins;
    }

    /*
     * IR Visitor
     */
    int exprDepth = 0;
    public Operand visitExpr(com.mercy.compiler.IR.Expr ir) {
        if (exprDepth == 0)
            top = 0;
        exprDepth++;
        Operand ret = ir.accept(this);
        exprDepth--;
        return ret;
    }

    public Operand visit(com.mercy.compiler.IR.Addr ir) {
        return new Address(ir.entity());
    }

    public Operand visit(com.mercy.compiler.IR.Assign ir) {
        Operand lhs = visitExpr(ir.left());
        exprDepth++;
        Operand rhs = visitExpr(ir.right());
        exprDepth--;
        if (ir.left() instanceof Addr)
            ins.add(new Move(lhs, rhs));
        else {
            ins.add(new Move(new Address(lhs), rhs));
        }
        return null;
    }

    private void addBinary(com.mercy.compiler.IR.Binary.BinaryOp operator, Operand left, Operand right) {
        switch (operator) {
            case ADD: ins.add(new Add(left, right)); break;
            case SUB: ins.add(new Sub(left, right)); break;
            case MUL: ins.add(new Mul(left, right)); break;
            case DIV: ins.add(new Div(left, right)); break;
            case MOD: ins.add(new Mod(left, right)); break;
            case LOGIC_AND: case BIT_AND:
                ins.add(new And(left, right)); break;
            case LOGIC_OR: case BIT_OR:
                ins.add(new Or(left, right));  break;
            case BIT_XOR:
                ins.add(new Xor(left, right)); break;
            case LSHIFT:
                ins.add(new Sal(left, right)); break;
            case RSHIFT:
                ins.add(new Sar(left, right)); break;
            case EQ:
                ins.add(new Cmp(left, right, Cmp.Operator.EQ)); break;
            case NE:
                ins.add(new Cmp(left, right, Cmp.Operator.NE)); break;
            case GE:
                ins.add(new Cmp(left, right, Cmp.Operator.GE)); break;
            case GT:
                ins.add(new Cmp(left, right, Cmp.Operator.GT)); break;
            case LE:
                ins.add(new Cmp(left, right, Cmp.Operator.LE)); break;
            case LT:
                ins.add(new Cmp(left, right, Cmp.Operator.LT)); break;
            default:
                throw new InternalError("Invalid operator " + operator);
        }
    }

    List<Reference> tmpStack;
    int top = 0;
    public Reference getTmp() {
        if (top >= tmpStack.size())
            tmpStack.add(new Reference());
        return tmpStack.get(top++);
    }

    public Operand visit(com.mercy.compiler.IR.Binary ir) {
        Operand left = visitExpr(ir.left());
        Operand right = visitExpr(ir.right());
        top--;
        addBinary(ir.operator(), left, right);
        return left;
    }

    public Operand visit(com.mercy.compiler.IR.Call ir) {
        List<Operand> operands = new LinkedList<>();

        int backupTop = top;

        for (Expr arg : ir.args()) {
            exprDepth++;
            operands.add(visitExpr(arg));
            exprDepth--;
        }

        Reference ret = null;
        Call call = new Call(ir.entity(), operands);
        if (exprDepth != 0) {
            ret = getTmp();
            call.setRet(ret);
        }
        ins.add(call);

        top = backupTop;

        return ret;
    }

    public Operand visit(com.mercy.compiler.IR.CJump ir) {
        Operand tmp = visitExpr(ir.cond());
        ins.add(new CJump(tmp, new Label(ir.trueLabel().name()),
                new Label(ir.falseLabel().name())));
        return null;
    }

    public Operand visit(com.mercy.compiler.IR.Jump ir) {
        ins.add(new Jmp(new Label(ir.label().name())));
        return null;
    }

    public Operand visit(com.mercy.compiler.IR.Label ir) {
        ins.add(new Label(ir.name()));
        return null;
    }

    public Operand visit(com.mercy.compiler.IR.Return ir) {
        if (ir.expr() == null) {
            ins.add(new Return(null));
        } else {
            Operand ret = visitExpr(ir.expr());
            ins.add(new Return(ret));
        }
        return null;
    }

    public Operand visit(com.mercy.compiler.IR.Unary ir) {
        Operand ret = visitExpr(ir.expr());
        switch (ir.operator()) {
            case MINUS:
                ins.add(new Neg(ret)); break;
            case BIT_NOT:
                ins.add(new Not(ret)); break;
            case LOGIC_NOT:
                ins.add(new Cmp(ret, new Immediate(0), Cmp.Operator.EQ));
                break;
            default:
                throw new InternalError("invalid operator " + ir.operator());
        }
        return ret;
    }

    public Operand visit(com.mercy.compiler.IR.Mem ir) {
        Operand addr = visitExpr(ir.expr());
        Reference ret = getTmp();
        if (ir.expr() instanceof Addr)
            ins.add(new Move(ret, addr));
        else {
            ins.add(new Move(ret, new Address(addr)));
        }
        return ret;
    }

    public Operand visit(com.mercy.compiler.IR.StrConst ir) {
        Reference ret = getTmp();
        ins.add(new Move(ret, new Address(ir.entity())));
        return ret;
    }

    public Operand visit(com.mercy.compiler.IR.IntConst ir) {
        Reference ret = getTmp();
        ins.add(new Move(ret, new Immediate(ir.value())));
        return ret;
    }

    public Operand visit(com.mercy.compiler.IR.Var ir) {
        Reference ret = getTmp();
        ins.add(new Move(ret, new Address(ir.entity())));
        return ret;
    }

    // getter
    public List<FunctionEntity> functionEntities() {
        return functionEntities;
    }

    public Scope globalScope() {
        return globalScope;
    }

    public List<IR> globalInitializer() {
        return globalInitializer;
    }

    /***** DEBUG TOOL *****/
    private void printFunction(PrintStream out, FunctionEntity entity) {
        out.println("========== INS ==========");
        for (Instruction instruction : entity.ins()) {
            out.println(instruction.toString());
        }
    }

    public void printSelf(PrintStream out) {
        for (FunctionEntity functionEntity : functionEntities) {
            printFunction(out, functionEntity);
        }
    }
}

/*

    private List<Reference> virtualStack;
    private int top = -1;
    private void virtualPush(Operand operand) {
        top++;
        if (top >= virtualStack.size()) {
            virtualStack.add(new Reference());
        }
        ins.add(new Move(virtualStack.get(top), operand));
    }

    private Operand virtualPop() {
        return virtualStack.get(top--);
    }

 */