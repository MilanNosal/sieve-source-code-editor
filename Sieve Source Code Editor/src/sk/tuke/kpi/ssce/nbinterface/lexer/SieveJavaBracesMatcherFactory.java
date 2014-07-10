/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.tuke.kpi.ssce.nbinterface.lexer;

import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport;

/**
 *
 * @author Matej Nosal
 */
//SsceIntent:Zvyraznovanie sparovanych zatvoriek v pomocnom subore;
public class SieveJavaBracesMatcherFactory implements BracesMatcherFactory {

    @Override
    public BracesMatcher createMatcher(MatcherContext context) {
        return BracesMatcherSupport.defaultMatcher(context, -1, -1);
    }
}