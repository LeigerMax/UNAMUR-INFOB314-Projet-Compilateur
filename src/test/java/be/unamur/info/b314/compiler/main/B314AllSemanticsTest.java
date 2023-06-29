package be.unamur.info.b314.compiler.main;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B314AllSemanticsTest {
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
     * TEST OK
     * ========================
     */

    @Test
    public void testvariables_mapFileSemantics_ok() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ok/mapFileSemantics.b314", testFolder.newFile(), true, "Erreur : Map init");
    }

    @Test
    public void testvariables_globalSemantics_ok() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ok/globalSemantics.b314", testFolder.newFile(), true, "Erreur : global");
    }

    @Test
    public void testvariables_importAndMainSemantics_ok() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ok/importAndMainSemantics.b314", testFolder.newFile(), true, "Erreur : import And Main");
    }


    @Test
    public void testvariables_id_variableSemantics_ok() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ok/variableSemantics.b314", testFolder.newFile(), true, "Erreur: variable");
    }


    @Test
    public void testvariables_actionSemantics_ok() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ok/actionSemantics.b314", testFolder.newFile(), true, "Erreur : action");
    }

    @Test
    public void testvariables_arraySemantics_ok() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ok/arraySemantics.b314", testFolder.newFile(), true, "Erreur : array");
    }

    @Test
    public void testvariables_enumSemantics_ok() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ok/enumSemantics.b314", testFolder.newFile(), true, "Erreur : enum");
    }

    @Test
    public void testvariables_typeDefSemantics_ok() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ok/typeDefSemantics.b314", testFolder.newFile(), true, "Erreur : typeDef");
    }

    @Test
    public void testvariables_expressionSemantics_ok() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ok/expressionSemantics.b314", testFolder.newFile(), true, "Erreur : expression");
    }



    /**
     * ========================
     * TEST KO
     * ========================
     */

    @Test
    public void testvariables_globalSemantics1_ko() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/globalSemantics1.b314", testFolder.newFile(), false, "global ko 1");
    }


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
    public void testvariables_enumSemantics1_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/enum/enumSemantics1.b314", testFolder.newFile(), false, "Erreur : enum ko 1");
    }

    @Test
    public void testvariables_expressionSemantics1_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/expression/expressionSemantics1.b314", testFolder.newFile(), false, "Erreur : expression ko 1");
    }

    @Test
    public void testvariables_expressionSemantics2_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/expression/expressionSemantics2.b314", testFolder.newFile(), false, "Erreur : expression ko 2");
    }

    @Test
    public void testvariables_expressionSemantics3_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/expression/expressionSemantics3.b314", testFolder.newFile(), false, "Erreur : expression ko 3");
    }

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
    public void testvariables_id_variableSemantics1_ko() throws Exception {
        CompilerTestHelper.launchCompilation("/semantics/variables/ko/variable/variableSemantics1.b314", testFolder.newFile(), false, "Erreur: variable ko 1");
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



}