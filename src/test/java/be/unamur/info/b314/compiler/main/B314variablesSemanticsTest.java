package be.unamur.info.b314.compiler.main;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B314variablesSemanticsTest {

    private static final Logger LOG = LoggerFactory.getLogger(B314variablesSemanticsTest.class);

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
    public void testvariables_id_variableSemantics_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ok/variableSemantics.b314", testFolder.newFile(), true, "Erreur: variable");
    }


    /**
     * ========================
     *  TEST KO
     * ========================
     */
    @Test
    public void testvariables_id_variableSemantics1_ko() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/variable/variableSemantics1.b314", testFolder.newFile(), false, "Erreur: variable ko 1");
    }

    @Test
    public void testvariables_id_variableSemantics2_ko() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/variable/variableSemantics2.b314", testFolder.newFile(), false, "Erreur: variable ko 2");
    }

    @Test
    public void testvariables_id_variableSemantics3_ko() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/variable/variableSemantics3.b314", testFolder.newFile(), false, "Erreur: variable ko 3");
    }

    @Test
    public void testvariables_id_variableSemantics4_ko() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/variable/variableSemantics4.b314", testFolder.newFile(), false, "Erreur: variable ko 4");
    }

    @Test
    public void testvariables_id_variableSemantics5_ko() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/variable/variableSemantics5.b314", testFolder.newFile(), false, "Erreur: variable ko 5");
    }

    @Test
    public void testvariables_id_variableSemantics6_ko() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/variable/variableSemantics6.b314", testFolder.newFile(), false, "Erreur: variable ko 6");
    }

    @Test
    public void testvariables_id_variableSemantics7_ko() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/variable/variableSemantics7.b314", testFolder.newFile(), false, "Erreur: variable ko 7");
    }

    @Test
    public void testvariables_id_variableSemantics8_ko() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/variable/variableSemantics8.b314", testFolder.newFile(), false, "Erreur: variable ko 8");
    }

    // Test avec fonction
    @Test
    public void testvariables_fonctionSemantics3_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/fonction/fonctionSemantics3.b314", testFolder.newFile(), false, "Erreur : fonction ko 3");
    }

    // Test avec fonction
    @Test
    public void testvariables_fonctionSemantics4_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/fonction/fonctionSemantics4.b314", testFolder.newFile(), false, "Erreur : fonction ko 4");
    }

}