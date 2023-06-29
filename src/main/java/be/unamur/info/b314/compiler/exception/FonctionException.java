package be.unamur.info.b314.compiler.exception;

/**
 * Exception non-vérifié appelée FonctionException, lorsqu'on retrouve une erreur dans une fonction
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 */
public class FonctionException extends RuntimeException {
    public FonctionException(String message) {
        super(message);
    }

    public FonctionException(){
        super();
    }
}

