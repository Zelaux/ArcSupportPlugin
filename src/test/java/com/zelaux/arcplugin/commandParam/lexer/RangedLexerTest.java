package com.zelaux.arcplugin.commandParam.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.zelaux.arcplugin.commandParam.parsing.ArcCommandParamsLexer;
import com.zelaux.arcplugin.commandParam.psi.CPTypes;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.util.ArrayList;

import static com.zelaux.arcplugin.commandParam.lexer.LexerResultEntry.entry;
import static com.zelaux.arcplugin.commandParam.lexer.RangedLexerTestCase.test;


public class RangedLexerTest {
    static final RangedLexerTestCase[] cases = {
            test("[vvv] <ddd>\n[dadw w...]",
            entry(CPTypes.LEFT_BRACKET,"[",0,1),
            entry(CPTypes.IDENTIFIER,"vvv",1,4),
            entry(CPTypes.RIGHT_BRACKET,"]",4,5),
            entry(CPTypes.SPACE," ",5,6),
            entry(CPTypes.LEFT_ARROW,"<",6,7),
            entry(CPTypes.IDENTIFIER,"ddd",7,10),
            entry(CPTypes.RIGHT_ARROW,">",10,11),
            entry(TokenType.BAD_CHARACTER,"\n",11,12),
            entry(CPTypes.LEFT_BRACKET,"[",12,13),
            entry(CPTypes.IDENTIFIER,"dadw w",13,19),
            entry(CPTypes.DOTS,"...",19,22),
            entry(CPTypes.RIGHT_BRACKET,"]",22,23)
            ),
    };

    public RangedLexerTest() {
        super();
    }

    @org.junit.Test
    public void testLexer() {

        Lexer lexer = new ArcCommandParamsLexer();
        for (RangedLexerTestCase rangedTestCase : cases) {
            LexerResultEntry[] entries = rangedTestCase.entries;
            if(entries.length==0){
                ArrayList<LexerResultEntry> tmp=new ArrayList<>();
                lexer.start(rangedTestCase.data);
                while (lexer.getTokenType()!=null){
                    LexerResultEntry entry = lexerResultEntry(lexer);
                    if (entry.tokenStart >= rangedTestCase.start) {
                        if (entry.tokenEnd > rangedTestCase.end) break;
                        tmp.add(entry);
                    }
                    lexer.advance();
                }
                entries=tmp.toArray(entries);
            }
            lexer.start(rangedTestCase.data, rangedTestCase.start, rangedTestCase.end);
            System.out.println("begin");
            int i = 0;
            while (lexer.getTokenType() != null) {
                LexerResultEntry entry = lexerResultEntry(lexer);
                Assert.assertTrue("Unexpected token " + entry, i < entries.length);
                LexerResultEntry expectedEntry = entries[i];
                Assert.assertNotEquals("String lexer returns empty token(" + lexer.getTokenStart() + "," + lexer.getTokenEnd() + ")", "", entry.text);
                Assert.assertEquals(expectedEntry, entry);
                System.out.println("\t" + entry);
                i++;
                lexer.advance();
            }
            if (i < entries.length)
                Assert.fail("Missing tokens " + (entries.length - i - 1) + "(" + rangedTestCase.data + ")");
            System.out.println("end");
        }


    }

    @NotNull
    private static LexerResultEntry lexerResultEntry(Lexer lexer) {
        LexerResultEntry entry = new LexerResultEntry(
                lexer.getTokenType(),
                lexer.getTokenText(),
                lexer.getTokenStart(),
                lexer.getTokenEnd()
        );
        return entry;
    }

}