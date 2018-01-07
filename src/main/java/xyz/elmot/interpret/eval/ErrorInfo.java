package xyz.elmot.interpret.eval;

public class ErrorInfo {
    private final String msg;
    private final int line;
    private final int pos;
    private final int len;

    public ErrorInfo(String msg, int line, int pos, int len) {
        this.msg = msg;
        this.line = line;
        this.pos = pos;
        this.len = len;
    }

    public String getMsg() {
        return msg;
    }

    public int getLine() {
        return line;
    }

    public int getPos() {
        return pos;
    }

    public int getLen() {
        return len;
    }
}
