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
            return x == 1 || x == 2 || x == 4 || x == 8;
        }
        return false;
    }

    int exprDepth = 0;
    public Operand visitExpr(com.mercy.compiler.IR.Expr ir) {
        boolean matched = false;
        Operand ret = null;

        exprDepth++;

        Triple<Expr, Expr, Integer> addr;

        if ((addr = matchAddress(ir)) != null) {
            int backupTop = tmpTop;
            Operand base = visitExpr(addr.first);
            Operand index = visitExpr(addr.second);

            if (base instanceof Reference) {
                tmpTop = backupTop + 1; // only leave ret, remove other useless tmp register
                ret = base;
                ins.add(new Lea(ret, new Address(base, index, addr.third)));
            } else if (base instanceof Immediate) {
                ret = getTmp();
                ins.add(new Move(ret, base));
                ins.add(new Lea(ret, new Address(ret, index, addr.third)));
            } else if (base instanceof Address) {
                tmpTop = backupTop; // only leave ret, remove other useless tmp register
                ret = getTmp();
                ins.add(new Move(ret, base));
                ins.add(new Lea(ret, new Address(ret, index, addr.third)));
            } else {
                throw new InternalError("Unhandled case in lea");
            }
            matched = true;
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

    public Operand visit(com.mercy.compiler.IR.Assign ir) {
        Operand dest = null;

        if (ir.left() instanceof Var) {
            dest = new Address(new Address(((Var) ir.left()).entity()));
        } else if (ir.left() instanceof Addr) {
            dest = new Address(((Addr) ir.left()).entity());
        }/* else if (ir.left() instanceof Mem) {
        } else if (ir.left() instanceof Binary) {
        } else {
            throw new InternalError("Unhandled case in IR Assign left: " + ir.left());
        }*/

        if (dest == null) {
            Operand lhs = visitExpr(ir.left());
            if (ir.left() instanceof Addr)
                dest = lhs;
            else
                dest = new Address(lhs);
        }

        exprDepth++;
        Operand rhs = visitExpr(ir.right());
        exprDepth--;
        ins.add(new Move(dest, rhs));

        return null;
    }

    private Operand addBinary(com.mercy.compiler.IR.Binary.BinaryOp operator, Operand left, Operand right) {
        if (left instanceof Address) {
            switch (((Address) left).type()) {
                case BASE_ONLY:
                    while(((Address) left).base() instanceof Address) {
                        left = ((Address) left).base();
                    }
                    ins.add(new Move(((Address) left).base(), left));
                    left = ((Address) left).base();
                    break;
                case BASE_INDEX_MUL:
                    while(((Address) left).base() instanceof Address) {
                        left = ((Address) left).base();
                    }
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
        Triple<Expr, Expr, Integer> addr;

        if ((addr = matchAddress(ir.expr())) != null) {
            int backupTop = tmpTop;
            Operand base =  visitExpr(addr.first);
            Operand index =  visitExpr(addr.second);
            Operand ret = base;
            tmpTop = backupTop + 1;

            while(ret instanceof Address) {
                ret = ((Address) ret).base();
            }

            ins.add(new Move(ret, new Address(base, index, addr.third)));
            return ret;
        } else {
            Operand expr = visitExpr(ir.expr());
            if (ir.expr() instanceof Addr) {
                throw new InternalError("Unhandled case in IR Mem " + ir.expr());
            } else {       // should add address "[]" in this case
                if (expr instanceof Reference) {
                    return new Address(expr);
                } else {
                    throw new InternalError("unhanded address type in IR Mem: " + expr);
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

    /*
     * temp virtual register
     */
    List<Reference> tmpStack;
    int tmpTop = 0;
    public Reference getTmp() {
        if (tmpTop >= tmpStack.size())
            tmpStack.add(new Reference());
        return tmpStack.get(tmpTop++);
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