package be.unamur.info.b314.compiler.exception;

/**
 * Exception non-vérifié appelée ArgException, lorsqu'on retrouve une erreur dans le parametre d'une fonction
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 */
public class ArgException extends RuntimeException {
    public ArgException(String message) {
        super(message);
    }

    public ArgException(){
        super();
    }

}
