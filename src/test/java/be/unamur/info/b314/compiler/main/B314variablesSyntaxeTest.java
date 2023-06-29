package be.unamur.info.b314.compiler.main;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B314variablesSyntaxeTest {

    private static final Logger LOG = LoggerFactory.getLogger(B314variablesSyntaxeTest.class);

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
    public void testvariables_id_variablesSyntaxe_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ok/variablesSyntaxe.b314", testFolder.newFile(), true, "Erreur: variable");
    }


    /**
     * ========================
     *  TEST KO
     * ========================
     */
    @Test
    public void testvariables_id_variableSyntaxeKO_ko() throws Exception {
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/variableSyntaxeKO.b314", testFolder.newFile(), false, "Erreur: variable ko 1");
    }



}