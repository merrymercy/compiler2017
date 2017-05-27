package com.mercy.compiler.INS.Operand;

import com.mercy.compiler.Entity.Entity;
import com.mercy.compiler.INS.Move;
import com.mercy.compiler.Utility.InternalError;

import java.util.HashSet;
import java.util.Set;

import static com.mercy.compiler.BackEnd.Translator.GLOBAL_PREFIX;
import static com.mercy.compiler.INS.Operand.Reference.Type.*;

/**
 * Created by mercy on 17-4-25.
 */
public class Reference extends Operand {
    public enum Type {
        GLOBAL, OFFSET, REG, UNKNOWN, UNUSED
    }

    Type type;
    String name;
    int offset;
    Register reg;
    Entity entity;
    int refTimes;

    // for allocator, speed
    public Set<Reference> adjList = new HashSet<>();
    public Set<Reference> originalAdjList = new HashSet<>();
    public int degree;
    public Reference alias;
    public Register color;
    public Set<Move> moveList = new HashSet<>();
    public boolean isPrecolored;
    public boolean isSpilled;

    public void reset() {
        refTimes = 0;
        moveList = new HashSet<>();
        adjList = new HashSet<>();
        originalAdjList = new HashSet<>();
        if (!isPrecolored) {
            color = null;
            degree = 0;
        } else {
            degree = 999999;
        }
        alias = null;
        isSpilled = false;
    }

    public Reference(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public Reference(Register reg) {
        setRegister(reg);
        name = reg.name();
    }

    public Reference(int offset, Register reg) {
        setOffset(offset, reg);
    }

    public Reference(Entity entity) {
        this.name = entity.name();
        this.entity = entity;
        this.type = UNKNOWN;
    }

    /*
     * getter and setter
     */
    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOffset(int offset, Register reg) {
        this.offset = offset;
        this.reg = reg;
        this.type = OFFSET;
    }

    public void setRegister(Register reg) {
        this.reg = reg;
        this.type = REG;
    }

    public Register reg() {
        return reg;
    }

    public Type type() {
        return type;
    }

    public int offset() {
        return offset;
    }

    public Entity entity() {
        return entity;
    }

    public boolean isUnknown() {
        return type == UNKNOWN;
    }

    public void addRefTime() {
        refTimes++;
    }

    public int refTimes() {
        return refTimes;
    }

    @Override
    public Operand replace(Operand from, Operand to) {
        return this == from ? to : this;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Reference) {
            Reference other = (Reference)o;
            if (type != other.type)
                return false;
            switch (type) {
                case REG: return reg == other.reg;
                case OFFSET:
                    return reg == other.reg && offset == other.offset;
                case GLOBAL:
                    return name == other.name();
                case UNKNOWN:
                    return this == other;
                default:
                    throw new InternalError("Unhandled case in Reference.equals()");
            }
        }
        return false;
    }

    @Override
    public Set<Reference> getAllRef() {
        Set<Reference> ret = new HashSet<>();
        if (this.type != GLOBAL)
            ret.add(this);
        return ret;
    }

    @Override
    public boolean isRegister() {
        return type == REG;
    }

    @Override
    public boolean isDirect() {
        return true;
    }

    @Override
    public String toNASM() {
        switch (type) {
            case GLOBAL: return "qword " + "[" + GLOBAL_PREFIX + name + "]";
            case OFFSET: return "qword " + "[" + reg.name() + "+" + offset + "]";
            case REG:    return reg.name();
            case UNUSED:
            case UNKNOWN:
            default:
                throw new InternalError("Unallocated reference " + this);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
