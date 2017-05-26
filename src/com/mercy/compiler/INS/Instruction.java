package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Reference;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by mercy on 17-4-25.
 */
abstract public class Instruction {
    protected List<Instruction> predessor = new LinkedList<>();
    protected List<Instruction> sucessor = new LinkedList<>();

    protected Set<Reference> in = new HashSet<>();
    protected Set<Reference> out = new HashSet<>();

    protected Set<Reference> use;
    protected Set<Reference> def;
    protected Set<Reference> allref;
    protected Set<Reference> live;

    abstract public void replaceUse(Reference from, Reference to);
    abstract public void replaceDef(Reference from, Reference to);
    public void replaceAll(Reference from, Reference to) {
        this.replaceUse(from, to);
        this.replaceDef(from, to);
    }

    /*
     * getter and setter
     */
    public List<Instruction> predessor() {
        return predessor;
    }

    public List<Instruction> sucessor() {
        return sucessor;
    }

    public Set<Reference> in() {
        return in;
    }

    public Set<Reference> out() {
        return out;
    }

    public Set<Reference> use() {
        if (use == null) {
            initDefAndUse();
            this.calcDefAndUse();
        }
        return use;
    }

    public Set<Reference> def() {
        if (def == null) {
            initDefAndUse();
            this.calcDefAndUse();
        }
        return def;
    }

    public void initDefAndUse() {
        use = new HashSet<>();
        def = new HashSet<>();
        allref = new HashSet<>();
    }

    public Set<Reference> allref() {
        if (allref == null) {
            initDefAndUse();
            this.calcDefAndUse();
        }
        return allref;
    }

    abstract public void calcDefAndUse();

    public void setIn(Set<Reference> in) {
        this.in = in;
    }

    public void setOut(Set<Reference> out) {
        this.out = out;
    }

    public Set<Reference> live() {
        if (live == null) {
            live = new HashSet<>();
            for (Reference ref : in) {
                if (ref.alias != null)
                    live.add(ref.alias);
                else
                    live.add(ref);
            }
            for (Reference ref : out) {
                if (ref.alias != null)
                    live.add(ref.alias);
                else
                    live.add(ref);
            }
        }
        return live;
    }

    public void setLive(Set<Reference> live) {
        this.live = live;
    }

    abstract public void accept(Translator translator);
}
