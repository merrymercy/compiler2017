package com.mercy.compiler.BackEnd;

import com.mercy.Option;
import com.mercy.compiler.AST.FunctionDefNode;
import com.mercy.compiler.Entity.*;
import com.mercy.compiler.INS.*;
import com.mercy.compiler.INS.CJump;
import com.mercy.compiler.INS.Call;
import com.mercy.compiler.INS.Label;
import com.mercy.compiler.INS.Operand.Address;
import com.mercy.compiler.INS.Operand.Immediate;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.INS.Return;
import com.mercy.compiler.IR.*;
import com.mercy.compiler.Utility.InternalError;
import com.mercy.compiler.Utility.Triple;

import java.util.*;

import static com.mercy.compiler.INS.Operand.Reference.Type.*;
import static com.mercy.compiler.IR.Binary.BinaryOp.*;
import static java.lang.System.err;

/**
 * Created by mercy on 17-4-25.
 */
public class InstructionEmitter implements IRVisitor<Operand> {
    private List<FunctionEntity> functionEntities;
    private Scope globalScope;
    private List<IR> globalInitializer;

    private List<Instruction> ins;
    private FunctionEntity currentFunction;
    private boolean isInLeaf;

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
        int stringCounter = 1;

        // set reference for global variables
        for (Entity entity : globalScope.entities().values()) {
            //System.out.println(entity.name());
            if (entity instanceof VariableEntity) {
                entity.setReference(new Reference(entity.name(), GLOBAL));
            } else if (entity instanceof  StringConstantEntity) {
                ((StringConstantEntity) entity).setAsmName(StringConstantEntity.STRING_CONSTANT_ASM_LABEL_PREFIX + stringCounter++);
            }
        }

        // emit functions
        for (FunctionEntity functionEntity : functionEntities) {
            currentFunction = functionEntity;
            tmpStack = new ArrayList<>();
            functionEntity.setINS(emitFunction(functionEntity));
            functionEntity.setTmpStack(tmpStack);
        }
    }
    private Map<Entity, Entity> globalLocalMap = new HashMap<>();
    private Set<Entity> usedGlobal;

    private List<Instruction> emitFunction(FunctionEntity entity) {
        if (entity.isInlined())
            return null;

        // leaf function optimization
        int callSize = entity.calls().size();
        for (FunctionEntity called : entity.calls()) {
            if (called.isInlined() || called.isLibFunction())
                callSize--;
        }

        if (Option.enableLeafFunctionOptimization && callSize == 0) {
            isInLeaf = true;
            err.println(entity.name() + " is leaf");
            usedGlobal = new HashSet<>();
            // make copy to local
            for (Entity global : globalScope.entities().values()) {
                if (global instanceof  VariableEntity) {
                    VariableEntity local = new VariableEntity(global.location(), global.type(), "g_" + global.name(), null);
                    globalLocalMap.put(global, local);
                    currentFunction.scope().insert(local);
                }
            }
        } else
            isInLeaf = false;

        // set reference for params and local variable
        for (ParameterEntity parameterEntity : entity.params()) {
            parameterEntity.setReference(new Reference(parameterEntity));
            parameterEntity.setSource(new Reference(parameterEntity.name() + "_src", CANNOT_COLOR));
        }
        for (VariableEntity variableEntity : entity.allLocalVariables()) {
            variableEntity.setReference(new Reference(variableEntity));
        }

        // emit instructions
        entity.setLabelINS(getLabel(entity.beginLabelIR().name()), getLabel(entity.endLabelIR().name()));
        ins = new LinkedList<>();
        for (IR ir : entity.IR()) {
            tmpTop = exprDepth = 0;
            ir.accept(this);
        }

        // if is in leaf, add move instruction for copy global to local
        if (isInLeaf) {
            for (Entity global : usedGlobal) {
                ins.add(1, new Move(transEntity(global).reference(), global.reference()));
                ins.add(ins.size(), new Move(global.reference(), transEntity(global).reference()));
            }
        }
        return ins;
    }

    /*
     * Instruction Selection (match address)
     */
    private boolean isPowerOf2(Expr ir) {
        if (ir instanceof IntConst) {
            int x = ((IntConst) ir).value();
            return x == 1 || x == 2 || x == 4 || x == 8;
        }
        return false;
    }
    private class AddressTuple {
        Expr base, index;
        int mul, add;
        AddressTuple(Expr base, Expr index, int mul, int add) {
            this.base = base;
            this.index = index;
            this.mul = mul;
            this.add = add;
        }
    }

    // only match [base + index * mul]
    private boolean matchSimpleAdd = false;  // whether to match [base + index]
    private Triple<Expr, Expr, Integer>  matchBaseIndexMul(Expr expr) {
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
            } else if (matchSimpleAdd) {
                base = bin.left();
                index = bin.right();
                mul = 1;
                matched = true;
            }
        }
        if (matched) {
            return new Triple<>(base, index, mul);
        } else {
            return null;
        }
    }

    // match all types of address [base + index * mul + offset]
    // two step : 1.match offset 2. match [base + index * mul]
    private AddressTuple matchAddress(Expr expr) {
        if (!Option.enableInstructionSelection)
            return null;

        if (!(expr instanceof Binary))
            return null;
        Binary bin = (Binary)expr;

        Expr base = null, index = null;
        int mul = 1, add = 0;
        boolean matched = false;
        Triple<Expr, Expr, Integer> baseIndexMul = null;
        if (bin.operator() == ADD) {
            if (bin.right() instanceof IntConst) {
                add = ((IntConst)bin.right()).value();

                if ((baseIndexMul = matchBaseIndexMul(bin.left())) != null) {
                    matched = true;
                } else {
                    base = bin.left();
                }
            } else if (bin.left()  instanceof IntConst) {
                add = ((IntConst)bin.left()).value();
                if ((baseIndexMul = matchBaseIndexMul(bin.right())) != null) {
                    matched = true;
                } else {
                    base = bin.right();
                }
            } else if ((baseIndexMul = matchBaseIndexMul(bin)) != null) {
                matched = true;
            }
        }

        if (baseIndexMul != null) {
            base = baseIndexMul.first;
            index = baseIndexMul.second;
            mul = baseIndexMul.third;
        }

        if (matched) {
            // get rid of nested address
            if (base != null && matchAddress(base) != null)
                return null;

            if (index != null && matchAddress(index) != null)
                return null;

            return new AddressTuple(base, index, mul, add);
        } else {
            return null;
        }
    }

    /*
     * IR Visitor
     */
    private int exprDepth = 0;
    public Operand visitExpr(com.mercy.compiler.IR.Expr ir) {
        boolean matched = false;
        Operand ret = null;

        exprDepth++;
        AddressTuple addr;

        // instruction selection for "lea"
        if ((addr = matchAddress(ir)) != null) {
            Operand base = visitExpr(addr.base);
            Operand index = null;
            if (addr.index != null) {
                index = visitExpr(addr.index);
                index = eliminateAddress(index);
            }
            base = eliminateAddress(base);

            ret = getTmp();
            ins.add(new Lea((Reference) ret, new Address(base, index, addr.mul, addr.add)));
            matched = true;
        }

        if (!matched) {
            ret = ir.accept(this);
        }
        exprDepth--;
        return ret;
    }

    public Operand visit(com.mercy.compiler.IR.Addr ir) {
        throw new InternalError("cannot happen in instruction emitter Addr ir");
    }

    public Operand visit(com.mercy.compiler.IR.Assign ir) {
        Operand dest = null;

        if (ir.left() instanceof Addr) {
            dest = transEntity(((Addr) ir.left()).entity()).reference();
        } else {
            Operand lhs = visitExpr(ir.left());
            lhs = eliminateAddress(lhs);
            dest = new Address(lhs);
        }

        exprDepth++;
        Operand rhs = visitExpr(ir.right());
        exprDepth--;

        if (dest.isAddress() && rhs.isAddress()) {
            Reference tmp = getTmp();
            ins.add(new Move(tmp, rhs));
            ins.add(new Move(dest, tmp));
        } else
            ins.add(new Move(dest, rhs));

        return null;
    }

    private Operand addBinary(com.mercy.compiler.IR.Binary.BinaryOp operator, Operand left, Operand right) {
        left = getLvalue(left);
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

    public Operand visit(com.mercy.compiler.IR.Binary ir) {
        Operand ret;
        Expr left = ir.left(), right = ir.right();
        Binary.BinaryOp op = ir.operator();

        if (isCommutative(op) && left instanceof IntConst) {
            Expr t = left; left = right; right = t;
        }

        // use shift to boost multiplication and division
        if (op == MUL) {
            if (right instanceof IntConst && log2(((IntConst) right).value()) != -1) {
                op = LSHIFT;
                right = new IntConst(log2(((IntConst) right).value()));
            }
        } else if (op == Binary.BinaryOp.DIV) {
            if (right instanceof IntConst && log2(((IntConst) right).value()) != -1) {
                op = RSHIFT;
                right = new IntConst(log2(((IntConst) right).value()));
            }
        }

        ret = visitExpr(left);
        Operand rrr = visitExpr(right);
        ret = addBinary(op, ret, rrr);

        return ret;
    }

    public Operand visit(com.mercy.compiler.IR.Call ir) {
        List<Operand> operands = new LinkedList<>();

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

        return ret;
    }

    public Operand visit(com.mercy.compiler.IR.CJump ir) {
        if (ir.cond() instanceof Binary && isCompareOP(((Binary) ir.cond()).operator())) {
            Operand left = visitExpr(((Binary) ir.cond()).left());
            Operand right = visitExpr(((Binary) ir.cond()).right());

            CJump.Type type;
            switch (((Binary) ir.cond()).operator()) {
                case EQ: type = CJump.Type.EQ; break;
                case NE: type = CJump.Type.NE; break;
                case GT: type = CJump.Type.GT; break;
                case GE: type = CJump.Type.GE; break;
                case LT: type = CJump.Type.LT; break;
                case LE: type = CJump.Type.LE; break;
                default:
                    throw new InternalError("invalid compare operator");
            }

            if (left.isAddress()) {
                Reference tmp = getTmp();
                ins.add(new Move(tmp, left));
                left = tmp;
            }
            ins.add(new CJump(left, right, type, getLabel(ir.trueLabel().name()),
                    getLabel(ir.falseLabel().name())));
        } else {
            Operand cond = visitExpr(ir.cond());
            if (cond instanceof Immediate) {
                if (((Immediate) cond).value() != 0) {
                    ins.add(new Jmp(getLabel(ir.trueLabel().name())));
                } else {
                    ins.add(new Jmp(getLabel(ir.falseLabel().name())));
                }
            } else {
                if (cond.isAddress()) {
                    Reference tmp = getTmp();
                    ins.add(new Move(tmp, cond));
                    cond = tmp;
                }
                ins.add(new CJump(cond, getLabel(ir.trueLabel().name()),
                    getLabel(ir.falseLabel().name())));
            }
        }
        return null;
    }

    public Operand visit(com.mercy.compiler.IR.Jump ir) {
        ins.add(new Jmp(getLabel(ir.label().name())));
        return null;
    }

    public Operand visit(com.mercy.compiler.IR.Label ir) {
        ins.add(getLabel(ir.name()));
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
        ret = getLvalue(ret);
        switch (ir.operator()) {
            case MINUS:
                ins.add(new Neg(ret)); break;
            case BIT_NOT:
                ins.add(new Not(ret)); break;
            case LOGIC_NOT:
                ins.add(new Xor(ret, new Immediate(1)));
                break;
            default:
                throw new InternalError("invalid operator " + ir.operator());
        }
        return ret;
    }

    public Operand visit(com.mercy.compiler.IR.Mem ir) {
        AddressTuple addr;

        if ((addr = matchAddress(ir.expr())) != null) {
            Operand base =  visitExpr(addr.base);
            Operand index = null;
            if (addr.index != null) {
                index = visitExpr(addr.index);
                index = eliminateAddress(index);
            }
            base = eliminateAddress(base);
            Reference ret = getTmp();

            ins.add(new Move(ret, new Address(base, index, addr.mul, addr.add)));
            return ret;
        } else {
            Operand expr = visitExpr(ir.expr());
            expr = eliminateAddress(expr);
            return new Address(expr); // should add address "[]" in this case
        }
    }

    public Operand visit(com.mercy.compiler.IR.StrConst ir) {
        return new Immediate(ir.entity().asmName());
    }

    public Operand visit(com.mercy.compiler.IR.IntConst ir) {
        return new Immediate(ir.value());
    }

    public Operand visit(com.mercy.compiler.IR.Var ir) {
        if (ir.entity().name().equals("null")){
            return new Immediate(0);
        } else {
            return transEntity(ir.entity()).reference();
        }
    }

    /*
     * utility for emitter
     */
    // make the label unique, i.e. point to the same object
    private Map<String, Label> labelMap = new HashMap<>();
    private Label getLabel(String name) {
        Label ret = labelMap.get(name);
        if (ret == null) {
            ret = new Label(name);
            labelMap.put(name, ret);
        }
        return ret;
    }

    // translate entity for global in leaf function
    private Entity transEntity(Entity entity) {
        if (isInLeaf) {
            Entity ret = globalLocalMap.get(entity);
            if (ret != null) {
                usedGlobal.add(entity);
                return ret;
            }
        }
        return entity;
    }

    // to get rid of nested address
    private Operand eliminateAddress(Operand operand) {
        if (operand instanceof Address || (operand instanceof Reference && ((Reference) operand).type() == GLOBAL)) {
            Reference tmp = getTmp();
            ins.add(new Move(tmp, operand));
            return tmp;
        } else {
            return operand;
        }
    }

    // get lvalue for immediate or reference of entity
    private Reference getLvalue(Operand operand) {
        operand = eliminateAddress(operand);
        if (operand instanceof Immediate ||
                (operand instanceof Reference && !((Reference) operand).canBeAccumulator())) {
            Reference ret = getTmp();
            ins.add(new Move(ret, operand));
            return ret;
        }
        return (Reference) operand;
    }

    private int log2(int x) {
        for (int i = 0; i < 30; i++) {
            if (x == (1 << i))
                return i;
        }
        return -1;
    }

    // temp virtual register
    private List<Reference> tmpStack;
    private int tmpTop = 0;
    private int tmpCounter = 0;
    public Reference getTmp() {
        if (Option.enableGlobalRegisterAllocation) {
            return new Reference("ref_" + tmpCounter++, UNKNOWN);
        } else {
            // reuse temp register
            if (tmpTop >= tmpStack.size()) {
                tmpStack.add(new Reference("ref_" + tmpCounter, UNKNOWN));
            }
            return tmpStack.get(tmpTop++);
        }
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

    private boolean isCompareOP(Binary.BinaryOp op) {
        switch (op) {
            case EQ: case NE:
            case GT: case GE:
            case LT: case LE:
                return true;
            default:
                return false;
        }
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
}