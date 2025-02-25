package transact.model.entry.exceptions;

/**
 * Signals that the operation will result in duplicate entries (Persons,
 * transactions are considered duplicates if they have the same identity).
 */
public class DuplicateEntryException extends RuntimeException {
    public DuplicateEntryException() {
        super("Operation would result in duplicate entries");
    }
}
