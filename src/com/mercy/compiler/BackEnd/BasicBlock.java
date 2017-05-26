package com.mercy.compiler.BackEnd;

import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Label;
import com.mercy.compiler.INS.Operand.Reference;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

    private Set<Reference> use = new HashSet<>();
    private Set<Reference> def = new HashSet<>();

    private Set<Reference> liveIn = new HashSet<>();
    private Set<Reference> liveOut = new HashSet<>();

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

    public Set<Reference> liveIn() {
        return liveIn;
    }

    public void setLiveIn(Set<Reference> liveIn) {
        this.liveIn = liveIn;
    }

    public Set<Reference> liveOut() {
        return liveOut;
    }

    public void setLiveOut(Set<Reference> liveOut) {
        this.liveOut = liveOut;
    }

    public Set<Reference> use() {
        return use;
    }

    public void setUse(Set<Reference> use) {
        this.use = use;
    }

    public Set<Reference> def() {
        return def;
    }

    public void setDef(Set<Reference> def) {
        this.def = def;
    }

    public void setIns(List<Instruction> ins) {
        this.ins = ins;
    }

    @Override
    public String toString() {
        return label().toString();
    }
}
