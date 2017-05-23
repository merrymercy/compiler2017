package com.mercy.compiler.BackEnd;

import com.mercy.Option;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.INS.CJump;
import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Jmp;
import com.mercy.compiler.INS.Label;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
            if (Option.enableInlineFunction && functionEntity.canbeInlined())
                return;
            buildFunction(functionEntity);
            layoutFunction(functionEntity);
        }
    }

    int ct = 0;
    private void buildFunction(FunctionEntity entity) {
        List<BasicBlock> bbs = new LinkedList<>();

        BasicBlock bb = null;
        for (Instruction ins : entity.ins()) {
            if (bb == null && !(ins instanceof Label)) { // add new label
                Label label = new Label("cfg_added_" + ct++);
                bb = new BasicBlock(label);
                bb.addIns(label);
            }

            if (ins instanceof Label) {
                if (bb != null) {
                    bb.addJumpTo((Label) ins);
                    bb.ins().add(new Jmp((Label) ins));
                    bbs.add(bb);
                }
                bb = new BasicBlock((Label) ins);
                bb.addIns(ins);
            } else {
                bb.addIns(ins);
                if (ins instanceof Jmp) {
                    bb.addJumpTo(((Jmp) ins).dest());
                    bbs.add(bb);
                    bb = null;
                } else if (ins instanceof CJump) {
                    bb.addJumpTo(((CJump) ins).trueLabel());
                    bb.addJumpTo(((CJump) ins).falseLabel());
                    bbs.add(bb);
                    bb = null;
                }
            }
        }

        if (bb != null) { // handle the case that a function ends without "return"
            bb.addJumpTo(entity.endLabelINS());
            bbs.add(bb);
        }

        // link edge
        for (BasicBlock basicBlock : bbs) {
            for (Label label : basicBlock.jumpTo()) {
                if (label == entity.endLabelINS())
                    continue;
                basicBlock.addSuccessor(label.basicBlock());
                label.basicBlock().addPredecessor(basicBlock);
            }
        }

        entity.setBbs(bbs);
    }

    void layoutFunction(FunctionEntity entity) {
        List<Instruction> ins = new LinkedList<>();
        List<BasicBlock> bbs = entity.bbs();
        Queue<BasicBlock> queue = new ArrayDeque<>();

        queue.addAll(bbs);

        while(!queue.isEmpty()) {
            BasicBlock bb = queue.remove();
            while(bb != null && !bb.layouted()) {
                BasicBlock next = null;
                for (BasicBlock suc : bb.successor()) {
                    if (!suc.layouted()) {
                        if (bb.ins().get(bb.ins().size()-1) instanceof Jmp) {
                            bb.ins().remove(bb.ins().size()-1);  // remove redundant jump
                        }
                        next = suc;
                        break;
                    }
                }
                bb.setLayouted(true);
                ins.addAll(bb.ins());
                bb = next;
            }
        }

    }

    /********** DEBUG TOOL **********/

    public void printSelf(PrintStream out) {
        for (FunctionEntity functionEntity : functionEntities) {
            if (Option.enableInlineFunction && functionEntity.canbeInlined())
                return;
            out.println("========== " + functionEntity.name() + " ==========");
            if (Option.enableInlineFunction && functionEntity.canbeInlined()) {
                out.println("BE INLINED");
                continue;
            }
            for (BasicBlock basicBlock : functionEntity.bbs()) {
                out.print("----- b -----");
                for (Label label : basicBlock.jumpTo()) {
                    out.print("  " + label.name());
                }
                out.println("");
                for (Instruction instruction : basicBlock.ins()) {
                    out.println(instruction.toString());
                }
            }
        }
    }
}

