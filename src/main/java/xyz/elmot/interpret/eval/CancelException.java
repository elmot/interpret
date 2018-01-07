package xyz.elmot.interpret.eval;

/**
 * Service runtime exception to silently break long running scripts
 */
@SuppressWarnings("WeakerAccess")
public class CancelException extends RuntimeException{
    public CancelException() {
    }
}
