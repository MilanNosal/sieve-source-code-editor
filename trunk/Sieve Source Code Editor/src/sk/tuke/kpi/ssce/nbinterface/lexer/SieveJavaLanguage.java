/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.tuke.kpi.ssce.nbinterface.lexer;

import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;

/**
 *
 * @author Matej Nosal
 */ 
@LanguageRegistration(mimeType = "text/x-sieve-java")
public class SieveJavaLanguage extends DefaultLanguageConfig {

    @Override
    public Language getLexerLanguage() {
        return SieveJavaTokenId.language();
    }

    @Override
    public String getDisplayName() {
        return "Sieve Java";
    }
}
