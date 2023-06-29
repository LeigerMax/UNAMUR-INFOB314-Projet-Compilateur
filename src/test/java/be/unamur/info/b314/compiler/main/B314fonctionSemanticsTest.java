package be.unamur.info.b314.compiler.main;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B314fonctionSemanticsTest {

    private static final Logger LOG = LoggerFactory.getLogger(B314fonctionSemanticsTest.class);

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
    public void testvariables_fonctionSemantics_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ok/fonctionSemantics.b314", testFolder.newFile(), true, "Erreur : fonction");
    }

    /**
     * ========================
     *  TEST KO
     * ========================
     */
    @Test
    public void testvariables_fonctionSemantics1_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/fonction/fonctionSemantics1.b314", testFolder.newFile(), false, "Erreur : fonction ko 1");
    }

    @Test
    public void testvariables_fonctionSemantics2_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/fonction/fonctionSemantics2.b314", testFolder.newFile(), false, "Erreur : fonction ko 2");
    }

    @Test
    public void testvariables_fonctionSemantics3_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/fonction/fonctionSemantics3.b314", testFolder.newFile(), false, "Erreur : fonction ko 3");
    }

    @Test
    public void testvariables_fonctionSemantics4_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/fonction/fonctionSemantics4.b314", testFolder.newFile(), false, "Erreur : fonction ko 4");
    }

    @Test
    public void testvariables_fonctionSemantics5_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/fonction/fonctionSemantics5.b314", testFolder.newFile(), false, "Erreur : fonction ko 5");
    }



}