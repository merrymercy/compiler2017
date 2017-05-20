package com.mercy.compiler.INS.Operand;

import com.mercy.compiler.Entity.Entity;
import com.mercy.compiler.Entity.StringConstantEntity;
import com.mercy.compiler.Utility.InternalError;
import com.mercy.compiler.BackEnd.Translator;

import java.util.List;

import static com.mercy.compiler.BackEnd.Translator.GLOBAL_PREFIX;
import static com.mercy.compiler.INS.Operand.Reference.Type.*;

/**
 * Created by mercy on 17-4-25.
 */
public class Reference extends Operand {
    public enum Type {
        STRING, GLOBAL, OFFSET, REG, UNKNOWN
    }

    Type type;
    String name;
    int offset;
    Register reg;

    static private int counter = 0;

    public Reference(int offset, Register reg) {
        setOffset(offset, reg);
    }

    public Reference(String name, boolean isString) {
        this.name = name;
        this.type = STRING;
    }

    public Reference(String name) {
        this.name = name;
        this.type = GLOBAL;
    }

    public Reference(Register reg) {
        setRegister(reg);
    }

    public Reference() {
        this.name = "ref_" + (counter++);
        this.type = UNKNOWN;
    }

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
        assert type == REG;
        return reg;
    }

    @Override
    public boolean isRegister() {
        return type == REG;
    }

    @Override
    public String toNASM() {
        switch (type) {
            case STRING: return name;
            case GLOBAL: return "qword " + "[" + GLOBAL_PREFIX + name + "]";
            case OFFSET: return "qword " + "[" + reg.name() + "-" + offset + "]";
            case REG:    return reg.name();
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
