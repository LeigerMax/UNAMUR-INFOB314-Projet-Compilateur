package be.unamur.info.b314.compiler.main;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B314structSemanticsTest {

    private static final Logger LOG = LoggerFactory.getLogger(B314structSemanticsTest.class);

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
    public void testvariables_structSemantics_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ok/structSemantics.b314", testFolder.newFile(), true, "Erreur : struct");
    }

    /**
     * ========================
     *  TEST KO
     * ========================
     */
    @Test
    public void testvariables_structSemantics1_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/struct/structSemantics1.b314", testFolder.newFile(), false, "Erreur : struct ko 1");
    }

    @Test
    public void testvariables_structSemantics2_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/struct/structSemantics2.b314", testFolder.newFile(), false, "Erreur : struct ko 2");
    }

    @Test
    public void testvariables_structSemantics3_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/struct/structSemantics3.b314", testFolder.newFile(), false, "Erreur : struct ko 3");
    }

    @Test
    public void testvariables_structSemantics4_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/struct/structSemantics4.b314", testFolder.newFile(), false, "Erreur : struct ko 4");
    }



}