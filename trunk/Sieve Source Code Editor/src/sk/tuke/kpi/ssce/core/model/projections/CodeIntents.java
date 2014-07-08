package sk.tuke.kpi.ssce.core.model.projections;

import java.util.Set;
import javax.swing.text.Position;
import sk.tuke.kpi.ssce.concerns.interfaces.Searchable;

/**
 * Trieda modeluje mapovanie zamerov na fragment kodu.
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Model pre mapovanie zamerov;
public class CodeIntents implements Comparable<CodeIntents> {

    private final JavaFileIntents parent;
    private final String codeHead;
    private final Position startPositionJavaCode;
    
    private final int lengthJavaCode;
    //SsceIntent:Komentar uchovavajuci zamer;
    @Deprecated
    private final Position startPositionIntentsComment;
    //SsceIntent:Komentar uchovavajuci zamer;
    @Deprecated
    private final int lengthIntentsComment;
    private final Set<Searchable> intents;

    /**
     * Vytvori mapovanie zamerov na fragment kodu.
     * @param parent rodicovsky java subor.
     * @param codeHead hlavicka fragmentu kodu, ktora sa zobrazuje pri definovani konfiguracie zamerov. Je to v podstate kontext + nazov elementu.
     * @param startPositionJavaCode zaciatocna pozicia fragmentu kodu v java subore.
     * @param lengthJavaCode dlzka fragmentu v java subore.
     * @param startPositionIntentsComment zaciatocna pozicia komentara uchovavajuceho zamer.
     * @param lengthIntentsComment dlzka komentara uchovavajuceho zamer.
     * @param intents mnozina zamerov priradenych tomuto fragmentu kodu.
     */
    public CodeIntents(JavaFileIntents parent, String codeHead,
            Position startPositionJavaCode, int lengthJavaCode,
            @Deprecated Position startPositionIntentsComment, @Deprecated int lengthIntentsComment,
            Set<Searchable> intents) {
        this.parent = parent;
        this.codeHead = codeHead;
        this.startPositionJavaCode = startPositionJavaCode;
        this.lengthJavaCode = lengthJavaCode;
        this.startPositionIntentsComment = startPositionIntentsComment;
        this.lengthIntentsComment = lengthIntentsComment;
        this.intents = intents;
    }

    /**
     * Vrati rodicovsky java subor tohto fragmentu kodu.
     * @return rodicovsky java subor tohto fragmentu kodu.
     */
    public JavaFileIntents getParent() {
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
     * Vrati dlzku komentara uchovavajuceho zamer tohto fragmentu kodu.
     * @return dlzku komentara uchovavajuceho zamer tohto fragmentu kodu.
     */
    //SsceIntent:Komentar uchovavajuci zamer;
    @Deprecated
    public int getLengthIntentsComment() {
        return lengthIntentsComment;
    }

    /**
     * Vrati zaciatocnu poziciu tohto fragmentu kodu.
     * @return zaciatocnu poziciu tohto fragmentu kodu.
     */
    
    public Position getStartPositionJavaCode() {
        return startPositionJavaCode;
    }

    /**
     * Vrati zaciatocnu poziciu komentara uchovavajuceho zamer tohto fragmentu kodu.
     * @return zaciatocnu poziciu komentara uchovavajuceho zamer tohto fragmentu kodu.
     */
    //SsceIntent:Komentar uchovavajuci zamer;
    @Deprecated
    public Position getStartPositionIntentsComment() {
        return startPositionIntentsComment;
    }

    /**
     * Vrati mnozinu zamerov priradenych tomutu fragmentu kodu.
     * @return mnozinu zamerov priradenych tomutu fragmentu kodu.
     */
    public Set<Searchable> getIntents() {
        return intents;
    }

    /**
     * Vrati hlavicku fragmentu kodu, ktora sa zobrazuje pri definovani konfiguracie zamerov. Je to v podstate kontext + nazov elementu.
     * @return hlavicku fragmentu kodu.
     */
    public String getCodeHead() {
        return codeHead;
    }

    @Override
    public int compareTo(CodeIntents o) {
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
