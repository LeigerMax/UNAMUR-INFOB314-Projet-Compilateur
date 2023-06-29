package be.unamur.info.b314.compiler.main;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B314mapSemanticsTest {

    private static final Logger LOG = LoggerFactory.getLogger(B314mapSemanticsTest.class);

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
    public void testvariables_mapFileSemantics_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ok/mapFileSemantics.b314", testFolder.newFile(), true, "Erreur : Map init");
    }

    /**
     * ========================
     *  TEST KO
     * ========================
     */
    @Test
    public void testvariables_mapFileSemantics1_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/map/mapFileSemantics1.b314", testFolder.newFile(), false, "Map ko 1");
    }
    @Test
    public void testvariables_mapFileSemantics2_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/map/mapFileSemantics2.b314", testFolder.newFile(), false, "Map ko 2");
    }
    @Test
    public void testvariables_mapFileSemantics3_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/map/mapFileSemantics3.b314", testFolder.newFile(), false, "Map ko 3");
    }
    @Test
    public void testvariables_mapFileSemantics4_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/map/mapFileSemantics4.b314", testFolder.newFile(), false, "Map ko 4");
    }
    @Test
    public void testvariables_mapFileSemantics5_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/map/mapFileSemantics5.b314", testFolder.newFile(), false, "Map ko 5");
    }


}