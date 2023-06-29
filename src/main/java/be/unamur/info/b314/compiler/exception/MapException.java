package be.unamur.info.b314.compiler.exception;

/**
 * Exception non-vérifié appelée MapException, lorsqu'on retrouve une erreur dans la map
 *
 * @author Maxime Allemeersch - maxime.allemeersch@student.unamur.be
 */
public class MapException extends RuntimeException {
    public MapException(String message) {
        super(message);
    }

    public MapException(){
        super();
    }
}
