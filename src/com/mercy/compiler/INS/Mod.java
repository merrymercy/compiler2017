package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.INS.Operand.Operand;

import java.util.List;

/**
 * Created by mercy on 17-5-4.
 */
public class Mod extends Bin {
    public Mod(Operand left, Operand right) {
        super(left, right);
    }

    @Override
    public String name() {
        return "mod";
    }


    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }
}
