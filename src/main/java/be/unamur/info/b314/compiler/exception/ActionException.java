package be.unamur.info.b314.compiler.exception;

/**
 * Exception non-vérifié appelée ActionException, lorsqu'on retrouve une erreur dans les actions
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 */

public class ActionException extends RuntimeException {

    public ActionException(String message) {
        super(message);
    }

    public ActionException(){
        super();
    }
}




