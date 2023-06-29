package be.unamur.info.b314.compiler.main;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B314importAndMainSemanticsTest {

    private static final Logger LOG = LoggerFactory.getLogger(B314importAndMainSemanticsTest.class);

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
    public void testvariables_importAndMainSemantics_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ok/importAndMainSemantics.b314", testFolder.newFile(), true, "Erreur : import And Main");
    }

    /**
     * ========================
     *  TEST KO
     * ========================
     */
    @Test
    public void testvariables_importAndMainSemantics1_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/importAndMain/importAndMainSemantics1.b314", testFolder.newFile(), false, "Erreur : import And Main ko 1");
    }

    @Test
    public void testvariables_importAndMainSemantics2_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/importAndMain/importAndMainSemantics2.b314", testFolder.newFile(), false, "Erreur : import And Main ko 2");
    }

    @Test
    public void testvariables_importAndMainSemantics3_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/importAndMain/importAndMainSemantics3.b314", testFolder.newFile(), false, "Erreur : import And Main ko 3");
    }

    @Test
    public void testvariables_importAndMainSemantics4_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/importAndMain/importAndMainSemantics4.b314", testFolder.newFile(), false, "Erreur : import And Main ko 4");
    }





}