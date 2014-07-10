package sk.tuke.kpi.ssce.core.model.possibleprojections;

import java.util.Set;
import javax.swing.text.Position;
import sk.tuke.kpi.ssce.annotations.concerns.Model;
import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;

/**
 * Trieda modeluje mapovanie zamerov na fragment kodu.
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Model pre mapovanie zamerov;
@Model(model = RepresentationOf.PROJECTION)
public class CodeSnippetConcerns implements Comparable<CodeSnippetConcerns> {

    private final JavaFileConcerns parent;
    private final String codeHead;
    private final Position startPositionJavaCode;
    
    private final int lengthJavaCode;
    
    private final Set<Concern> concerns;

    /**
     * Vytvori mapovanie zamerov na fragment kodu.
     * @param parent rodicovsky java subor.
     * @param codeHead hlavicka fragmentu kodu, ktora sa zobrazuje pri definovani konfiguracie zamerov. Je to v podstate kontext + nazov elementu.
     * @param startPositionJavaCode zaciatocna pozicia fragmentu kodu v java subore.
     * @param lengthJavaCode dlzka fragmentu v java subore.
     * @param concerns mnozina zamerov priradenych tomuto fragmentu kodu.
     */
    public CodeSnippetConcerns(JavaFileConcerns parent, String codeHead,
            Position startPositionJavaCode, int lengthJavaCode,
            Set<Concern> concerns) {
        this.parent = parent;
        this.codeHead = codeHead;
        this.startPositionJavaCode = startPositionJavaCode;
        this.lengthJavaCode = lengthJavaCode;
        this.concerns = concerns;
    }

    /**
     * Vrati rodicovsky java subor tohto fragmentu kodu.
     * @return rodicovsky java subor tohto fragmentu kodu.
     */
    public JavaFileConcerns getParent() {
        return parent;
    }

    /**
     * Vrati dlzku fragmentu kodu v java subore.
     * @return
     */
    public int getLengthJavaCode() {
        return lengthJavaCode;
    }

    /**
     * Vrati zaciatocnu poziciu tohto fragmentu kodu.
     * @return zaciatocnu poziciu tohto fragmentu kodu.
     */
    
    public Position getStartPositionJavaCode() {
        return startPositionJavaCode;
    }

    /**
     * Vrati mnozinu zamerov priradenych tomutu fragmentu kodu.
     * @return mnozinu zamerov priradenych tomutu fragmentu kodu.
     */
    public Set<Concern> getConcerns() {
        return concerns;
    }

    /**
     * Vrati hlavicku fragmentu kodu, ktora sa zobrazuje pri definovani konfiguracie zamerov. Je to v podstate kontext + nazov elementu.
     * @return hlavicku fragmentu kodu.
     */
    public String getCodeHead() {
        return codeHead;
    }

    @Override
    public int compareTo(CodeSnippetConcerns o) {
        if (this.startPositionJavaCode.getOffset() > o.startPositionJavaCode.getOffset()) {
            return 1;
        } else if (this.startPositionJavaCode.getOffset() == o.startPositionJavaCode.getOffset()) {
            if (this.lengthJavaCode < o.lengthJavaCode) {
                return 1;
            } else if (this.lengthJavaCode == o.lengthJavaCode) {
                return 0;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }
}
