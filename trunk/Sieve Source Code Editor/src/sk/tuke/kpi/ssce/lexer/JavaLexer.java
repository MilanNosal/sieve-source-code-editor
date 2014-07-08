/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package sk.tuke.kpi.ssce.lexer;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for java language. <br/> It recognizes "version" attribute
 * and expects
 * <code>java.lang.Integer</code> value for it. The default value is
 * Integer.valueOf(5). The lexer changes its behavior in the following way: <ul>
 * <li> Integer.valueOf(4) - "assert" recognized as keyword (not identifier)
 * <li> Integer.valueOf(5) - "enum" recognized as keyword (not identifier) </ul>
 *
 * @author Miloslav Metelka, Matej Nosal
 * @version 1.00
 */
//SsceIntent:Lexikalna analyza pomocneho dokumentu .sj;
public class JavaLexer implements Lexer<SieveJavaTokenId> {

    private static final int EOF = LexerInput.EOF;
    private final LexerInput input;
    private final TokenFactory<SieveJavaTokenId> tokenFactory;
    private final int version;

    public JavaLexer(LexerRestartInfo<SieveJavaTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null); // never set to non-null value in state()

        Integer ver = (Integer) info.getAttributeValue("version");
        this.version = (ver != null) ? ver.intValue() : 7; // TODO: Java 1.7 used by default
    }

    public Object state() {
        return null; // always in default state after token recognition
    }

    public Token<SieveJavaTokenId> nextToken() {
        while (true) {
            int c = input.read();
            SieveJavaTokenId lookupId = null;
            switch (c) {
                case '#':
                    int backup = 1;
                    switch (c = input.read()) {
                        case 'f':
                            backup++;
                            if ((c = input.read()) == 'i') {
                                backup++;
                                if ((c = input.read()) == 'l') {
                                    backup++;
                                    if ((c = input.read()) == 'e') {
                                        backup++;
                                        if ((c = input.read()) == EOF || !Character.isJavaIdentifierPart(c = translateSurrogates(c))) {
                                            // For surrogate 2 chars must be backed up
                                            input.backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);
                                            return token(SieveJavaTokenId.FILE);
                                        }
                                    }
                                }
                            }
                            break;
                        case 'c':
                            backup++;
                            if ((c = input.read()) == 'o') {
                                backup++;
                                if ((c = input.read()) == 'd') {
                                    backup++;
                                    if ((c = input.read()) == 'e') {
                                        backup++;
                                        if ((c = input.read()) == EOF || !Character.isJavaIdentifierPart(c = translateSurrogates(c))) {
                                            // For surrogate 2 chars must be backed up
                                            input.backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);
                                            return token(SieveJavaTokenId.CODE);
                                        }
                                    }
                                }
                            }
                            break;
                    }
                    input.backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2*backup : backup);



                    //Support for exotic identifiers has been removed 6999438
                    if (true || this.version < 7 || input.read() != '"') {
                        return token(SieveJavaTokenId.ERROR);
                    }
                    lookupId = SieveJavaTokenId.IDENTIFIER;

                case '"': // string literal
                    if (lookupId == null) {
                        lookupId = SieveJavaTokenId.STRING_LITERAL;
                    }
                    while (true) {
                        switch (input.read()) {
                            case '"': // NOI18N
                                return token(lookupId);
                            case '\\':
                                input.read();
                                break;
                            case '\r':
                                input.consumeNewline();
                            case '\n':
                            case EOF:
                                return tokenFactory.createToken(lookupId, //XXX: \n handling for exotic identifiers?
                                        input.readLength(), PartType.START);
                        }
                    }

                case '\'': // char literal
                    while (true) {
                        switch (input.read()) {
                            case '\'': // NOI18N
                                return token(SieveJavaTokenId.CHAR_LITERAL);
                            case '\\':
                                input.read(); // read escaped char
                                break;
                            case '\r':
                                input.consumeNewline();
                            case '\n':
                            case EOF:
                                return tokenFactory.createToken(SieveJavaTokenId.CHAR_LITERAL,
                                        input.readLength(), PartType.START);
                        }
                    }

                case '/':
                    switch (input.read()) {
                        case '/': // in single-line comment
                            while (true) {
                                switch (input.read()) {
                                    case '\r':
                                        input.consumeNewline();
                                    case '\n':
                                    case EOF:
                                        return token(SieveJavaTokenId.LINE_COMMENT);
                                }
                            }
                        case '=': // found /=
                            return token(SieveJavaTokenId.SLASHEQ);
                        case '*': // in multi-line or javadoc comment
                            c = input.read();
                            if (c == '*') { // either javadoc comment or empty multi-line comment /**/
                                c = input.read();
                                if (c == '/') {
                                    return token(SieveJavaTokenId.BLOCK_COMMENT);
                                }
                                while (true) { // in javadoc comment
                                    while (c == '*') {
                                        c = input.read();
                                        if (c == '/') {
                                            return token(SieveJavaTokenId.JAVADOC_COMMENT);
                                        } else if (c == EOF) {
                                            return tokenFactory.createToken(SieveJavaTokenId.JAVADOC_COMMENT,
                                                    input.readLength(), PartType.START);
                                        }
                                    }
                                    if (c == EOF) {
                                        return tokenFactory.createToken(SieveJavaTokenId.JAVADOC_COMMENT,
                                                input.readLength(), PartType.START);
                                    }
                                    c = input.read();
                                }

                            } else { // in multi-line comment (and not after '*')
                                while (true) {
                                    c = input.read();
                                    while (c == '*') {
                                        c = input.read();
                                        if (c == '/') {
                                            return token(SieveJavaTokenId.BLOCK_COMMENT);
                                        } else if (c == EOF) {
                                            return tokenFactory.createToken(SieveJavaTokenId.BLOCK_COMMENT,
                                                    input.readLength(), PartType.START);
                                        }
                                    }
                                    if (c == EOF) {
                                        return tokenFactory.createToken(SieveJavaTokenId.BLOCK_COMMENT,
                                                input.readLength(), PartType.START);
                                    }
                                }
                            }
                    } // end of switch()
                    input.backup(1);
                    return token(SieveJavaTokenId.SLASH);

                case '=':
                    if (input.read() == '=') {
                        return token(SieveJavaTokenId.EQEQ);
                    }
                    input.backup(1);
                    return token(SieveJavaTokenId.EQ);

                case '>':
                    switch (input.read()) {
                        case '>': // after >>
                            switch (c = input.read()) {
                                case '>': // after >>>
                                    if (input.read() == '=') {
                                        return token(SieveJavaTokenId.GTGTGTEQ);
                                    }
                                    input.backup(1);
                                    return token(SieveJavaTokenId.GTGTGT);
                                case '=': // >>=
                                    return token(SieveJavaTokenId.GTGTEQ);
                            }
                            input.backup(1);
                            return token(SieveJavaTokenId.GTGT);
                        case '=': // >=
                            return token(SieveJavaTokenId.GTEQ);
                    }
                    input.backup(1);
                    return token(SieveJavaTokenId.GT);

                case '<':
                    switch (input.read()) {
                        case '<': // after <<
                            if (input.read() == '=') {
                                return token(SieveJavaTokenId.LTLTEQ);
                            }
                            input.backup(1);
                            return token(SieveJavaTokenId.LTLT);
                        case '=': // <=
                            return token(SieveJavaTokenId.LTEQ);
                    }
                    input.backup(1);
                    return token(SieveJavaTokenId.LT);

                case '+':
                    switch (input.read()) {
                        case '+':
                            return token(SieveJavaTokenId.PLUSPLUS);
                        case '=':
                            return token(SieveJavaTokenId.PLUSEQ);
                    }
                    input.backup(1);
                    return token(SieveJavaTokenId.PLUS);

                case '-':
                    switch (input.read()) {
                        case '-':
                            return token(SieveJavaTokenId.MINUSMINUS);
                        case '=':
                            return token(SieveJavaTokenId.MINUSEQ);
                    }
                    input.backup(1);
                    return token(SieveJavaTokenId.MINUS);

                case '*':
                    switch (input.read()) {
                        case '/': // invalid comment end - */
                            return token(SieveJavaTokenId.INVALID_COMMENT_END);
                        case '=':
                            return token(SieveJavaTokenId.STAREQ);
                    }
                    input.backup(1);
                    return token(SieveJavaTokenId.STAR);

                case '|':
                    switch (input.read()) {
                        case '|':
                            return token(SieveJavaTokenId.BARBAR);
                        case '=':
                            return token(SieveJavaTokenId.BAREQ);
                    }
                    input.backup(1);
                    return token(SieveJavaTokenId.BAR);

                case '&':
                    switch (input.read()) {
                        case '&':
                            return token(SieveJavaTokenId.AMPAMP);
                        case '=':
                            return token(SieveJavaTokenId.AMPEQ);
                    }
                    input.backup(1);
                    return token(SieveJavaTokenId.AMP);

                case '%':
                    if (input.read() == '=') {
                        return token(SieveJavaTokenId.PERCENTEQ);
                    }
                    input.backup(1);
                    return token(SieveJavaTokenId.PERCENT);

                case '^':
                    if (input.read() == '=') {
                        return token(SieveJavaTokenId.CARETEQ);
                    }
                    input.backup(1);
                    return token(SieveJavaTokenId.CARET);

                case '!':
                    if (input.read() == '=') {
                        return token(SieveJavaTokenId.BANGEQ);
                    }
                    input.backup(1);
                    return token(SieveJavaTokenId.BANG);

                case '.':
                    if ((c = input.read()) == '.') {
                        if (input.read() == '.') { // ellipsis ...
                            return token(SieveJavaTokenId.ELLIPSIS);
                        } else {
                            input.backup(2);
                        }
                    } else if ('0' <= c && c <= '9') { // float literal
                        return finishNumberLiteral(input.read(), true);
                    } else {
                        input.backup(1);
                    }
                    return token(SieveJavaTokenId.DOT);

                case '~':
                    return token(SieveJavaTokenId.TILDE);
                case ',':
                    return token(SieveJavaTokenId.COMMA);
                case ';':
                    return token(SieveJavaTokenId.SEMICOLON);
                case ':':
                    return token(SieveJavaTokenId.COLON);
                case '?':
                    return token(SieveJavaTokenId.QUESTION);
                case '(':
                    return token(SieveJavaTokenId.LPAREN);
                case ')':
                    return token(SieveJavaTokenId.RPAREN);
                case '[':
                    return token(SieveJavaTokenId.LBRACKET);
                case ']':
                    return token(SieveJavaTokenId.RBRACKET);
                case '{':
                    return token(SieveJavaTokenId.LBRACE);
                case '}':
                    return token(SieveJavaTokenId.RBRACE);
                case '@':
                    return token(SieveJavaTokenId.AT);

                case '0': // in a number literal
                    c = input.read();
                    if (c == 'x' || c == 'X') { // in hexadecimal (possibly floating-point) literal
                        boolean inFraction = false;
                        while (true) {
                            switch (input.read()) {
                                case '0':
                                case '1':
                                case '2':
                                case '3':
                                case '4':
                                case '5':
                                case '6':
                                case '7':
                                case '8':
                                case '9':
                                case 'a':
                                case 'b':
                                case 'c':
                                case 'd':
                                case 'e':
                                case 'f':
                                case 'A':
                                case 'B':
                                case 'C':
                                case 'D':
                                case 'E':
                                case 'F':
                                    break;
                                case '.': // hex float literal
                                    if (!inFraction) {
                                        inFraction = true;
                                    } else { // two dots in the float literal
                                        return token(SieveJavaTokenId.FLOAT_LITERAL_INVALID);
                                    }
                                    break;
                                case 'p':
                                case 'P': // binary exponent
                                    return finishFloatExponent();
                                default:
                                    input.backup(1);
                                    // if float then before mandatory binary exponent => invalid
                                    return token(inFraction ? SieveJavaTokenId.FLOAT_LITERAL_INVALID
                                            : SieveJavaTokenId.INT_LITERAL);
                            }
                        } // end of while(true)
                    }
                    return finishNumberLiteral(c, false);

                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    return finishNumberLiteral(input.read(), false);


                // Keywords lexing    
                case 'a':
                    switch (c = input.read()) {
                        case 'b':
                            if ((c = input.read()) == 's'
                                    && (c = input.read()) == 't'
                                    && (c = input.read()) == 'r'
                                    && (c = input.read()) == 'a'
                                    && (c = input.read()) == 'c'
                                    && (c = input.read()) == 't') {
                                return keywordOrIdentifier(SieveJavaTokenId.ABSTRACT);
                            }
                            break;
                        case 's':
                            if ((c = input.read()) == 's'
                                    && (c = input.read()) == 'e'
                                    && (c = input.read()) == 'r'
                                    && (c = input.read()) == 't') {
                                return (version >= 4)
                                        ? keywordOrIdentifier(SieveJavaTokenId.ASSERT)
                                        : finishIdentifier();
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'b':
                    switch (c = input.read()) {
                        case 'o':
                            if ((c = input.read()) == 'o'
                                    && (c = input.read()) == 'l'
                                    && (c = input.read()) == 'e'
                                    && (c = input.read()) == 'a'
                                    && (c = input.read()) == 'n') {
                                return keywordOrIdentifier(SieveJavaTokenId.BOOLEAN);
                            }
                            break;
                        case 'r':
                            if ((c = input.read()) == 'e'
                                    && (c = input.read()) == 'a'
                                    && (c = input.read()) == 'k') {
                                return keywordOrIdentifier(SieveJavaTokenId.BREAK);
                            }
                            break;
                        case 'y':
                            if ((c = input.read()) == 't'
                                    && (c = input.read()) == 'e') {
                                return keywordOrIdentifier(SieveJavaTokenId.BYTE);
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'c':
                    switch (c = input.read()) {
                        case 'a':
                            switch (c = input.read()) {
                                case 's':
                                    if ((c = input.read()) == 'e') {
                                        return keywordOrIdentifier(SieveJavaTokenId.CASE);
                                    }
                                    break;
                                case 't':
                                    if ((c = input.read()) == 'c'
                                            && (c = input.read()) == 'h') {
                                        return keywordOrIdentifier(SieveJavaTokenId.CATCH);
                                    }
                                    break;
                            }
                            break;
                        case 'h':
                            if ((c = input.read()) == 'a'
                                    && (c = input.read()) == 'r') {
                                return keywordOrIdentifier(SieveJavaTokenId.CHAR);
                            }
                            break;
                        case 'l':
                            if ((c = input.read()) == 'a'
                                    && (c = input.read()) == 's'
                                    && (c = input.read()) == 's') {
                                return keywordOrIdentifier(SieveJavaTokenId.CLASS);
                            }
                            break;
                        case 'o':
                            if ((c = input.read()) == 'n') {
                                switch (c = input.read()) {
                                    case 's':
                                        if ((c = input.read()) == 't') {
                                            return keywordOrIdentifier(SieveJavaTokenId.CONST);
                                        }
                                        break;
                                    case 't':
                                        if ((c = input.read()) == 'i'
                                                && (c = input.read()) == 'n'
                                                && (c = input.read()) == 'u'
                                                && (c = input.read()) == 'e') {
                                            return keywordOrIdentifier(SieveJavaTokenId.CONTINUE);
                                        }
                                        break;
                                }
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'd':
                    switch (c = input.read()) {
                        case 'e':
                            if ((c = input.read()) == 'f'
                                    && (c = input.read()) == 'a'
                                    && (c = input.read()) == 'u'
                                    && (c = input.read()) == 'l'
                                    && (c = input.read()) == 't') {
                                return keywordOrIdentifier(SieveJavaTokenId.DEFAULT);
                            }
                            break;
                        case 'o':
                            switch (c = input.read()) {
                                case 'u':
                                    if ((c = input.read()) == 'b'
                                            && (c = input.read()) == 'l'
                                            && (c = input.read()) == 'e') {
                                        return keywordOrIdentifier(SieveJavaTokenId.DOUBLE);
                                    }
                                    break;
                                default:
                                    return keywordOrIdentifier(SieveJavaTokenId.DO, c);
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'e':
                    switch (c = input.read()) {
                        case 'l':
                            if ((c = input.read()) == 's'
                                    && (c = input.read()) == 'e') {
                                return keywordOrIdentifier(SieveJavaTokenId.ELSE);
                            }
                            break;
                        case 'n':
                            if ((c = input.read()) == 'u'
                                    && (c = input.read()) == 'm') {
                                return (version >= 5)
                                        ? keywordOrIdentifier(SieveJavaTokenId.ENUM)
                                        : finishIdentifier();
                            }
                            break;
                        case 'x':
                            if ((c = input.read()) == 't'
                                    && (c = input.read()) == 'e'
                                    && (c = input.read()) == 'n'
                                    && (c = input.read()) == 'd'
                                    && (c = input.read()) == 's') {
                                return keywordOrIdentifier(SieveJavaTokenId.EXTENDS);
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'f':
                    switch (c = input.read()) {
                        case 'a':
                            if ((c = input.read()) == 'l'
                                    && (c = input.read()) == 's'
                                    && (c = input.read()) == 'e') {
                                return keywordOrIdentifier(SieveJavaTokenId.FALSE);
                            }
                            break;
                        case 'i':
                            if ((c = input.read()) == 'n'
                                    && (c = input.read()) == 'a'
                                    && (c = input.read()) == 'l') {
                                switch (c = input.read()) {
                                    case 'l':
                                        if ((c = input.read()) == 'y') {
                                            return keywordOrIdentifier(SieveJavaTokenId.FINALLY);
                                        }
                                        break;
                                    default:
                                        return keywordOrIdentifier(SieveJavaTokenId.FINAL, c);
                                }
                            }
                            break;
                        case 'l':
                            if ((c = input.read()) == 'o'
                                    && (c = input.read()) == 'a'
                                    && (c = input.read()) == 't') {
                                return keywordOrIdentifier(SieveJavaTokenId.FLOAT);
                            }
                            break;
                        case 'o':
                            if ((c = input.read()) == 'r') {
                                return keywordOrIdentifier(SieveJavaTokenId.FOR);
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'g':
                    if ((c = input.read()) == 'o'
                            && (c = input.read()) == 't'
                            && (c = input.read()) == 'o') {
                        return keywordOrIdentifier(SieveJavaTokenId.GOTO);
                    }
                    return finishIdentifier(c);

                case 'i':
                    switch (c = input.read()) {
                        case 'f':
                            return keywordOrIdentifier(SieveJavaTokenId.IF);
                        case 'm':
                            if ((c = input.read()) == 'p') {
                                switch (c = input.read()) {
                                    case 'l':
                                        if ((c = input.read()) == 'e'
                                                && (c = input.read()) == 'm'
                                                && (c = input.read()) == 'e'
                                                && (c = input.read()) == 'n'
                                                && (c = input.read()) == 't'
                                                && (c = input.read()) == 's') {
                                            return keywordOrIdentifier(SieveJavaTokenId.IMPLEMENTS);
                                        }
                                        break;
                                    case 'o':
                                        if ((c = input.read()) == 'r'
                                                && (c = input.read()) == 't') {
                                            return keywordOrIdentifier(SieveJavaTokenId.IMPORT);
                                        }
                                        break;
                                }
                            }
                            break;
                        case 'n':
                            switch (c = input.read()) {
                                case 's':
                                    if ((c = input.read()) == 't'
                                            && (c = input.read()) == 'a'
                                            && (c = input.read()) == 'n'
                                            && (c = input.read()) == 'c'
                                            && (c = input.read()) == 'e'
                                            && (c = input.read()) == 'o'
                                            && (c = input.read()) == 'f') {
                                        return keywordOrIdentifier(SieveJavaTokenId.INSTANCEOF);
                                    }
                                    break;
                                case 't':
                                    switch (c = input.read()) {
                                        case 'e':
                                            if ((c = input.read()) == 'r'
                                                    && (c = input.read()) == 'f'
                                                    && (c = input.read()) == 'a'
                                                    && (c = input.read()) == 'c'
                                                    && (c = input.read()) == 'e') {
                                                return keywordOrIdentifier(SieveJavaTokenId.INTERFACE);
                                            }
                                            break;
                                        default:
                                            return keywordOrIdentifier(SieveJavaTokenId.INT, c);
                                    }
                                    break;
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'l':
                    if ((c = input.read()) == 'o'
                            && (c = input.read()) == 'n'
                            && (c = input.read()) == 'g') {
                        return keywordOrIdentifier(SieveJavaTokenId.LONG);
                    }
                    return finishIdentifier(c);

                case 'n':
                    switch (c = input.read()) {
                        case 'a':
                            if ((c = input.read()) == 't'
                                    && (c = input.read()) == 'i'
                                    && (c = input.read()) == 'v'
                                    && (c = input.read()) == 'e') {
                                return keywordOrIdentifier(SieveJavaTokenId.NATIVE);
                            }
                            break;
                        case 'e':
                            if ((c = input.read()) == 'w') {
                                return keywordOrIdentifier(SieveJavaTokenId.NEW);
                            }
                            break;
                        case 'u':
                            if ((c = input.read()) == 'l'
                                    && (c = input.read()) == 'l') {
                                return keywordOrIdentifier(SieveJavaTokenId.NULL);
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'p':
                    switch (c = input.read()) {
                        case 'a':
                            if ((c = input.read()) == 'c'
                                    && (c = input.read()) == 'k'
                                    && (c = input.read()) == 'a'
                                    && (c = input.read()) == 'g'
                                    && (c = input.read()) == 'e') {
                                return keywordOrIdentifier(SieveJavaTokenId.PACKAGE);
                            }
                            break;
                        case 'r':
                            switch (c = input.read()) {
                                case 'i':
                                    if ((c = input.read()) == 'v'
                                            && (c = input.read()) == 'a'
                                            && (c = input.read()) == 't'
                                            && (c = input.read()) == 'e') {
                                        return keywordOrIdentifier(SieveJavaTokenId.PRIVATE);
                                    }
                                    break;
                                case 'o':
                                    if ((c = input.read()) == 't'
                                            && (c = input.read()) == 'e'
                                            && (c = input.read()) == 'c'
                                            && (c = input.read()) == 't'
                                            && (c = input.read()) == 'e'
                                            && (c = input.read()) == 'd') {
                                        return keywordOrIdentifier(SieveJavaTokenId.PROTECTED);
                                    }
                                    break;
                            }
                            break;
                        case 'u':
                            if ((c = input.read()) == 'b'
                                    && (c = input.read()) == 'l'
                                    && (c = input.read()) == 'i'
                                    && (c = input.read()) == 'c') {
                                return keywordOrIdentifier(SieveJavaTokenId.PUBLIC);
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'r':
                    if ((c = input.read()) == 'e'
                            && (c = input.read()) == 't'
                            && (c = input.read()) == 'u'
                            && (c = input.read()) == 'r'
                            && (c = input.read()) == 'n') {
                        return keywordOrIdentifier(SieveJavaTokenId.RETURN);
                    }
                    return finishIdentifier(c);

                case 's':
                    switch (c = input.read()) {
                        case 'h':
                            if ((c = input.read()) == 'o'
                                    && (c = input.read()) == 'r'
                                    && (c = input.read()) == 't') {
                                return keywordOrIdentifier(SieveJavaTokenId.SHORT);
                            }
                            break;
                        case 't':
                            switch (c = input.read()) {
                                case 'a':
                                    if ((c = input.read()) == 't'
                                            && (c = input.read()) == 'i'
                                            && (c = input.read()) == 'c') {
                                        return keywordOrIdentifier(SieveJavaTokenId.STATIC);
                                    }
                                    break;
                                case 'r':
                                    if ((c = input.read()) == 'i'
                                            && (c = input.read()) == 'c'
                                            && (c = input.read()) == 't'
                                            && (c = input.read()) == 'f'
                                            && (c = input.read()) == 'p') {
                                        return keywordOrIdentifier(SieveJavaTokenId.STRICTFP);
                                    }
                                    break;
                            }
                            break;
                        case 'u':
                            if ((c = input.read()) == 'p'
                                    && (c = input.read()) == 'e'
                                    && (c = input.read()) == 'r') {
                                return keywordOrIdentifier(SieveJavaTokenId.SUPER);
                            }
                            break;
                        case 'w':
                            if ((c = input.read()) == 'i'
                                    && (c = input.read()) == 't'
                                    && (c = input.read()) == 'c'
                                    && (c = input.read()) == 'h') {
                                return keywordOrIdentifier(SieveJavaTokenId.SWITCH);
                            }
                            break;
                        case 'y':
                            if ((c = input.read()) == 'n'
                                    && (c = input.read()) == 'c'
                                    && (c = input.read()) == 'h'
                                    && (c = input.read()) == 'r'
                                    && (c = input.read()) == 'o'
                                    && (c = input.read()) == 'n'
                                    && (c = input.read()) == 'i'
                                    && (c = input.read()) == 'z'
                                    && (c = input.read()) == 'e'
                                    && (c = input.read()) == 'd') {
                                return keywordOrIdentifier(SieveJavaTokenId.SYNCHRONIZED);
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 't':
                    switch (c = input.read()) {
                        case 'h':
                            switch (c = input.read()) {
                                case 'i':
                                    if ((c = input.read()) == 's') {
                                        return keywordOrIdentifier(SieveJavaTokenId.THIS);
                                    }
                                    break;
                                case 'r':
                                    if ((c = input.read()) == 'o'
                                            && (c = input.read()) == 'w') {
                                        switch (c = input.read()) {
                                            case 's':
                                                return keywordOrIdentifier(SieveJavaTokenId.THROWS);
                                            default:
                                                return keywordOrIdentifier(SieveJavaTokenId.THROW, c);
                                        }
                                    }
                                    break;
                            }
                            break;
                        case 'r':
                            switch (c = input.read()) {
                                case 'a':
                                    if ((c = input.read()) == 'n'
                                            && (c = input.read()) == 's'
                                            && (c = input.read()) == 'i'
                                            && (c = input.read()) == 'e'
                                            && (c = input.read()) == 'n'
                                            && (c = input.read()) == 't') {
                                        return keywordOrIdentifier(SieveJavaTokenId.TRANSIENT);
                                    }
                                    break;
                                case 'u':
                                    if ((c = input.read()) == 'e') {
                                        return keywordOrIdentifier(SieveJavaTokenId.TRUE);
                                    }
                                    break;
                                case 'y':
                                    return keywordOrIdentifier(SieveJavaTokenId.TRY);
                            }
                            break;
                    }
                    return finishIdentifier(c);

                case 'v':
                    if ((c = input.read()) == 'o') {
                        switch (c = input.read()) {
                            case 'i':
                                if ((c = input.read()) == 'd') {
                                    return keywordOrIdentifier(SieveJavaTokenId.VOID);
                                }
                                break;
                            case 'l':
                                if ((c = input.read()) == 'a'
                                        && (c = input.read()) == 't'
                                        && (c = input.read()) == 'i'
                                        && (c = input.read()) == 'l'
                                        && (c = input.read()) == 'e') {
                                    return keywordOrIdentifier(SieveJavaTokenId.VOLATILE);
                                }
                                break;
                        }
                    }
                    return finishIdentifier(c);

                case 'w':
                    if ((c = input.read()) == 'h'
                            && (c = input.read()) == 'i'
                            && (c = input.read()) == 'l'
                            && (c = input.read()) == 'e') {
                        return keywordOrIdentifier(SieveJavaTokenId.WHILE);
                    }
                    return finishIdentifier(c);

                // Rest of lowercase letters starting identifiers
                case 'h':
                case 'j':
                case 'k':
                case 'm':
                case 'o':
                case 'q':
                case 'u':
                case 'x':
                case 'y':
                case 'z':
                // Uppercase letters starting identifiers
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '$':
                case '_':
                    return finishIdentifier();

                // All Character.isWhitespace(c) below 0x80 follow
                // ['\t' - '\r'] and [0x1c - ' ']
                case '\t':
                case '\n':
                case 0x0b:
                case '\f':
                case '\r':
                case 0x1c:
                case 0x1d:
                case 0x1e:
                case 0x1f:
                    return finishWhitespace();
                case ' ':
                    c = input.read();
                    if (c == EOF || !Character.isWhitespace(c)) { // Return single space as flyweight token
                        input.backup(1);
                        return tokenFactory.getFlyweightToken(SieveJavaTokenId.WHITESPACE, " ");
                    }
                    return finishWhitespace();

                case EOF:
                    return null;

                default:
                    if (c >= 0x80) { // lowSurr ones already handled above
                        c = translateSurrogates(c);
                        if (Character.isJavaIdentifierStart(c)) {
                            return finishIdentifier();
                        }
                        if (Character.isWhitespace(c)) {
                            return finishWhitespace();
                        }
                    }

                    // Invalid char
                    return token(SieveJavaTokenId.ERROR);
            } // end of switch (c)
        } // end of while(true)
    }

    private int translateSurrogates(int c) {
        if (Character.isHighSurrogate((char) c)) {
            int lowSurr = input.read();
            if (lowSurr != EOF && Character.isLowSurrogate((char) lowSurr)) {
                // c and lowSurr form the integer unicode char.
                c = Character.toCodePoint((char) c, (char) lowSurr);
            } else {
                // Otherwise it's error: Low surrogate does not follow the high one.
                // Leave the original character unchanged.
                // As the surrogates do not belong to any
                // specific unicode category the lexer should finally
                // categorize them as a lexical error.
                input.backup(1);
            }
        }
        return c;
    }

    private Token<SieveJavaTokenId> finishWhitespace() {
        while (true) {
            int c = input.read();
            // There should be no surrogates possible for whitespace
            // so do not call translateSurrogates()
            if (c == EOF || !Character.isWhitespace(c)) {
                input.backup(1);
                return tokenFactory.createToken(SieveJavaTokenId.WHITESPACE);
            }
        }
    }

    private Token<SieveJavaTokenId> finishIdentifier() {
        return finishIdentifier(input.read());
    }

    private Token<SieveJavaTokenId> finishIdentifier(int c) {
        while (true) {
            if (c == EOF || !Character.isJavaIdentifierPart(c = translateSurrogates(c))) {
                // For surrogate 2 chars must be backed up
                input.backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);
                return tokenFactory.createToken(SieveJavaTokenId.IDENTIFIER);
            }
            c = input.read();
        }
    }

    private Token<SieveJavaTokenId> keywordOrIdentifier(SieveJavaTokenId keywordId) {
        return keywordOrIdentifier(keywordId, input.read());
    }

    private Token<SieveJavaTokenId> keywordOrIdentifier(SieveJavaTokenId keywordId, int c) {
        // Check whether the given char is non-ident and if so then return keyword
        if (c == EOF || !Character.isJavaIdentifierPart(c = translateSurrogates(c))) {
            // For surrogate 2 chars must be backed up
            input.backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);
            return token(keywordId);
        } else // c is identifier part
        {
            return finishIdentifier();
        }
    }

//    private Token<JavaTokenId> keywordSieve(JavaTokenId keywordId) {
//        return keywordSieve(keywordId, input.read());
//    }
//
//    private Token<JavaTokenId> keywordSieve(JavaTokenId keywordId, int c) {
//        // Check whether the given char is non-ident and if so then return keyword
//        if (c == EOF || !Character.isJavaIdentifierPart(c = translateSurrogates(c))) {
//            // For surrogate 2 chars must be backed up
//            input.backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);
//            return token(keywordId);
//        } else // c is identifier part
//        {
//            return token(JavaTokenId.ERROR);
//        }
//    }

    private Token<SieveJavaTokenId> finishNumberLiteral(int c, boolean inFraction) {
        while (true) {
            switch (c) {
                case '.':
                    if (!inFraction) {
                        inFraction = true;
                    } else { // two dots in the literal
                        return token(SieveJavaTokenId.FLOAT_LITERAL_INVALID);
                    }
                    break;
                case 'l':
                case 'L': // 0l or 0L
                    return token(SieveJavaTokenId.LONG_LITERAL);
                case 'd':
                case 'D':
                    return token(SieveJavaTokenId.DOUBLE_LITERAL);
                case 'f':
                case 'F':
                    return token(SieveJavaTokenId.FLOAT_LITERAL);
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
                case 'e':
                case 'E': // exponent part
                    return finishFloatExponent();
                default:
                    input.backup(1);
                    return token(inFraction ? SieveJavaTokenId.DOUBLE_LITERAL
                            : SieveJavaTokenId.INT_LITERAL);
            }
            c = input.read();
        }
    }

    private Token<SieveJavaTokenId> finishFloatExponent() {
        int c = input.read();
        if (c == '+' || c == '-') {
            c = input.read();
        }
        if (c < '0' || '9' < c) {
            return token(SieveJavaTokenId.FLOAT_LITERAL_INVALID);
        }
        do {
            c = input.read();
        } while ('0' <= c && c <= '9'); // reading exponent
        switch (c) {
            case 'd':
            case 'D':
                return token(SieveJavaTokenId.DOUBLE_LITERAL);
            case 'f':
            case 'F':
                return token(SieveJavaTokenId.FLOAT_LITERAL);
            default:
                input.backup(1);
                return token(SieveJavaTokenId.DOUBLE_LITERAL);
        }
    }

    private Token<SieveJavaTokenId> token(SieveJavaTokenId id) {
        String fixedText = id.fixedText();
        return (fixedText != null)
                ? tokenFactory.getFlyweightToken(id, fixedText)
                : tokenFactory.createToken(id);
    }

    public void release() {
    }
}
