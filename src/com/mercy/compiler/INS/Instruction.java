package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;

/**
 * Created by mercy on 17-4-25.
 */
abstract public class Instruction {
    abstract public void accept(Translator translator);
}
