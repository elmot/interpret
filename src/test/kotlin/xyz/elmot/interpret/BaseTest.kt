package xyz.elmot.interpret

import kotlin.test.assertEquals
import kotlin.test.fail

open class BaseTest {
    protected fun doTest(input: String, expectedOutput: String) {
        val stringBuilder = StringBuilder()
        val errorInfos = Ator.runScript(input) { str: String -> stringBuilder.append(str) }
        if (!errorInfos.isEmpty()) {
            for (errorInfo in errorInfos) {
                System.err.println(errorInfo)
            }
            fail()
        }

        assertEquals(expectedOutput, stringBuilder.toString())
    }

    protected fun doTestException(input: String, firstMessage: String, line: Int, pos: Int, len: Int) {
        val errors = Ator.runScript(input) { _ -> }
        val error = errors[0]
        assertEquals(firstMessage, error.msg,"Message")
        assertEquals(line, error.line,"Line")
        assertEquals(pos, error.pos,"Pos")
        assertEquals(len, error.len,"Len")
    }
}