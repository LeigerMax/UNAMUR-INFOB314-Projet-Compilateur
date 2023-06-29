package be.unamur.info.b314.compiler.exception;

/**
 * Exception non-vérifié appelée RuntimeException, lorsqu'on retrouve une erreur dans le nom de la variable
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 */
public class VariableNameException  extends RuntimeException {
    public VariableNameException(String message) {
        super(message);
    }

    public VariableNameException(){
        super();
    }
}

