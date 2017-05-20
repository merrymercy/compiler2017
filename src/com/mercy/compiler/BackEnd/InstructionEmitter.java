package com.mercy.compiler.BackEnd;

import com.mercy.compiler.AST.FunctionDefNode;
import com.mercy.compiler.Entity.*;
import com.mercy.compiler.INS.*;
import com.mercy.compiler.INS.CJump;
import com.mercy.compiler.INS.Call;
import com.mercy.compiler.INS.Label;
import com.mercy.compiler.INS.Operand.*;
import com.mercy.compiler.INS.Return;
import com.mercy.compiler.IR.*;
import com.mercy.compiler.Utility.InternalError;
import com.mercy.compiler.Utility.Triple;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.mercy.compiler.INS.Operand.Address.Type.BASE_ONLY;
import static com.mercy.compiler.IR.Binary.BinaryOp.*;

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
        this.functionEntities = new LinkedList<>(irBuilder.functionEntities());
        for (ClassEntity entity : irBuilder.ast().classEntitsies()) {
            for (FunctionDefNode functionDefNode : entity.memberFuncs()) {
                this.functionEntities.add(functionDefNode.entity());
            }
        }
        this.globalInitializer = irBuilder.globalInitializer();
    }

    public void emit() {
        // emit functions
        for (FunctionEntity functionEntity : functionEntities) {
            currentFunction = functionEntity;
            tmpStack = new ArrayList<>();
            tmpTop = 0;
            functionEntity.setINS(emitFunction(functionEntity));
            functionEntity.setTmpStack(tmpStack);
        }
    }

    public List<Instruction> emitFunction(FunctionEntity entity) {
        ins = new LinkedList<>();
        for (IR ir : entity.IR()) {
            tmpTop = exprDepth = 0;
            ir.accept(this);
        }
        return ins;
    }

    /*
     * IR Visitor
     */
    private boolean isPowerOf2(Expr ir) {
        if (ir instanceof IntConst) {
            int x = ((IntConst) ir).value();
            return x == 1 || x == 2 || x == 4 || x == 8 || x == 16;
        }
        return false;
    }

    int exprDepth = 0;
    public Operand visitExpr(com.mercy.compiler.IR.Expr ir) {
        boolean matched = false;
        Operand ret = null;

        exprDepth++;

        if (ir instanceof Binary) {
            Binary bin = (Binary)ir;
            if (bin.operator() == ADD) {
                if (bin.right() instanceof Binary && ((Binary) bin.right()).operator() == MUL
                        && isPowerOf2(((Binary) bin.right()).right())) {
                    int backupTop = tmpTop;

                    Operand base = visitExpr(bin.left());
                    Operand index = visitExpr(((Binary) bin.right()).left());


                    if (base instanceof Reference) {
                        ret = base;
                        ins.add(new Lea(ret, new Address(base, index, ((IntConst) ((Binary) bin.right()).right()).value())));
                        tmpTop = backupTop + 1; // only leave ret, remove other useless tmp register
                    } else {
                        tmpTop = backupTop; // only leave ret, remove other useless tmp register
                        ret = getTmp();
                        ins.add(new Move(ret, base));     // TODO : optimize here
                        ins.add(new Lea(ret, new Address(ret, index, ((IntConst) ((Binary) bin.right()).right()).value())));
                    }

                    matched = true;
                }
            }
        }

        if (!matched) {
            ret = ir.accept(this);
        }
        exprDepth--;
        return ret;
    }

    public Operand visit(com.mercy.compiler.IR.Addr ir) {
        return new Address(ir.entity());
    }

    // base, index, mul
    private Triple<Expr, Expr, Integer> matchAddress(Expr expr) {
        if (!(expr instanceof Binary))
            return null;
        Binary bin = (Binary)expr;

        Expr base = null, index = null;
        int mul = 0;
        boolean matched = false;
        if (bin.operator() == ADD) {
            if (bin.right() instanceof Binary && ((Binary) bin.right()).operator() == MUL) {
                base = bin.left();
                Binary right = (Binary) bin.right();
                if (isPowerOf2(right.right())) {
                    index = right.left();
                    mul = ((IntConst) right.right()).value();
                    matched = true;
                } else if (isPowerOf2(right.left())) {
                    index = right.right();
                    mul = ((IntConst) right.left()).value();
                    matched = true;
                }
            } else if (bin.left() instanceof Binary && ((Binary) bin.left()).operator() == MUL) {
                base = bin.right();
                Binary left = (Binary) bin.left();
                if (isPowerOf2(left.right())) {
                    index = left.left();
                    mul = ((IntConst) left.right()).value();
                    matched = true;
                } else if (isPowerOf2(left.left())) {
                    index = left.right();
                    mul = ((IntConst) left.left()).value();
                    matched = true;
                }
            }
        }
        if (matched) {
            return new Triple<>(base, index, mul);
        } else {
            return null;
        }
    }


    boolean leftValueWanted = false;
    private boolean notuseTemp = false;
    public Operand visit(com.mercy.compiler.IR.Assign ir) {
        Triple<Expr, Expr, Integer> addr;
        Operand dest = null;

        if (ir.left() instanceof Var) {
            dest = new Address(new Address(((Var) ir.left()).entity()));
        } else if (ir.left() instanceof Addr) {
            dest = new Address(((Addr) ir.left()).entity());
        } else if (ir.left() instanceof Mem) {
            addr = matchAddress(((Mem) ir.left()).expr());
            if (addr == null) {
                throw new InternalError("Unhanded case in IR Assign Mem: " + ir.left());
            }
/*
            Operand base = visitExpr(addr.first);
            Operand index = visitExpr(addr.second);

            dest = new Address(base, index, addr.third);*/
        } else if (ir.left() instanceof Binary) {

        } else {
            throw new InternalError("Unhandled case in IR Assign left: " + ir.left());
        }

        if (dest != null) {
            if (ir.right() instanceof Mem && (addr = matchAddress(((Mem) ir.right()).expr())) != null) {
                Operand base = visitExpr(addr.first);
                Operand index = visitExpr(addr.second);

                ins.add(new Move(dest, new Address(base, index, addr.third)));
            } else {
                exprDepth++;
                Operand rhs = visitExpr(ir.right());
                exprDepth--;
                ins.add(new Move(dest, rhs));
            }
        } else {
            leftValueWanted = true;
            Operand lhs = visitExpr(ir.left());
            leftValueWanted = false;
            if (ir.right() instanceof Mem && (addr = matchAddress(((Mem) ir.right()).expr())) != null) {
                Operand base = visitExpr(addr.first);
                Operand index = visitExpr(addr.second);

                if (ir.left() instanceof Addr) {
                    ins.add(new Move(lhs, new Address(base, index, addr.third)));
                } else {
                    ins.add(new Move(new Address(lhs), new Address(base, index, addr.third)));
                }
            } else {
                exprDepth++;
                Operand rhs = visitExpr(ir.right());
                exprDepth--;
                if (ir.left() instanceof Addr) {
                    ins.add(new Move(lhs, rhs));
                } else {
                    ins.add(new Move(new Address(lhs), rhs));
                }
            }
        }

        return null;
    }

    private Operand addBinary(com.mercy.compiler.IR.Binary.BinaryOp operator, Operand left, Operand right) {
        if (left instanceof Address) {
            switch (((Address) left).type()) {
                case BASE_ONLY:
                    ins.add(new Move(((Address) left).base(), left));
                    left = ((Address) left).base();
                    break;
                case BASE_INDEX_MUL:
                    ins.add(new Move(((Address) left).base(), left));
                    left = ((Address) left).base();
                    break;
                case ENTITY:
                default:
                    throw new InternalError("Unhandled case in IR Binary " + left.getClass());
            }
        }
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
        return left;
    }

    List<Reference> tmpStack;
    int tmpTop = 0;
    public Reference getTmp() {
        if (tmpTop >= tmpStack.size())
            tmpStack.add(new Reference());
        return tmpStack.get(tmpTop++);
    }

    private boolean isCommutative(com.mercy.compiler.IR.Binary.BinaryOp op) {
        switch(op) {
            case ADD: case MUL:
            case BIT_AND: case BIT_OR: case BIT_XOR:
            case EQ: case NE:
                return true;
            default:
                return false;
        }
    }

    public Operand visit(com.mercy.compiler.IR.Binary ir) {
        Operand ret;
        if (ir.left() instanceof IntConst && isCommutative(ir.operator())) {
            ret = visitExpr(ir.right());
            ret = addBinary(ir.operator(), ret, new Immediate(((IntConst) ir.left()).value()));
        } else if (ir.right() instanceof IntConst) {
            ret = visitExpr(ir.left());
            ret = addBinary(ir.operator(), ret, new Immediate(((IntConst) ir.right()).value()));
        } else {
            ret = visitExpr(ir.left());
            if (ret instanceof Immediate) {  // not needy in the first two cases
                Reference tmp = getTmp();
                ins.add(new Move(tmp, ret));
                ret = tmp;
            }
            Operand right = visitExpr(ir.right());
            ret = addBinary(ir.operator(), ret, right);
            tmpTop--;   // remove right
        }
        return ret;
    }

    public Operand visit(com.mercy.compiler.IR.Call ir) {
        List<Operand> operands = new LinkedList<>();

        int backupTop = tmpTop;
        for (Expr arg : ir.args()) {
            exprDepth++;
            operands.add(visitExpr(arg));
            exprDepth--;
        }
        tmpTop = backupTop;

        Reference ret = null;
        Call call = new Call(ir.entity(), operands);
        if (exprDepth != 0) {
            ret = getTmp();
            call.setRet(ret);
        }
        ins.add(call);

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
        if (ret instanceof Immediate) {
            Reference tmp = getTmp();
            ins.add(new Move(tmp, ret));
            ret = tmp;
        }
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
        Triple<Expr, Expr, Integer> triple;

        /*if (!leftValueWanted && (triple = matchAddress(ir.expr())) != null) {
            int backupTop = tmpTop;
            Operand base =  visitExpr(triple.first);
            Operand index =  visitExpr(triple.second);
            tmpTop = backupTop + 1;

            ins.add(new Move(base, new Address(base, index, triple.third)));
            return base;
        } else */{
            Operand addr = visitExpr(ir.expr());
            if (ir.expr() instanceof Addr) {
                throw new InternalError("Unhandled case in IR Mem " + ir.expr());
            } else {       // should add address "[]" in this case
                if (addr instanceof Reference) {
                /*ret = (Reference)addr;
                ins.add(new Move(ret, new Address(addr)));*/
                    return new Address(addr);
                } else {
                    throw new InternalError("unhanded address type in IR Mem" + addr);
                }
            }
        }
    }

    public Operand visit(com.mercy.compiler.IR.StrConst ir) {
        Reference ret = getTmp();
        ins.add(new Move(ret, new Address(ir.entity())));
        return ret;
    }

    public Operand visit(com.mercy.compiler.IR.IntConst ir) {
        return new Immediate(ir.value());
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
        out.println("========== INS " + entity.name() + " ==========");
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
    private int tmpTop = -1;
    private void virtualPush(Operand base) {
        tmpTop++;
        if (tmpTop >= virtualStack.size()) {
            virtualStack.add(new Reference());
        }
        ins.add(new Move(virtualStack.get(tmpTop), base));
    }

    private Operand virtualPop() {
        return virtualStack.get(tmpTop--);
    }

 */