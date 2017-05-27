package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Reference;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mercy on 17-4-25.
 */
public class Jmp extends Instruction {
    Label dest;
    public Jmp(Label dest) {
        this.dest = dest;
    }
    Set<Reference> bringOut;

    public Label dest() {
        return dest;
    }

    public void setDest(Label dest) {
        this.dest = dest;
    }

    public Set<Reference> bringOut() {
        return bringOut;
    }

    public void setBringOut(Set<Reference> bringOut) {
        this.bringOut = bringOut;
    }

    @Override
    public void replaceUse(Reference from, Reference to) {
        if (bringOut != null && bringOut.contains(from)) {
            Set<Reference> newBringOut = new HashSet<>();
            for (Reference reference : bringOut) {
                newBringOut.add((Reference) reference.replace(from, to));
            }
            bringOut = newBringOut;
        }
    }

    @Override
    public void replaceDef(Reference from, Reference to) {

    }

    @Override
    public void calcDefAndUse() {
        if (bringOut != null)
            use.addAll(bringOut);
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        return "jmp " + dest;
    }
}
