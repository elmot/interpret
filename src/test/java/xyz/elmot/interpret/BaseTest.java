package xyz.elmot.interpret;

import xyz.elmot.interpret.eval.ErrorInfo;

import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SuppressWarnings("WeakerAccess")
public class BaseTest {
    protected void doTest(String input, String expectedOutput) {
        StringBuilder stringBuilder = new StringBuilder();
        List<ErrorInfo> errorInfos = Ator.runScript(input, stringBuilder::append);
        if(!errorInfos.isEmpty()) {
            for (ErrorInfo errorInfo : errorInfos) {
                System.err.println(errorInfo);
            }
            fail();
        }

        assertEquals(expectedOutput, stringBuilder.toString());
    }

    protected void doTestException(String input, String firstMessage, int line, int pos, int len) {
        List<ErrorInfo> errors = Ator.runScript(input, s -> {
        });
        ErrorInfo errorInfo = errors.get(0);
        assertEquals("Message",firstMessage, errorInfo.getMsg());
        assertEquals("Line",line, errorInfo.getLine());
        assertEquals("Pos",pos, errorInfo.getPos());
        assertEquals("Len",len, errorInfo.getLen());
    }

}
