package sk.tuke.kpi.ssce.core.model.view;

import sk.tuke.kpi.ssce.annotations.concerns.Model;
import sk.tuke.kpi.ssce.annotations.concerns.SievedDocument;
import sk.tuke.kpi.ssce.annotations.concerns.Synchronization;
import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;

/**
 * Trieda modeluje fragment kodu.
 * @author Matej Nosal
 */
//SsceIntent:Model pre synchronizaciu kodu;
@Model(model = RepresentationOf.VIEW)
public class CodeSnippet implements Comparable<CodeSnippet> {

//    private static final String START_TEXT = "#code :\n";
    //SsceIntent:Konstanta;
    @SievedDocument()
    private static final String START_TEXT_1_IN_SJDOC = "#code";
    //SsceIntent:Konstanta;
    @SievedDocument
    private static final String START_TEXT_2_IN_SJDOC = ":\n";
    @SievedDocument
    private static final String END_TEXT_IN_SJDOC = "\n";
    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
    @Synchronization
    private BindingPositions codeBinding;
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    private final String tab;
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    private final String codeContext;
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    private final String codeElementName;
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    private final String elementType;

    /**
     * Vrati text, ktory bude v pomocnom subore .sj uvadzat tento fragment kodu.
     * @return text, ktory bude v pomocnom subore .sj uvadzat tento fragment kodu.
     */
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    @SievedDocument
    public String getStartTextForSJDoc() {
        if (codeContext == null || codeContext.trim().length() == 0) {
            return START_TEXT_1_IN_SJDOC + " " + START_TEXT_2_IN_SJDOC + this.tab;
        } else {
            return START_TEXT_1_IN_SJDOC + " ( " + codeContext + " )" + START_TEXT_2_IN_SJDOC + this.tab;
        }
    }
    
    @SievedDocument
    public String getEndTextForSJDoc() {
        return END_TEXT_IN_SJDOC;
    }

    /**
     * Vytvori prepojenie pre fragment kodu.
     * @param initialTabSize uvodne odsadenie (medzera) fragmentu kodu v pomocnom subore.
     * @param codeContext kontext fragmentu kodu v textovej podobe.
     * @param codeElementName meno fragmentu kodu (elementu).
     * @param elementType typ fragmentu kodu (elementu).
     */
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    public CodeSnippet(int initialTabSize, String codeContext, String codeElementName, String elementType) {
        this.codeContext = codeContext;
        this.codeElementName = codeElementName;
        this.elementType = elementType;

        if (initialTabSize > 0) {
            this.tab = String.format("%" + initialTabSize + "s", "");
        } else {
            this.tab = "";
        }
    }

    /**
     * Vrati prepojenie usekov reprezentujucich tento fragment kodu.
     * @return prepojenie usekov reprezentujucich tento fragment kodu.
     */
    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
    public BindingPositions getCodeBinding() {
        return codeBinding;
    }

    /**
     * Nastavi prepojenie usekov reprezentujucich tento fragment kodu.
     * @param codeBinding prepojenie usekov reprezentujucich tento fragment kodu.
     */
    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
    @Synchronization
    public void setCodeBinding(BindingPositions codeBinding) {
        this.codeBinding = codeBinding;
    }

    /**
     * Vrati kontext fragmentu kodu v textovej podobe.
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
     * @return nazov fragmentu kodu (elementu).
     */
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    public String getElementName() {
        return codeElementName;
    }

    /**
     * Vrati cely nazov fragmentu kodu (elementu). Kontext + nazov elementu.
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
     * @return typ fragmentu kodu (elementu).
     */
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    public String getElementType() {
        return elementType;
    }
}
