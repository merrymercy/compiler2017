package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.Entity.Scope;
import com.mercy.compiler.INS.Operand.Operand;

import java.util.List;

/**
 * Created by mercy on 17-4-25.
 */
public class Add extends Bin {
    public Add(Operand left, Operand right) {
        super(left, right);
    }

    @Override
    public String name() {
        return "add";
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }
}
