package sk.tuke.kpi.ssce.core.model.view;

import java.util.Set;
import java.util.Stack;
import org.netbeans.editor.BaseDocument;
import sk.tuke.kpi.ssce.annotations.concerns.Model;
import sk.tuke.kpi.ssce.annotations.concerns.SievedDocument;
import sk.tuke.kpi.ssce.annotations.concerns.Synchronization;
import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;

/**
 * Trieda modeluje fragment kodu.
 *
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Model pre synchronizaciu kodu;
@Model(model = RepresentationOf.VIEW)
public class CodeSnippet<T extends Concern> implements Comparable<CodeSnippet> {

//    private static final String START_TEXT = "#code :\n";
    //SsceIntent:Konstanta;
    @SievedDocument()
    public static final String START_TEXT_1_IN_SJDOC = "#code";
    //SsceIntent:Konstanta;
    @SievedDocument
    public static final String START_TEXT_2_IN_SJDOC = ":\n";
    @SievedDocument
    public static final String END_TEXT_IN_SJDOC = "\n";
    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
    @Synchronization
    private BindingPositions codeBinding;
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    private final String initialTab;
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    private final String codeContext;
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    private final String codeElementName;
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    private final String elementType;
    
    private final Stack<Set<T>> snippetsConcerns;

    private final BaseDocument document;

    /**
     * Vrati text, ktory bude v pomocnom subore .sj uvadzat tento fragment kodu.
     *
     * @return text, ktory bude v pomocnom subore .sj uvadzat tento fragment
     * kodu.
     */
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    @SievedDocument
    public String getStartTextForSJDoc() {
//        if (codeContext == null || codeContext.trim().length() == 0) {
//            return START_TEXT_1_IN_SJDOC + " " + START_TEXT_2_IN_SJDOC + this.initialTab;
//        } else {
        return START_TEXT_1_IN_SJDOC + " ( " + this.getFullElementName() + " : " + this.getElementType() + " )" + START_TEXT_2_IN_SJDOC + this.initialTab;
//        }
    }

    @SievedDocument
    public String getEndTextForSJDoc() {
        return END_TEXT_IN_SJDOC;
    }

    /**
     * Vytvori prepojenie pre fragment kodu.
     *
     * @param initialTabSize uvodne odsadenie (medzera) fragmentu kodu v
     * pomocnom subore.
     * @param codeContext kontext fragmentu kodu v textovej podobe.
     * @param codeElementName meno fragmentu kodu (elementu).
     * @param elementType typ fragmentu kodu (elementu).
     */
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    public CodeSnippet(String initialTab, String codeContext, String codeElementName,
            String elementType, Stack<Set<T>> concerns, BaseDocument document) {
        this.codeContext = codeContext;
        this.codeElementName = codeElementName;
        this.elementType = elementType;
        this.initialTab = initialTab;
        this.document = document;
        this.snippetsConcerns = concerns;
    }

    public BaseDocument getDocument() {
        return document;
    }

    public Stack<Set<T>> getSnippetsConcerns() {
        return snippetsConcerns;
    }

    /**
     * Vrati prepojenie usekov reprezentujucich tento fragment kodu.
     *
     * @return prepojenie usekov reprezentujucich tento fragment kodu.
     */
    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
    public BindingPositions getCodeBinding() {
        return codeBinding;
    }

    /**
     * Nastavi prepojenie usekov reprezentujucich tento fragment kodu.
     *
     * @param codeBinding prepojenie usekov reprezentujucich tento fragment
     * kodu.
     */
    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
    @Synchronization
    public void setCodeBinding(BindingPositions codeBinding) {
        this.codeBinding = codeBinding;
    }

    /**
     * Vrati kontext fragmentu kodu v textovej podobe.
     *
     * @return kontext fragmentu kodu v textovej podobe.
     */
    @Synchronization
    public String getCodeContext() {
        return codeContext;
    }

    @Override
    public int compareTo(CodeSnippet o) {
        if (this.codeBinding.getStartPositionJavaDocument() > o.codeBinding.getStartPositionJavaDocument()) {
            return 1;
        } else if (this.codeBinding.getStartPositionJavaDocument() == o.codeBinding.getStartPositionJavaDocument()) {
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * Vrati nazov fragmentu kodu (elementu).
     *
     * @return nazov fragmentu kodu (elementu).
     */
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    public String getElementName() {
        return codeElementName;
    }

    /**
     * Vrati cely nazov fragmentu kodu (elementu). Kontext + nazov elementu.
     *
     * @return cely nazov fragmentu kodu (elementu).
     */
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    public String getFullElementName() {
        if (codeContext != null && codeContext.trim().length() > 0) {
            return codeContext + "." + codeElementName;
        } else {
            return codeElementName;
        }
    }

    /**
     * vrati typ fragmentu kodu (elementu).
     *
     * @return typ fragmentu kodu (elementu).
     */
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    public String getElementType() {
        return elementType;
    }
}
