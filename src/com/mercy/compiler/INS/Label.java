package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.BasicBlock;
import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Reference;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mercy on 17-4-26.
 */
public class Label extends Instruction {
    String name;
    BasicBlock basicBlock;
    Set<Reference> bringIn;

    public Label(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public Set<Reference> bringIn() {
        return bringIn;
    }

    public void setBringIn(Set<Reference> bringIn) {
        this.bringIn = bringIn;
    }

    public BasicBlock basicBlock() {
        return basicBlock;
    }

    public void setBasicBlock(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    @Override
    public void replaceUse(Reference from, Reference to) {
    }

    @Override
    public void replaceDef(Reference from, Reference to) {
        if (bringIn != null) {
            Set<Reference> newBringin = new HashSet<>();
            for (Reference ref : bringIn) {
                newBringin.add((Reference) ref.replace(from, to));
            }
            bringIn = newBringin;
        }
    }

    @Override
    public void replaceAll(Reference from, Reference to) {
        replaceDef(from, to);
    }

    @Override
    public void calcDefAndUse() {
        if (bringIn != null)
            def.addAll(bringIn);
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        return name;
    }
}
