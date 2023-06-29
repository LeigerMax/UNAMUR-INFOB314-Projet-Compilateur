package be.unamur.info.b314.compiler.main;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B314AllSyntaxeTest {
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
     public void testvariables_actionSyntaxe_ok() throws Exception{
         CompilerTestHelper.launchCompilation("/syntax/comments/ok/actionSyntaxe.b314", testFolder.newFile(), true, "Erreur : Action");
     }
     @Test
     public void testvariables_arraySyntaxe_ok() throws Exception{
         CompilerTestHelper.launchCompilation("/syntax/comments/ok/arraySyntaxe.b314", testFolder.newFile(), true, "Erreur : Array ");
     }

    @Test
    public void testvariables_enumSyntaxe_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ok/enumSyntaxe.b314", testFolder.newFile(), true, "Erreur : enum");
    }

    @Test
    public void testvariables_typeDefSyntaxe_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ok/typeDefSyntaxe.b314", testFolder.newFile(), true, "Erreur : typeDef");
    }

    @Test
    public void testvariables_expressionSyntaxe_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ok/expressionSyntaxe.b314", testFolder.newFile(), true, "Erreur : expression");
    }

    @Test
    public void testvariables_fonctionSyntaxe_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ok/fonctionSyntaxe.b314", testFolder.newFile(), true, "Erreur : fonction");
    }

    @Test
    public void testvariables_importAndMainSyntaxe_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ok/importAndMainSyntaxe.b314", testFolder.newFile(), true, "Erreur : import And Main");
    }

    @Test
    public void testvariables_structSyntaxe_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ok/structSyntaxe.b314", testFolder.newFile(), true, "Erreur : struct");
    }

    @Test
    public void testvariables_id_variablesSyntaxe_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ok/variablesSyntaxe.b314", testFolder.newFile(), true, "Erreur: variable");
    }






    /**
     * ========================
     *  TEST KO
     * ========================
     */

    @Test
    public void testcomments_actionSyntaxe_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/actionSyntaxeKO.b314", testFolder.newFile(), false, "actionSyntaxe: KO");
    }
    @Test
    public void testvariables_arraySyntaxeko_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/arraySyntaxeko.b314", testFolder.newFile(), false, "Erreur : array ko 1");
    }

    @Test
    public void testvariables_arraySyntaxeko2_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/arraySyntaxeko2.b314", testFolder.newFile(), false, "Erreur : array ko 1");
    }
    @Test
    public void testvariables_enumSyntaxe1_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/enumSyntaxeKO.b314", testFolder.newFile(), false, "Erreur : enum ko 1");
    }

    @Test
    public void testvariables_typeDefSyntaxe1_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/typeDefSyntaxeKO.b314", testFolder.newFile(), false, "Erreur : typeDef ko 1");
    }
    @Test
    public void testvariables_fonctionSyntaxe1_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/fonctionSyntaxeKO.b314", testFolder.newFile(), false, "Erreur : fonction ko 1");
    }
    @Test
    public void testvariables_importAndMainSyntaxeKO_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/importAndMainSyntaxeKO.b314", testFolder.newFile(), false, "Erreur : import And Main ko 1");
    }

    @Test
    public void testvariables_comments_everywhere_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/comments_everywhereko.b314", testFolder.newFile(), false, "Erreur : import And Main ko 1");
    }

    @Test
    public void testvariables_structSyntaxeKO_ko() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/structSyntaxeKO.b314", testFolder.newFile(), false, "Erreur : struct ko 1");
    }

    @Test
    public void testvariables_id_variableSyntaxeKO_ko() throws Exception {
        CompilerTestHelper.launchCompilation("/syntax/comments/ko/variablesSyntaxeKO.b314", testFolder.newFile(), false, "Erreur: variable ko 1");
    }



}