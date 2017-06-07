package com.mercy.compiler.FrontEnd;

import com.mercy.Option;
import com.mercy.compiler.Utility.SemanticError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

import static com.mercy.Main.compile;
import static java.lang.System.exit;
import static junit.framework.TestCase.fail;

/**
 * Created by mercy on 17-3-26.
 */
@RunWith(Parameterized.class)
public class SemanticTest {
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

    public SemanticTest(String filename, boolean shouldPass) {
        this.filename = filename;
        this.shouldPass = shouldPass;
    }

    @Test
    public void testPass() throws Exception {
        System.out.println("# " + filename);
        System.out.flush();

        InputStream is = new FileInputStream(filename);
        PrintStream os = new PrintStream(new FileOutputStream(Option.outFile));

        try {
            compile(is, os);
            if (!shouldPass)
                fail("should not pass");
        }  catch (SemanticError error) {
            if (shouldPass)
                fail("should pass");
        } catch (InternalError error) {
            System.err.println(error.getMessage());
            exit(1);
        }
    }

}