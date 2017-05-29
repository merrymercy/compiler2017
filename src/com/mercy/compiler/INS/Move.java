package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;

/**
 * Created by mercy on 17-4-25.
 */
public class Move extends Instruction {
    Operand dest, src;
    public Move(Operand dest, Operand src) {
        this.dest = dest;
        this.src = src;
    }

    public Operand dest() {
        return dest;
    }
    public Operand src() {
        return src;
    }

    public void setDest(Operand dest) {
        this.dest = dest;
    }
    public void setSrc(Operand src) {
        this.src = src;
    }

    public boolean isRefMove() {
        if(dest instanceof Reference && src instanceof Reference) {
            Reference.Type type1 = ((Reference) src).type();
            Reference.Type type2 = ((Reference) dest).type();
            return (type1 == Reference.Type.UNKNOWN || type1 == Reference.Type.REG)
                    && (type2 == Reference.Type.UNKNOWN || type2 == Reference.Type.REG);
        }
        return false;
    }

    @Override
    public void replaceUse(Reference from, Reference to) {
        src = src.replace(from, to);
        if (!(dest instanceof Reference))
            dest = dest.replace(from, to);
    }

    @Override
    public void replaceDef(Reference from, Reference to) {
        dest = dest.replace(from, to);
    }

    @Override
    public void replaceAll(Reference from, Reference to) {
        src = src.replace(from, to);
        dest = dest.replace(from, to);
    }


    @Override
    public void calcDefAndUse() {
        if (dest instanceof Reference) {
            def.addAll(dest.getAllRef());
            use.addAll(src.getAllRef());
        } else {
            use.addAll(dest.getAllRef());
            use.addAll(src.getAllRef());
        }
        allref.addAll(use);
        allref.addAll(def);
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        return "mov " + dest + ", " + src;
    }
}
