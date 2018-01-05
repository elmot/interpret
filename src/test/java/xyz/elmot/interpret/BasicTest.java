package xyz.elmot.interpret;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.Test;

import java.io.StringReader;

public class BasicTest {
    @Test
    public void basicTest() {
        String text = "var a = 1 + 1\nprint \"a=\"\nout a";
        CodePointCharStream stream = CharStreams.fromString(text);
        AtorLexer lexer = new AtorLexer(stream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        AtorParser parser = new AtorParser(tokenStream);
        AtorParser.ProgramContext program = parser.program();
        System.out.println("program = " + program);
    }

}
