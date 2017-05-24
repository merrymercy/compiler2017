package com.mercy.compiler.INS.Operand;

import com.mercy.compiler.Utility.InternalError;

import java.util.Set;

/**
 * Created by mercy on 17-5-4.
 */
public class Register extends Operand {
    private String name;
    private boolean isCalleeSave;

    public Register(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public boolean calleeSave() {
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
    public Set<Reference> getAllRef() {
        throw new InternalError("Invalid getAllRef of Register");
        //return null;
    }

    @Override
    public boolean isRegister() {
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
