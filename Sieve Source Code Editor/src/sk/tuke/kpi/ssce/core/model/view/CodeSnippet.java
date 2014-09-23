package sk.tuke.kpi.ssce.core.model.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import sk.tuke.kpi.ssce.annotations.concerns.Model;
import sk.tuke.kpi.ssce.annotations.concerns.SievedDocument;
import sk.tuke.kpi.ssce.annotations.concerns.Synchronization;
import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.FoldingRequest;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.GuardingRequest;

/**
 * Trieda modeluje fragment kodu.
 * @author Matej Nosal, Milan Nosal
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
    private final String initialTab;
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    private final String codeContext;
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    private final String codeElementName;
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    private final String elementType;
    
    private final List<GuardingRequest> guardingRequests = new LinkedList<GuardingRequest>();
    private final List<FoldingRequest> foldingRequests = new LinkedList<FoldingRequest>();

    /**
     * Vrati text, ktory bude v pomocnom subore .sj uvadzat tento fragment kodu.
     * @return text, ktory bude v pomocnom subore .sj uvadzat tento fragment kodu.
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
     * @param initialTabSize uvodne odsadenie (medzera) fragmentu kodu v pomocnom subore.
     * @param codeContext kontext fragmentu kodu v textovej podobe.
     * @param codeElementName meno fragmentu kodu (elementu).
     * @param elementType typ fragmentu kodu (elementu).
     */
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    public CodeSnippet(String initialTab, String codeContext, String codeElementName, String elementType) {
        this.codeContext = codeContext;
        this.codeElementName = codeElementName;
        this.elementType = elementType;
        this.initialTab = initialTab;
    }
    
    public void addGuardingRequestAbsolute(int absoluteStartOffset, int absoluteEndOffset) {
        this.guardingRequests.add(GuardingRequest.create(absoluteStartOffset, absoluteEndOffset));
    }
    
    public void addGuardingRequestRelative(int relativeStartOffset, int relativeEndOffset) {
        int startInSieve = this.codeBinding.getStartPositionSieveDocument();
        this.guardingRequests.add(GuardingRequest.create(
                startInSieve + relativeStartOffset,
                startInSieve + relativeEndOffset));
    }
    
    public void clearGuardingRequests() {
        this.guardingRequests.clear();
    }
    
    public List<GuardingRequest> getGuardingRequests() {
        List<GuardingRequest> updatedGuardingRequests = new LinkedList<GuardingRequest>(this.guardingRequests);
        updatedGuardingRequests.addAll(standardGuardingRequests());
        return updatedGuardingRequests;
    }
    
    public void addFoldingRequestAbsolute(int absoluteStartOffset, int absoluteEndOffset, String description) {
        this.foldingRequests.add(FoldingRequest.create(absoluteStartOffset, absoluteEndOffset, description));
    }
    
    public void addFoldingRequestAbsolute(int absoluteStartOffset, int absoluteEndOffset, String description,
            int guardedLengthStart, int guardedLengthEnd) {
        this.foldingRequests.add(FoldingRequest.create(absoluteStartOffset, absoluteEndOffset, description,
                guardedLengthStart, guardedLengthEnd));
    }
    
    public void addFoldingRequestRelative(int relativeStartOffset, int relativeEndOffset, String description) {
        int startInSieve = this.codeBinding.getStartPositionSieveDocument();
        this.foldingRequests.add(FoldingRequest.create(
                startInSieve + relativeStartOffset,
                startInSieve + relativeEndOffset,
                description));
    }
    
    public void addFoldingRequestRelative(int relativeStartOffset, int relativeEndOffset, String description,
            int guardedLengthStart, int guardedLengthEnd) {
        int startInSieve = this.codeBinding.getStartPositionSieveDocument();
        this.foldingRequests.add(FoldingRequest.create(
                startInSieve + relativeStartOffset,
                startInSieve + relativeEndOffset,
                description,
                guardedLengthStart, guardedLengthEnd));
    }
    
    public void clearFoldingRequests() {
        this.foldingRequests.clear();
    }
    
    public List<FoldingRequest> getFoldingRequests() {
        List<FoldingRequest> updatedFoldingRequests = new LinkedList<FoldingRequest>(this.foldingRequests);
        updatedFoldingRequests.addAll(standardFoldingRequests());
        return updatedFoldingRequests;
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
    
    /**
     * Has to use absolute offsets.
     * @return 
     */
    private List<FoldingRequest> standardFoldingRequests() {
        List<FoldingRequest> folds = new ArrayList<FoldingRequest>();
        if (this.codeBinding.isInitialized()) {
            int startInSieve = this.codeBinding.getStartPositionSieveDocument();
            folds.add(FoldingRequest.create(
                    startInSieve + 1 - (this.getStartTextForSJDoc().length() - START_TEXT_1_IN_SJDOC.length()),
                    this.codeBinding.getEndPositionSieveDocument() + 1, 
                    " " + this.getFullElementName() + " : " + this.getElementType() + " "));
        }
        return folds;
    }
    
    /**
     * Has to use absolute offsets.
     * @return 
     */
    private List<GuardingRequest> standardGuardingRequests() {
        List<GuardingRequest> guards = new ArrayList<GuardingRequest>();
        guards.add(GuardingRequest.create(
                this.getCodeBinding().getStartPositionSieveDocument() - this.getStartTextForSJDoc().length(),
                this.getCodeBinding().getStartPositionSieveDocument()
        ));
        guards.add(GuardingRequest.create(
                this.getCodeBinding().getEndPositionSieveDocument() + 1,
                this.getCodeBinding().getEndPositionSieveDocument() + this.getEndTextForSJDoc().length()
        ));
        return guards;
    }
}
