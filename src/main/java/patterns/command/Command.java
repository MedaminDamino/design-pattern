package patterns.command;

/**
 * Command – Command Pattern interface (ISP).
 * Every undoable action implements this.
 */
public interface Command {
    void execute();
    void undo();
}
