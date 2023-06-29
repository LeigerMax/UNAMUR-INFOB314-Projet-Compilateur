package be.unamur.info.b314.compiler.main;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B314enumAndTypeDefSyntaxeTest {

    private static final Logger LOG = LoggerFactory.getLogger(B314enumAndTypeDefSyntaxeTest.class);

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
    public void testvariables_enumSyntaxe_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ok/enumSyntaxe.b314", testFolder.newFile(), true, "Erreur : enum");
    }

    @Test
    public void testvariables_typeDefSyntaxe_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ok/typeDefSyntaxe.b314", testFolder.newFile(), true, "Erreur : typeDef");
    }

    /**
     * ========================
     *  TEST KO
     * ========================
     */

    @Test
    public void testvariables_enumSyntaxe1_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/enumSyntaxeKO.b314", testFolder.newFile(), false, "Erreur : enum ko 1");
    }

    @Test
    public void testvariables_typeDefSyntaxe1_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/typeDefSyntaxeKO.b314", testFolder.newFile(), false, "Erreur : typeDef ko 1");
    }




}