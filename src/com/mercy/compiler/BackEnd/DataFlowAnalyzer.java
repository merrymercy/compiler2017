package com.mercy.compiler.BackEnd;

import com.mercy.Option;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.INS.Bin;
import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Label;
import com.mercy.compiler.INS.Move;
import com.mercy.compiler.INS.Operand.Address;
import com.mercy.compiler.INS.Operand.Immediate;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.IR.Expr;
import com.mercy.compiler.Utility.InternalError;
import com.mercy.compiler.Utility.Pair;

import java.util.*;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * Created by mercy on 17-5-29.
 */
public class DataFlowAnalyzer {
    List<FunctionEntity> functionEntities;
    public DataFlowAnalyzer(InstructionEmitter emitter) {
        functionEntities = emitter.functionEntities();
    }

    public void transform() {
        for (FunctionEntity functionEntity : functionEntities) {
            if (functionEntity.isInlined())
                continue;
            for (BasicBlock basicBlock : functionEntity.bbs()) {
                commonSubexpressionElimination(basicBlock);
            }


            for (BasicBlock basicBlock : functionEntity.bbs()) {
                constantPropagation(basicBlock);
            }
            refreshDefAndUse(functionEntity);

            initLivenessAnalysis(functionEntity);
            livenessAnalysis(functionEntity);

            for (BasicBlock basicBlock : functionEntity.bbs()) {
                deadCodeElimination(basicBlock);
            }
            refreshDefAndUse(functionEntity);
        }
        err.println("dead code : " + ct);
    }

    private void refreshDefAndUse(FunctionEntity entity) {
        for (BasicBlock basicBlock : entity.bbs()) {
            for (Instruction ins : basicBlock.ins()) {
                ins.initDefAndUse();
                ins.calcDefAndUse();
            }
        }
    }

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
                hash *= right.hashCode();
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

    Map<Reference, Reference>  copyTable = new HashMap<>();
    Map<Expression, Reference> exprTable = new HashMap<>();

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
    private void putExpr(Reference res, Expression expr) {  // put this expression into exprTable
        removeKey(res);
        exprTable.put(expr, res);
    }
    private void putCopy(Reference dest, Reference src) {
        err.println("copy " + dest.name() + " = " + src.name());
        copyTable.put(dest, src);
    }
    private Operand replaceCopy(Operand operand) {
        for (Map.Entry<Reference, Reference> entry : copyTable.entrySet()) {
            Reference from = entry.getKey();
            Reference to = entry.getValue();
            operand = operand.replace(from, to);
        }
        return operand;
    }

    private void commonSubexpressionElimination(BasicBlock basicBlock) {
        exprTable = new HashMap<>();
        copyTable = new HashMap<>();
        List<Instruction> newIns = new LinkedList<>();
        for (Instruction ins : basicBlock.ins()) {
            if (ins instanceof Move) {
                if (((Move) ins).dest().isAddress()) {        // store
                    copyTable.clear(); exprTable.clear();
                } else if (((Move) ins).isRefMove()) {        // move ref1, ref2 (copy propagation
                    Reference dest = (Reference) ((Move) ins).dest();
                    Reference src = (Reference) ((Move) ins).src();
                    src = (Reference)replaceCopy(src);
                    putCopy(dest, src);
                } else {                                     //  move ref1, expr
                    Operand src = replaceCopy(((Move) ins).src());
                    Expression exprSrc = new Expression("unary", src, null);
                    Reference res = exprTable.get(exprSrc);
                    if (res == null) {
                        putExpr((Reference) ((Move) ins).dest(), exprSrc);
                    } else {
                        putCopy((Reference)((Move) ins).dest(), res);
                        ((Move) ins).setSrc(res);
                    }
                }
            } else if (ins instanceof Bin) {
                if (((Bin) ins).left().isAddress()) {          // add [ref1], ref2
                    copyTable.clear(); exprTable.clear();
                } else {                                       // add ref1, 12
                    Reference dest  = (Reference) ((Bin) ins).left();
                    Reference src1 = (Reference)replaceCopy(dest);

                    Operand src2 = replaceCopy(((Bin) ins).right());

                    Expression expr = new Expression(((Bin) ins).name(), src1, src2);

                    Reference res = exprTable.get(expr);
                    if (res == null) {
                        putExpr(dest, expr);
                    } else {
                        ins = new Move(dest, res);
                    }
                }
            } else {
                exprTable.clear();
            }
            newIns.add(ins);
        }
        basicBlock.setIns(newIns);
    }

    /**
     * constant propagation and folding
     */
    Pair<Boolean, Integer> getConstant(Operand operand) {
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
        }
        return new Pair<>(isConstant, value);
    }

    Map<Reference, Integer> constantTable = new HashMap<>();
    private void constantPropagation(BasicBlock basicBlock) {
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
                        case "sal" : value = left.second << right.second; break;
                        case "sar" : value = left.second >> right.second; break;
                        case "add" : value = left.second + right.second; break;
                        case "sub" : value = left.second - right.second; break;
                        case "and" : value = left.second & right.second; break;
                        case "imul" :value = left.second * right.second; break;
                        case "div" : value = left.second / right.second; break;
                        case "mod" : value = left.second % right.second; break;
                        case "xor" : value = left.second ^ right.second; break;
                        case "or" :  value = left.second | right.second; break;
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
            } else if (ins instanceof Label) {
                newIns.add(ins);
            } else {
                constantTable.clear();
                newIns.add(ins);
            }
        }
        basicBlock.setIns(newIns);

    }


    /**
     *  dead code elimination
     */
    int ct = 0;
    private void deadCodeElimination(BasicBlock basicBlock) {
        List newIns = new LinkedList();
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

                if (!dead)
                    newIns.add(ins);
                if (dead) {
                    ct++;
                }
            } else {
                newIns.add(ins);
            }
        }
        basicBlock.setIns(newIns);
    }

    List<BasicBlock> sorted;
    Set<BasicBlock> visited;
    private void dfsSort(BasicBlock bb) {
        sorted.add(bb);
        visited.add(bb);
        for (BasicBlock pre : bb.predecessor()) {
            if (!visited.contains(pre)) {
                dfsSort(pre);
            }
        }
    }

    void initLivenessAnalysis(FunctionEntity entity) {
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
        // print Def and Use
        if (Option.printGlobalAllocationInfo) {
            err.println("====== USE & DEF ======");
            for (BasicBlock basicBlock : entity.bbs()) {
                for (Instruction ins : basicBlock.ins()) {
                    err.printf("%-20s def:", ins.toString());
                    for (Reference reference : ins.def()) {
                        err.print(" " + reference);
                    }
                    err.print("       use: ");
                    for (Reference reference : ins.use()) {
                        err.print(" " + reference);
                    }
                    err.println();
                }
                err.println();
            }
        }

        /***** solve dataflow equation *****/
        // in block
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
        // among blocks
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
