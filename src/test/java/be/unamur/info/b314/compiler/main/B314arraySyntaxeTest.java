package be.unamur.info.b314.compiler.main;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B314arraySyntaxeTest {

    private static final Logger LOG = LoggerFactory.getLogger(B314arraySyntaxeTest.class);

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder(); // Create a temporary folder for outputs deleted after tests

    @Rule
    public TestRule watcher = new TestWatcher() { // Prints message on logger before each test
        @Override
        protected void starting(Description description) {
            LOG.info(String.format("Starting test: %s()...",
                    description.getMethodName()));
        }
    ;
    };

    /**
     * ========================
     *  TEST OK
     * ========================
     */
    @Test
    public void testvariables_arraySyntaxe_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ok/arraySyntaxe.b314", testFolder.newFile(), true, "Erreur : array");
    }

    /**
     * ========================
     *  TEST KO
     * ========================
     */
   @Test
   public void testvariables_arraySyntaxeko_ko() throws Exception{
       CompilerTestHelper.launchCompilation("/syntax/comments/ko/arraySyntaxeko.b314", testFolder.newFile(), false, "Erreur : array ko 1");
   }

    @Test
    public void testvariables_arraySyntaxeko2_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/arraySyntaxeko2.b314", testFolder.newFile(), false, "Erreur : array ko 1");
    }





}