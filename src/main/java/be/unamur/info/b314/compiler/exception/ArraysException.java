package be.unamur.info.b314.compiler.exception;

/**
 * Exception non-vérifié appelée ArraysException, lorsqu'on retrouve une erreur dans un array
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 */
public class ArraysException extends RuntimeException {
    public ArraysException(String message) {
        super(message);
    }

    public ArraysException(){
        super();
    }
}
