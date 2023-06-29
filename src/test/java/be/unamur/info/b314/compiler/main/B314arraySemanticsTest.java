package be.unamur.info.b314.compiler.main;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B314arraySemanticsTest {

    private static final Logger LOG = LoggerFactory.getLogger(B314arraySemanticsTest.class);

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
    public void testvariables_arraySemantics_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ok/arraySemantics.b314", testFolder.newFile(), true, "Erreur : array");
    }

    /**
     * ========================
     *  TEST KO
     * ========================
     */
    @Test
    public void testvariables_arraySemantics1_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/array/arraySemantics1.b314", testFolder.newFile(), false, "Erreur : array ko 1");
    }

    @Test
    public void testvariables_arraySemantics2_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/array/arraySemantics2.b314", testFolder.newFile(), false, "Erreur : array ko 2");
    }

    @Test
    public void testvariables_arraySemantics3_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/array/arraySemantics3.b314", testFolder.newFile(), false, "Erreur : array ko 3");
    }

    @Test
    public void testvariables_arraySemantics4_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/array/arraySemantics4.b314", testFolder.newFile(), false, "Erreur : array ko 4");
    }

    @Test
    public void testvariables_arraySemantics5_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/array/arraySemantics5.b314", testFolder.newFile(), false, "Erreur : array ko 5");
    }

    @Test
    public void testvariables_arraySemantics6_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/array/arraySemantics6.b314", testFolder.newFile(), false, "Erreur : array ko 6");
    }



}