package com.zelaux.arcplugin.commandParam.lexer;

class RangedLexerTestCase {
    public final String data;
    public final int start;
    public final int end;
    public final LexerResultEntry[] entries;

    RangedLexerTestCase(String data, int start, int end, LexerResultEntry... entries) {
        this.data = data;
        this.start = start;
        this.end = end;
        this.entries = entries;
    }

    static RangedLexerTestCase test(String data, int start, int end, LexerResultEntry... entries) {
        return new RangedLexerTestCase(data, start, end, entries);
    }
    static RangedLexerTestCase test(String data, LexerResultEntry... entries) {
        return new RangedLexerTestCase(data, 0, data.length(), entries);
    }
}
