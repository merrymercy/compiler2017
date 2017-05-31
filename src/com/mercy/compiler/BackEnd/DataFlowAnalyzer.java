package com.mercy.compiler.BackEnd;

import com.mercy.Option;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.INS.*;
import com.mercy.compiler.INS.Operand.Address;
import com.mercy.compiler.INS.Operand.Immediate;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.Utility.InternalError;
import com.mercy.compiler.Utility.Pair;

import java.util.*;

import static java.lang.System.err;

/**
 * Created by mercy on 17-5-29.
 */
public class DataFlowAnalyzer {
    List<FunctionEntity> functionEntities;
    FunctionEntity currentFunction;
    public DataFlowAnalyzer(InstructionEmitter emitter) {
        functionEntities = emitter.functionEntities();
    }

    public void transform() {
        for (FunctionEntity functionEntity : functionEntities) {
            if (functionEntity.isInlined())
                continue;
            currentFunction = functionEntity;

            if (Option.enableCommonExpressionElimination) {
                commonSubexpressionElimination(functionEntity);
            }

            if (Option.enableConstantPropagation) {
                constantPropagation(functionEntity);
                refreshDefAndUse(functionEntity);
            }

            if (Option.enableDeadcodeElimination) {
                initLivenessAnalysis(functionEntity);
                for (int i = 0; i < 2; i++) {  // iterate for 2 times
                    livenessAnalysis(functionEntity);
                    for (BasicBlock basicBlock : functionEntity.bbs()) {
                        deadCodeElimination(basicBlock);
                    }
                    refreshDefAndUse(functionEntity);
                }
            }
        }
        err.println("dead code : " + deadcodeCt);
    }

    private void refreshDefAndUse(FunctionEntity entity) {
        for (BasicBlock basicBlock : entity.bbs()) {
            for (Instruction ins : basicBlock.ins()) {
                ins.initDefAndUse();
                ins.calcDefAndUse();
            }
        }
    }

    /**
     * Common Subexpression Elimination (only handle Mov, Lea, Bin. todo: Neg)
     */
    class Expression {
        public String name;
        public Operand left;
        public Operand right;

        public Expression(String name, Operand left, Operand right) {
            this.name = name;
            this.left = left;
            this.right = right;
        }

        @Override
        public int hashCode() {
            int hash = name.hashCode();
            if (left != null)
                hash *= left.hashCode();
            if (right != null)
                hash += right.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Expression) {
                boolean first = name.equals(((Expression) o).name)
                        && left.equals(((Expression) o).left)
                        && ((right == null && ((Expression) o).right == null) || right.equals(((Expression) o).right));
                return first;
            }
            return  false;
        }
    }

    private Map<Reference, Reference>  copyTable; // copy propagation
    private Map<Expression, Reference> exprTable; // expression table

    private void commonSubexpressionElimination(FunctionEntity entity) {
        for (BasicBlock basicBlock : entity.bbs()) { // local
            exprTable = new HashMap<>();
            copyTable = new HashMap<>();
            List<Instruction> newIns = new LinkedList<>();
            for (Instruction ins : basicBlock.ins()) {
                err.println(ins.toString());
                List<Instruction> toadd = new LinkedList<>();
                if (ins instanceof Move) {
                    if (((Move) ins).dest().isAddress()) {        // store
                        exprTable.clear(); copyTable.clear();
                    } else if (((Move) ins).isRefMove()) {        // move ref1, ref2 (copy propagation
                        Reference dest = (Reference) ((Move) ins).dest();
                        Reference src = (Reference) ((Move) ins).src();
                        src = (Reference)replaceCopy(src);
                        transformMove(dest, src, toadd);
                    } else {                                     //  load ref1, expr
                        Reference dest = (Reference) ((Move) ins).dest();
                        Operand src = replaceCopy(((Move) ins).src());
                        Expression exprSrc = new Expression("unary", src, null);
                        Reference res = exprTable.get(exprSrc);
                        if (res == null) {
                            transformExpr(dest, exprSrc, toadd);
                        } else {
                            ins = new Move(dest, res);
                            transformMove(dest, res, toadd);
                        }
                    }
                } else if (ins instanceof Bin) {
                    if (((Bin) ins).left().isAddress()) {          // add [ref1], ref2
                        exprTable.clear(); copyTable.clear();
                    } else {                                       // add ref1, 12
                        Reference dest  = (Reference) ((Bin) ins).left();

                        Reference src1 = (Reference)replaceCopy(dest);
                        Operand src2 = replaceCopy(((Bin) ins).right());

                        Expression expr = new Expression(((Bin) ins).name(), src1, src2);
                        Reference res = exprTable.get(expr);
                        if (res == null) {
                            transformExpr(dest, expr, toadd);
                        } else {
                            ins = new Move(dest, res);
                            transformMove(dest, res, toadd);
                        }
                    }
                } else if (ins instanceof Lea) {  // the same as move
                    Reference dest = ((Lea) ins).dest();
                    Operand src = replaceCopy(((Lea) ins).addr());

                    Expression exprSrc = new Expression("dis address", src, null);
                    Reference res = exprTable.get(exprSrc);
                    if (res == null) {
                        transformExpr(dest, exprSrc, toadd);
                    } else {
                        ins = new Move(dest, res);
                        transformMove(dest, res, toadd);
                    }
                } else if (ins instanceof Label)  {
                    ; // do nothing
                } else {
                    exprTable.clear(); copyTable.clear();
                }
                newIns.add(ins);
                newIns.addAll(toadd);
            }
            basicBlock.setIns(newIns);
        }
    }

    private void removeKey(Reference toremove) {
        // remove in expr table
        for (Map.Entry<Expression, Reference> entry : exprTable.entrySet()) {
            if (entry.getValue() == toremove) {
                exprTable.remove(entry.getKey());
                break;
            }
        }
        // remove in copy table
        copyTable.remove(toremove);
        List<Reference> toremoveKeys = new LinkedList<>();
        for (Map.Entry<Reference, Reference> entry : copyTable.entrySet()) {
            if (entry.getValue() == toremove) {
                toremoveKeys.add(entry.getKey());
            }
        }
        for (Reference toremoveKey : toremoveKeys) {
            copyTable.remove(toremoveKey);
        }
    }
    private int tmpCt = 0;
    private void putExpr(Reference res, Expression expr) {  // put expression into exprTable
        removeKey(res);
        exprTable.put(expr, res);
    }
    private void putCopy(Reference dest, Reference src) {
        copyTable.put(dest, src);
    }
    private Operand replaceCopy(Operand operand) {         // replace all the copies in a specific operand
        for (Map.Entry<Reference, Reference> entry : copyTable.entrySet()) {
            Reference from = entry.getKey();
            Reference to = entry.getValue();
            operand = operand.replace(from, to);
        }
        return operand;
    }
    // It's a trick here. transform 2-address instruction into 3-address instruction
    private void transformMove(Reference dest, Reference src, List<Instruction> toadd) {
        Reference copy = new Reference("tmp_copy_" + tmpCt++, Reference.Type.UNKNOWN);
        putCopy(copy, src);
        putCopy(dest, src);
        toadd.add(new Move(copy, dest));
        currentFunction.tmpStack().add(copy);

    }
    private void transformExpr(Reference dest, Expression expr, List<Instruction> toadd) {
        Reference copy = new Reference("tmp_copy_" + tmpCt++, Reference.Type.UNKNOWN);
        putExpr(copy, expr);
        putCopy(dest, copy);
        toadd.add(new Move(copy, dest));
        currentFunction.tmpStack().add(copy);
    }


    /**
     * Constant Propagation and Folding (only handle Mov, Lea, Bin. todo: Neg)
     */
    private Map<Reference, Integer> constantTable = new HashMap<>();
    private void constantPropagation(FunctionEntity entity) {
        for (BasicBlock basicBlock : entity.bbs()) { // local
            constantTable = new HashMap<>();
            List<Instruction> newIns = new LinkedList<>();
            for (Instruction ins : basicBlock.ins()) {
                if (ins instanceof Move) {
                    Operand dest = ((Move) ins).dest();
                    Operand src  = ((Move) ins).src();

                    if (!dest.isAddress()) {
                        Pair<Boolean, Integer> ret = getConstant(src);

                        if (ret.first) {
                            constantTable.put((Reference) dest, new Integer(ret.second));
                            ((Move)ins).setSrc(new Immediate(ret.second));
                        }
                    }
                    newIns.add(ins);
                } else if (ins instanceof Bin) {
                    Pair<Boolean, Integer> left = getConstant(((Bin) ins).left());
                    Pair<Boolean, Integer> right = getConstant(((Bin) ins).right());

                    if (left.first && right.first) {
                        int value = 0;
                        switch (((Bin) ins).name()) {
                            case "sal":
                                value = left.second << right.second;
                                break;
                            case "sar":
                                value = left.second >> right.second;
                                break;
                            case "add":
                                value = left.second + right.second;
                                break;
                            case "sub":
                                value = left.second - right.second;
                                break;
                            case "and":
                                value = left.second & right.second;
                                break;
                            case "imul":
                                value = left.second * right.second;
                                break;
                            case "div":
                                value = left.second / right.second;
                                break;
                            case "mod":
                                value = left.second % right.second;
                                break;
                            case "xor":
                                value = left.second ^ right.second;
                                break;
                            case "or":
                                value = left.second | right.second;
                                break;
                            default:
                                throw new InternalError("invalid operator in constant propagation");
                        }
                        constantTable.put((Reference) ((Bin) ins).left(), new Integer(value));
                        newIns.add(new Move(((Bin) ins).left(), new Immediate(value)));
                    } else {
                        if (left.first)
                            constantTable.remove(((Bin) ins).left());
                        newIns.add(ins);
                    }
                } else if (ins instanceof Lea) {
                    replaceAddress(((Lea) ins).addr());
                    Pair<Boolean, Integer> dest = getConstant(((Lea) ins).dest());
                    if (dest.first)
                        constantTable.remove(((Lea) ins).dest());
                    newIns.add(ins);
                } else if (ins instanceof Label) {
                    newIns.add(ins);
                } else {
                    constantTable.clear();
                    newIns.add(ins);
                }
            }
            basicBlock.setIns(newIns);
        }
    }
    // look up constant table to find the result of specific operand
    private Pair<Boolean, Integer> getConstant(Operand operand) {
        if (operand == null)
            return new Pair<>(false, null);
        boolean isConstant = false;
        int value = 0;
        if (operand.isConstInt()){
            isConstant = true;
            value = ((Immediate)operand).value();
        } else {
            Integer find = constantTable.get(operand);
            if (find != null) {
                isConstant = true;
                value = find.intValue();
            }
            if (operand instanceof Address) {
                replaceAddress((Address)operand);
            }
        }
        return new Pair<>(isConstant, value);
    }
    // replace constant in address
    private void replaceAddress(Address addr) {
        Pair<Boolean, Integer> base = getConstant(addr.base());
        Pair<Boolean, Integer> index = getConstant(addr.index());
        if (index.first) {
            addr.setAdd(addr.mul() * index.second + addr.add());
            addr.setIndex(null);
        }
    }


    /**
     *  Dead Code Elimination
     */
    private int deadcodeCt = 0;
    private void deadCodeElimination(BasicBlock basicBlock) {
        List<Instruction> newIns = new LinkedList();
        for (Instruction ins : basicBlock.ins()) {
            if (ins instanceof Bin || ins instanceof Move) {
                boolean dead = false;
                if (ins.def().size() == 1) {
                    dead = true;
                    for (Reference ref : ins.def()) {
                        if (ins.out().contains(ref))
                            dead = false;
                    }
                }
                if (!dead) {
                    newIns.add(ins);
                }
                else {
                    deadcodeCt++;
                }
            } else {
                newIns.add(ins);
            }
        }
        basicBlock.setIns(newIns);
    }


    /**
     *  Data Flow Equation, Liveliness Analysis
     */
    private List<BasicBlock> sorted;
    private Set<BasicBlock> visited;
    private void dfsSort(BasicBlock bb) {
        sorted.add(bb);
        visited.add(bb);
        for (BasicBlock pre : bb.predecessor()) {
            if (!visited.contains(pre)) {
                dfsSort(pre);
            }
        }
    }

    // following is copied from com.mercy.compiler.BackEnd.Allocator
    private void initLivenessAnalysis(FunctionEntity entity) {
        // sort blocks to boost iteration, iterate in reverse
        sorted = new LinkedList<>();
        visited = new HashSet<>();
        ListIterator li = entity.bbs().listIterator(entity.bbs().size());
        while (li.hasPrevious()) {
            BasicBlock pre = (BasicBlock) li.previous();
            if (!visited.contains(pre))
                dfsSort(pre);
        }
    }

    private void livenessAnalysis(FunctionEntity entity) {
        // inside a block, use linear scan
        for (BasicBlock basicBlock : entity.bbs()) {
            Set<Reference> def = basicBlock.def();
            Set<Reference> use = basicBlock.use();
            basicBlock.liveIn().clear();
            basicBlock.liveOut().clear();
            def.clear();
            use.clear();
            for (Instruction ins : basicBlock.ins()) {
                for (Reference ref : ins.use()) {
                    if (!def.contains(ref)) {
                        use.add(ref);
                    }
                }
                for (Reference ref : ins.def()) {
                    def.add(ref);
                }
            }
        }

        // among blocks, use iteration to find fixed point
        boolean modified = true;
        while (modified) {
            modified = false;
            for (BasicBlock bb : sorted) {
                Set<Reference> newIn = new HashSet<>();
                Set<Reference> right = new HashSet<>(bb.liveOut());
                right.removeAll(bb.def());
                newIn.addAll(bb.use());
                newIn.addAll(right);

                Set<Reference> newOut = new HashSet<>();
                for (BasicBlock suc : bb.successor()) {
                    newOut.addAll(suc.liveIn());
                }

                modified |= !bb.liveIn().equals(newIn) || !bb.liveOut().equals(newOut);

                bb.setLiveIn(newIn);
                bb.setLiveOut(newOut);
            }
        }

        // inside a block, use linear scan to build live-in and live-out for every instruction
        Set<Reference> tmp;
        for (BasicBlock basicBlock : entity.bbs()) {
            HashSet<Reference> live = new HashSet<>(basicBlock.liveOut());

            // generate an iterator. Start just after the last element.
            ListIterator li = basicBlock.ins().listIterator(basicBlock.ins().size());
            while (li.hasPrevious()) {
                Instruction ins = (Instruction) li.previous();

                tmp= new HashSet<>(); tmp.addAll(live); ins.setOut(tmp);

                live.removeAll(ins.def());
                live.addAll(ins.use());

                tmp= new HashSet<>(); tmp.addAll(live); ins.setIn(tmp);
            }
        }
    }
}
