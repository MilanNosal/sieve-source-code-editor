package sk.tuke.kpi.ssce.core;

import java.io.IOException;
import java.util.Collections;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Matej Nosal, Milan Nosal
 */
public class CompilationUtilities {
    /**
     * Extrahuje kompilacne info pre zdrojovy kod v dokumente doc. Predtym to tahal
     * Mato z kontextu ako property, teraz to vytvaram zakazdym nanovo. Zda sa,
     * ze v tom starom modeli niekedy nebol syncnuty aktualny stav s kompilacnym info
     * a preto mi vracievalo null.
     * @param doc dukument so zdrojovym kodom.
     * @return kompilacne info pre zdrojovy kod v dokumente doc.
     */
    //SsceIntent:Syntakticka analyza java dokumentu;
    public static CompilationInfo getCompilationInfo(BaseDocument doc) {

        final Lookup lookup = MimeLookup.getLookup("text/x-java");
        final ParserFactory parserFactory = lookup.lookup(ParserFactory.class);
        if (parserFactory == null) {
            throw new IllegalArgumentException("No parser for mime type: text/x-java");
        }
        Snapshot snapshot = Source.create(doc).createSnapshot();

        Parser p = parserFactory.createParser(Collections.singletonList(snapshot));
        final UserTask task = new UserTask() {

            @Override
            public void run(ResultIterator ri) throws Exception {
            }
        };

        Utilities.acquireParserLock();
        try {
            p.parse(snapshot, task, null);

            CompilationInfo info = CompilationInfo.get(p.getResult(task));
            ((CompilationController) info).toPhase(JavaSource.Phase.PARSED);
            return info;

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            Utilities.releaseParserLock();
        }

        return null;
    }
    
    /**
     * Skonstruuje kompilacne info pre zdrojovy kode v dokumente doc od offset o dlzke length.
     * @param doc dukument so zdrojovym kodom.
     * @param offset offset zaciatku useku zdrojoveho kodu, pre ktory sa ma kompilacne info vytvorit.
     * @param length dlzka useku zdrojoveho kodu, pre ktory sa ma kompilacne info vytvorit.
     * @return kompilacne info pre zdrojovy kode v dokumente doc od offset o dlzke length.
     */
    //SsceIntent:Syntakticka analyza java dokumentu;
    public static CompilationInfo getCompilationInfo(BaseDocument doc, int offset, int length) {
        final Lookup lookup = MimeLookup.getLookup("text/x-java");
        final ParserFactory parserFactory = lookup.lookup(ParserFactory.class);
        if (parserFactory == null) {
            throw new IllegalArgumentException("No parser for mime type: text/x-java");
        }
        Snapshot snapshot = Source.create(doc).createSnapshot().create(offset, length, "text/x-java").getSnapshot();

        Parser p = parserFactory.createParser(Collections.singletonList(snapshot));
        final UserTask task = new UserTask() {

            @Override
            public void run(ResultIterator ri) throws Exception {
            }
        };
        Utilities.acquireParserLock();
        try {
            p.parse(snapshot, task, null);

            CompilationInfo info = CompilationInfo.get(p.getResult(task));
            ((CompilationController) info).toPhase(JavaSource.Phase.PARSED);
            return info;

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            Utilities.releaseParserLock();
        }

        return null;
    }
}
