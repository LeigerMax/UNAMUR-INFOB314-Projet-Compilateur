package be.unamur.info.b314.compiler.exception;

/**
 * Exception non-vérifié appelée ExpressionException, lorsqu'on retrouve une erreur dans une expression
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 */
public class ExpressionException extends RuntimeException {
    public ExpressionException(String message) {
        super(message);
    }

    public ExpressionException(){
        super();
    }
}


