package be.unamur.info.b314.compiler.exception;

/**
 * Exception non-vérifié appelée StructAException, lorsqu'on retrouve une erreur dans le struct
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 */
public class StructException  extends RuntimeException {
    public StructException(String message) {
        super(message);
    }

    public StructException(){
        super();
    }
}

