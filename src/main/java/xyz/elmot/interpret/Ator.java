package xyz.elmot.interpret;

import org.antlr.v4.runtime.*;
import xyz.elmot.interpret.eval.ErrorInfo;
import xyz.elmot.interpret.eval.EvalException;
import xyz.elmot.interpret.eval.ProgVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Ator {
    public static List<ErrorInfo> runScript(String input, Consumer<String> stringConsumer) {
        ArrayList<ErrorInfo> errors = new ArrayList<>();
        CodePointCharStream stream = CharStreams.fromString(input);
        AtorLexer lexer = new AtorLexer(stream);
        lexer.removeErrorListeners();
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        AtorParser parser = new AtorParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                errors.add(new ErrorInfo(msg, line, charPositionInLine, 1));
            }
        });
        AtorParser.ProgramContext program = parser.program();
        if (parser.getNumberOfSyntaxErrors() == 0) {
            AtorBaseVisitor<Void> visitor = new ProgVisitor(stringConsumer);
            try {
                program.accept(visitor);
            } catch (EvalException e) {
                Token start = e.getContext().getStart();
                Token stop = e.getContext().getStop();
                int line = start.getLine();
                int pos = start.getCharPositionInLine();
                int len = 0;
                if (stop.getLine() == line) {
                    len = stop.getCharPositionInLine() - pos;
                    if (stop.getText() != null) {
                        len += stop.getText().length();
                    }
                }
                if (len <= 0 && start.getText() != null) {
                    len = start.getText().length();
                }
                errors.add(new ErrorInfo(e.getMessage(), line, pos, len));
            }
        }
        return errors;
    }

}
