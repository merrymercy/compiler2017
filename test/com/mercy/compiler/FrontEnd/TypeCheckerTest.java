package com.mercy.compiler.FrontEnd;

import com.mercy.compiler.AST.AST;
import com.mercy.compiler.Parser.MalicLexer;
import com.mercy.compiler.Parser.MalicParser;
import com.mercy.compiler.Type.Type;
import com.mercy.compiler.Utility.SemanticError;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

import static com.mercy.Main.compileFile;
import static com.mercy.Main.getLibrary;
import static junit.framework.TestCase.fail;

/**
 * Created by mercy on 17-3-26.
 */
@RunWith(Parameterized.class)
public class TypeCheckerTest {
    @Parameterized.Parameters
    public static Collection<Object[]> testcase() {
        Collection<Object[]> files = new ArrayList<>();
        for (File f : new File("testcase/semantic/pass/").listFiles()) {
            if (f.isFile() && f.getName().endsWith(".mx")) {
                files.add(new Object[] {"testcase/semantic/pass/" + f.getName(), true});
            }
        }
        for (File f : new File("testcase/semantic/error/").listFiles()) {
            if (f.isFile() && f.getName().endsWith(".mx")) {
                files.add(new Object[] {"testcase/semantic/error/" + f.getName(), false});
            }
        }

        return files;
    }

    private String filename;
    private boolean shouldPass;

    public TypeCheckerTest(String filename, boolean shouldPass) {
        this.filename = filename;
        this.shouldPass = shouldPass;
    }

    @Test
    public void testPass() throws Exception {
        System.out.println("# " + filename);
        System.out.flush();

        int status = compileFile(filename);

        if (!shouldPass && status == 0)
            fail("should not pass");
        if (shouldPass && status != 0)
            fail("should pass");
    }

}