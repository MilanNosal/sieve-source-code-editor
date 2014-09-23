package sk.tuke.kpi.ssce.core.model.view;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import sk.tuke.kpi.ssce.annotations.concerns.Model;
import sk.tuke.kpi.ssce.annotations.concerns.Synchronization;
import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;

/**
 * Trieda sluzi na prepojenie dvoch usekoch, jedneho v pomocnom subore .sj a
 * druheho v java subore.
 *
 * @author Matej Nosal
 */
//SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
@Synchronization()
@Model(model = RepresentationOf.VIEW)
public class BindingPositions {

    private Position startPositionJavaDocument;
    private int lengthJavaDocument;
    private Position startPositionSieveDocument;
    private Position endPositionSieveDocument;
    
    /**
     * Vytvori nedefinovane prepojenie dvoch usekov.
     */
    public BindingPositions() {
    }

    /**
     * Vytvori ciastocne definovane prepojenie dvoch usekov. Usek v java subore
     * je definovany.
     *
     * @param startPositionJavaDocument zaciatocna pozicia useku v java subore.
     * @param lengthJavaDocument dlzka useku v java subore.
     */
    public BindingPositions(Position startPositionJavaDocument, int lengthJavaDocument) {
        this.startPositionJavaDocument = startPositionJavaDocument;
        this.lengthJavaDocument = lengthJavaDocument;
    }
    

    /**
     * Vrati dlzku useku v java subore.
     *
     * @return dlzku useku v java subore.
     */
    public int getLengthBindingAreaJavaDocument() {
        return lengthJavaDocument;
    }

    /**
     * Vrati koncovu poziciu (offset) useku v java subore.
     *
     * @return koncovu poziciu (offset) useku v java subore.
     */
    public int getEndPositionJavaDocument() {
        return startPositionJavaDocument.getOffset() + lengthJavaDocument;
    }

    /**
     * Nastavi dlzku useku v java subore.
     *
     * @param lengthJavaDocument dlzka useku v java subore.
     */
    public void setLengthJavaDocument(int lengthJavaDocument) {
        this.lengthJavaDocument = lengthJavaDocument;
    }

    /**
     * Vrati koncovu poziciu (offset) useku v pomocnom subore .sj.
     *
     * @return koncovu poziciu (offset) useku v pomocnom subore .sj.
     */
    public int getEndPositionSieveDocument() {
        return endPositionSieveDocument.getOffset() - 1;
    }

    /**
     * Nastavi koncovu poziciu v pomocnom subore .sj.
     *
     * @param doc dokument pomocneho suboru .sj.
     * @param endPositionSieveDocument koncova pozicia (offset) useku v pomocnom
     * subore .sj.
     * @throws BadLocationException
     */
    //SsceIntent:Praca s pomocnym suborom;
    public void setEndPositionSieveDocument(BaseDocument doc, int endPositionSieveDocument) throws BadLocationException {
        this.endPositionSieveDocument = doc.createPosition(endPositionSieveDocument + 1);
    }

    /**
     * Vrati dlzku useku v pomocnom subore .sj.
     *
     * @return dlzku useku v pomocnom subore .sj.
     */
    public int getLengthBindingAreaSieveDocument() {
        return getEndPositionSieveDocument() - getStartPositionSieveDocument() + 1;
    }

    /**
     * Vrati zaciatocnu poziciu (offset) useku v java subore.
     *
     * @return zaciatocnu poziciu (offset) useku v java subore.
     */
    public int getStartPositionJavaDocument() {
        return startPositionJavaDocument.getOffset();
    }

    /**
     * Nastavi zaciatocnu poziciu useku v java subore.
     *
     * @param doc dokument pre java subor.
     * @param startPositionJavaDocument zaciatocna pozicia (offset) useku.
     * @throws BadLocationException
     */
    public void setStartPositionJavaDocument(BaseDocument doc, int startPositionJavaDocument) throws BadLocationException {
        this.startPositionJavaDocument = doc.createPosition(startPositionJavaDocument);
    }

    /**
     * Vrati zaciatocnu poziciu (offset) useku v pomocnom subore .sj.
     *
     * @return zaciatocnu poziciu (offset) useku v pomocnom subore .sj.
     */
    public int getStartPositionSieveDocument() {
        return startPositionSieveDocument.getOffset() + 1;
    }

    /**
     * Nastavi zaciatocnu poziciu useku v pomocnom subore .sj.
     *
     * @param doc dokument pre pomocny subor .sj.
     * @param startPositionSieveDocument zaciatocna pozicia (offset) useku.
     * @throws BadLocationException
     */
    //SsceIntent:Praca s pomocnym suborom;
    public void setStartPositionSieveDocument(BaseDocument doc, int startPositionSieveDocument) throws BadLocationException {
        this.startPositionSieveDocument = doc.createPosition(startPositionSieveDocument - 1);
    }

    /**
     * Overi ci prepojenie usekov je konzistentne. T.j. oba useky su jednoznacne
     * definovane.
     *
     * @return true, ak je prepojenie konzistentne, v opacnom pripade false.
     */
    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
    public boolean isInitialized() {
        return startPositionJavaDocument != null && lengthJavaDocument > -1
                && startPositionSieveDocument != null && endPositionSieveDocument != null
                && startPositionSieveDocument.getOffset() <= endPositionSieveDocument.getOffset();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("startSieve=").append(startPositionSieveDocument.getOffset()).append(" endSieve=").append(endPositionSieveDocument.getOffset()).append(" StartJava=").append(startPositionJavaDocument.getOffset()).append(" lengthJava=").append(lengthJavaDocument).append(")");
        return builder.toString();
    }
}
