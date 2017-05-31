package com.mercy.compiler.BackEnd;

import com.mercy.Option;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.INS.CJump;
import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Jmp;
import com.mercy.compiler.INS.Label;

import java.io.PrintStream;
import java.util.*;


/**
 * Created by mercy on 17-5-23.
 */
public class ControlFlowAnalyzer {
    List<FunctionEntity> functionEntities;

    public ControlFlowAnalyzer(InstructionEmitter emitter) {
        functionEntities = emitter.functionEntities();
    }

    public void buildControlFlow() {
        for (FunctionEntity functionEntity : functionEntities) {
            if (functionEntity.isInlined())
                continue;
            buildBasicBlock(functionEntity);
            buildControFlowGraph(functionEntity);
            if (Option.enableControlFlowOptimization) {
                Optimize(functionEntity);
            }
            layoutFunction(functionEntity);
        }
    }

    int ct = 0;
    private void buildBasicBlock(FunctionEntity entity) {
        List<BasicBlock> bbs = new LinkedList<>();

        BasicBlock bb = null;
        for (Instruction ins : entity.INS()) {
            if (bb == null && !(ins instanceof Label)) { // add new label
                Label label = new Label("cfg_added_" + ct++);
                bb = new BasicBlock(label);
                bb.ins().add(label);
            }

            if (ins instanceof Label) {
                if (bb != null) {
                    bb.jumpTo().add((Label) ins);
                    bb.ins().add(new Jmp((Label) ins));
                    bbs.add(bb);
                }
                bb = new BasicBlock((Label) ins);
                bb.ins().add(ins);
            } else {
                bb.ins().add(ins);
                if (ins instanceof Jmp) {
                    bb.jumpTo().add(((Jmp) ins).dest());
                    bbs.add(bb);
                    bb = null;
                } else if (ins instanceof CJump) {
                    bb.jumpTo().add(((CJump) ins).trueLabel());
                    bb.jumpTo().add(((CJump) ins).falseLabel());
                    bbs.add(bb);
                    bb = null;
                }
            }
        }

        if (bb != null) { // handle the case that a function ends without "return"
            bb.jumpTo().add(entity.endLabelINS());
            bbs.add(bb);
        }

        // link edge
        for (BasicBlock basicBlock : bbs) {
            for (Label label : basicBlock.jumpTo()) {
                if (label == entity.endLabelINS())
                    continue;
                basicBlock.successor().add(label.basicBlock());
                label.basicBlock().predecessor().add(basicBlock);
            }
        }

        entity.setBbs(bbs);
        entity.setINS(null); // disable direct instruction access, so you must access instructions by BasicBlocks
    }

    private void buildControFlowGraph(FunctionEntity entity) {
        for (BasicBlock basicBlock : entity.bbs()) {
            // inside bb
            List<Instruction> ins = basicBlock.ins();
            Iterator<Instruction> iter = ins.iterator();
            if (iter.hasNext()) {
                Instruction pre = iter.next();
                while(iter.hasNext()) {
                    Instruction now = iter.next();
                    pre.sucessor().add(now);
                    now.predessor().add(pre);
                    pre = now;
                }
            }

            // between two bb
            Instruction first = ins.get(0);
            for (BasicBlock pre : basicBlock.predecessor()) {
                pre.ins().get(pre.ins().size()-1).sucessor().add(first);
            }

            Instruction last = ins.get(ins.size()-1);
            for (BasicBlock suc : basicBlock.successor()) {
                suc.ins().get(0).predessor().add(last);
            }
        }
    }

    void Optimize(FunctionEntity entity) {
        Set<BasicBlock> toMerge = new HashSet<>();

        boolean modified = true;
        while(modified) {
            modified = false;

            toMerge.clear();

            BasicBlock now;
            // merge
            for (BasicBlock basicBlock : entity.bbs()) {
                if (basicBlock.successor().size() == 1 && basicBlock.successor().get(0).predecessor().size() == 1
                        && basicBlock.ins().get(basicBlock.ins().size() - 1) instanceof Jmp) { // suc_size == 1 may happen when func_end is a branch of Cjump, so ignore this case
                    now = basicBlock;
                    BasicBlock next = now.successor().get(0);
                    if (next.successor().size() != 0) {
                        modified = true;
                        for (BasicBlock next_next : next.successor()) {
                            //err.println("merge " + now.label() + " <- " + next.label());
                            next_next.predecessor().remove(next);
                            next_next.predecessor().add(now);
                            now.successor().add(next_next);
                        }

                        // remove label and jmp
                        next.ins().remove(0);
                        now.ins().remove(now.ins().size()-1);

                        now.ins().addAll(next.ins());
                        entity.bbs().remove(next);
                        now.successor().remove(next);
                        break;
                    }
                }
            }

            // same branch
            for (BasicBlock basicBlock : entity.bbs()) {
                if (basicBlock.successor().size() == 2 && basicBlock.successor().get(0) == basicBlock.successor().get(1)) {
                    ;
                }
            }

            // only jump block
            List<BasicBlock> uselessBasicBlock = new LinkedList<>();
            for (BasicBlock toremove : entity.bbs()) {
                Instruction last = toremove.ins().get(1);
                if (toremove.ins().size() == 2 && last instanceof Jmp) {
                    //err.println("transform jump " + toremove.label() + " -> " + ((Jmp) last).dest());
                    List<BasicBlock> backup = new LinkedList<>(toremove.predecessor());
                    for (BasicBlock pre : backup) {
                        Instruction jump = pre.ins().get(pre.ins().size() - 1);
                        if (jump instanceof Jmp) {
                            modified = true;
                            ((Jmp) jump).setDest(((Jmp) last).dest());
                            pre.successor().remove(toremove);
                            uselessBasicBlock.add(toremove);
                            if (toremove.successor().size() == 1) {
                                BasicBlock suc = toremove.successor().get(0);
                                pre.successor().add(suc);
                                suc.predecessor().add(pre);
                            }
                        } else if (jump instanceof CJump) {
                            modified = true;
                            if (((CJump) jump).trueLabel() == toremove.label())
                                ((CJump) jump).setTrueLabel(((Jmp) last).dest());
                            if (((CJump) jump).falseLabel() == toremove.label())
                                ((CJump) jump).setFalseLabel(((Jmp) last).dest());

                            pre.successor().remove(toremove);
                            uselessBasicBlock.add(toremove);
                            if (toremove.successor().size() == 1) {
                                BasicBlock suc = toremove.successor().get(0);
                                pre.successor().add(suc);
                                suc.predecessor().add(pre);
                            }
                        }
                    }
                    if (modified)
                        break;
                }
            }

            for (BasicBlock basicBlock : entity.bbs()) {
                if (basicBlock.predecessor().size() == 0 && basicBlock.label() != entity.beginLabelINS()) {
                    modified = true;
                    uselessBasicBlock.add(basicBlock);
                }
            }

            entity.bbs().removeAll(uselessBasicBlock);
        }
    }

    void layoutFunction(FunctionEntity entity) {
        List<BasicBlock> bbs = entity.bbs();
        Queue<BasicBlock> queue = new ArrayDeque<>();

        queue.addAll(bbs);

        List<BasicBlock> newBBs = new LinkedList<>();
        List<Instruction> newIns = new LinkedList<>();

        while(!queue.isEmpty()) {
            BasicBlock bb = queue.remove();
            while(bb != null && !bb.layouted()) {
                BasicBlock next = null;
                for (BasicBlock suc : bb.successor()) {
                    if (!suc.layouted()) {
                        Instruction last = bb.ins().get(bb.ins().size()-1);
                        if (last instanceof Jmp) {
                            bb.ins().remove(bb.ins().size()-1);  // remove redundant jump
                        } else if (last instanceof CJump) {
                            ((CJump) last).setFallThrough(suc.label());
                        }
                        next = suc;
                        break;
                    }
                }
                bb.setLayouted(true);
                newBBs.add(bb);
                newIns.addAll(bb.ins());
                bb = next;
            }
        }

        entity.setBbs(newBBs);
        entity.setINS(null);
    }

    /********** DEBUG TOOL **********/
    public void printSelf(PrintStream out) {
        for (FunctionEntity functionEntity : functionEntities) {
            out.println("========== " + functionEntity.name() + " ==========");
            if (functionEntity.isInlined()) {
                out.println("BE INLINED");
                continue;
            }
            for (BasicBlock basicBlock : functionEntity.bbs()) {
                out.print("----- b -----"  + "  jump to:");
                for (Label label : basicBlock.jumpTo()) {
                    out.print("   " + label.name());
                }
                out.println();
                for (Instruction instruction : basicBlock.ins()) {
                    out.println(instruction.toString());
                }
            }
        }
    }
}

