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
import com.mercy.compiler.Utility.InternalError;
import com.mercy.compiler.Utility.Pair;

import java.util.*;

import static java.lang.System.err;

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
                commonSubexpresionElimination(basicBlock);
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

    int hashOperand(Operand operand) {
        if (operand instanceof Address) {
            int hash = 1;
            Address addr = (Address)operand;
            if (addr.base() != null)
                hash *= addr.base().hashCode();
            if (addr.index() != null)
                hash += addr.index().hashCode();
            hash = hash * addr.mul() + addr.add();
            return hash;
        } else {
            return operand.hashCode();
        }
    }
    Map<Integer, Reference> exprTable = new HashMap<>();
    Reference getReference(Operand operand) {
        if (operand instanceof Address) {
            Reference ret = exprTable.get(new Integer(hashOperand(operand)));
            return ret;
        } else {
            return null;
        }
    }
    private void commonSubexpresionElimination(BasicBlock basicBlock) {
        exprTable = new HashMap<>();
        for (Instruction ins : basicBlock.ins()) {
            if (ins instanceof Move) {
                Reference ret = getReference(((Move) ins).src());
                if (ret != null) // replace
                    ((Move) ins).setSrc(ret);

                // refresh table
                if (((Move) ins).dest().isAddress()) {
                    exprTable.clear();
                } else {
                    Reference dest = (Reference)((Move) ins).dest();
                    // remove old
                    for (Map.Entry<Integer, Reference> entry : exprTable.entrySet()) {
                        if (entry.getValue() == dest) {
                            exprTable.remove(entry.getKey());
                            break;
                        }
                    }
                    // insert new
                    exprTable.put(hashOperand(((Move) ins).src()), dest);
                }
            } else if (ins instanceof Bin) {
                if (((Bin) ins).left().isAddress()) {
                    exprTable.clear();
                } else {
                    Reference dest = (Reference) ((Bin) ins).left();
                    // remove old
                    for (Map.Entry<Integer, Reference> entry : exprTable.entrySet()) {
                        if (entry.getValue() == dest) {
                            exprTable.remove(entry.getKey());
                            break;
                        }
                    }
                }
            } else {
                exprTable.clear();
            }
        }
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
