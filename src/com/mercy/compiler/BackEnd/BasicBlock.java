package com.mercy.compiler.BackEnd;

import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Label;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mercy on 17-5-23.
 */
public class BasicBlock {
    private List<BasicBlock> predecessor = new LinkedList<>();
    private List<BasicBlock> successor = new LinkedList<>();
    private Label label;
    private List<Instruction> ins = new LinkedList<>();
    private List<Label> jumpTo = new LinkedList<>();
    private boolean layouted = false;

    BasicBlock(Label label) {
        this.label = label;
        label.setBasicBlock(this);
    }

    public List<BasicBlock> predecessor() {
        return predecessor;
    }

    public List<BasicBlock> successor() {
        return successor;
    }

    public Label label() {
        return label;
    }

    public List<Instruction> ins() {
        return ins;
    }

    public List<Label> jumpTo() {
        return jumpTo;
    }

    public boolean layouted() {
        return layouted;
    }

    public void setLayouted(boolean layouted) {
        this.layouted = layouted;
    }

    void addIns(Instruction instruction) {
        ins.add(instruction);
    }

    void addPredecessor(BasicBlock bb) {
        predecessor.add(bb);
    }

    void addSuccessor(BasicBlock bb) {
        successor.add(bb);
    }

    void addJumpTo(Label label) {
        jumpTo.add(label);
    }
}
