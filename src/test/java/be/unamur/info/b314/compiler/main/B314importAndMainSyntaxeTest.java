package be.unamur.info.b314.compiler.main;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B314importAndMainSyntaxeTest {

    private static final Logger LOG = LoggerFactory.getLogger(B314importAndMainSyntaxeTest.class);

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
    public void testvariables_importAndMainSyntaxe_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ok/importAndMainSyntaxe.b314", testFolder.newFile(), true, "Erreur : import And Main");
    }

    /**
     * ========================
     *  TEST KO
     * ========================
     */
    @Test
    public void testvariables_importAndMainSyntaxeKO_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/importAndMainSyntaxeKO.b314", testFolder.newFile(), false, "Erreur : import And Main ko 1");
    }

    @Test
    public void testvariables_comments_everywhere_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/comments_everywhereko.b314", testFolder.newFile(), false, "Erreur : import And Main ko 1");
    }







}