package com.mercy.compiler.INS.Operand;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mercy on 17-5-4.
 */
public class Register extends Operand {
    private String name;
    private String lowName;
    private boolean isCalleeSave;

    public Register(String name, String lowName) {
        this.name = name;
        this.lowName = lowName;
    }

    public String name() {
        return name;
    }
    public String lowName() {
        return lowName;
    }

    public boolean isCalleeSave() {
        return isCalleeSave;
    }
    public void setCalleeSave(boolean calleeSave) {
        isCalleeSave = calleeSave;
    }

    public boolean callerSave() {
        return !isCalleeSave;
    }
    public void setCallerSave(boolean callerSave) {
        isCalleeSave = !callerSave;
    }

    @Override
    public Operand replace(Operand from, Operand to) {
        return this;
    }

    @Override
    public Set<Reference> getAllRef() {
        return new HashSet<>();
        //throw new InternalError("Invalid getAllRef of Register");
        //return null;
    }

    @Override
    public boolean isRegister() {
        return true;
    }

    @Override
    public boolean isDirect() {
        return true;
    }

    @Override
    public String toNASM() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
