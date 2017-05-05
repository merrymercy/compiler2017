package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;

import java.io.PrintStream;
import java.util.List;

/**
 * Created by mercy on 17-4-25.
 */
abstract public class Instruction {
    abstract public void accept(Translator translator);
}
