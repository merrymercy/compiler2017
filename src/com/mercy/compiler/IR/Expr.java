package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.IRBuilder.CommonExprInfo;
import com.mercy.compiler.BackEnd.InstructionEmitter;
import com.mercy.compiler.INS.Operand.Operand;

/**
 * Created by mercy on 17-3-30.
 */
abstract public class Expr extends IR {
    @Override
    abstract public Operand accept(InstructionEmitter emitter);

    private CommonExprInfo commonExprInfo;

    public CommonExprInfo commonExprInfo() {
        return commonExprInfo;
    }

    public void setCommonExprInfo(CommonExprInfo commonExprInfo) {
        this.commonExprInfo = commonExprInfo;
    }
}
