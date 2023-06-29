package be.unamur.info.b314.compiler.main;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B314actionSyntaxeTest {

    private static final Logger LOG = LoggerFactory.getLogger(B314actionSyntaxeTest.class);

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
    public void testcomments_actionSyntaxe_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ok/actionSyntaxe.b314", testFolder.newFile(), true, "actionSyntaxe: OK");
    }



    /**
     * ========================
     *  TEST KO
     * ========================
     */
    //@Test
    //public void testcomments_at_least_one_instruction_in_default_ko() throws Exception {
    //    CompilerTestHelper.launchCompilation("/syntax/comments/ko/at_least_one_instruction_in_default.b314", testFolder.newFile(), false, "actionSyntaxe: KO");
    //}

    @Test
    public void testcomments_actionSyntaxe_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/actionSyntaxeKO.b314", testFolder.newFile(), false, "actionSyntaxe: KO");
    }



}