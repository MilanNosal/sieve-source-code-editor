package sk.tuke.kpi.ssce.core.model.view;

import sk.tuke.kpi.ssce.core.model.view.postprocessing.GuardingRequest;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.FoldingRequest;
import sk.tuke.kpi.ssce.core.model.view.importshandling.Imports;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.openide.cookies.EditorCookie;
import sk.tuke.kpi.ssce.annotations.concerns.ImportsManagement;
import sk.tuke.kpi.ssce.annotations.concerns.Model;
import sk.tuke.kpi.ssce.annotations.concerns.SievedDocument;
import sk.tuke.kpi.ssce.annotations.concerns.Synchronization;
import sk.tuke.kpi.ssce.annotations.concerns.View;
import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;
import sk.tuke.kpi.ssce.annotations.concerns.enums.ViewAspect;

/**
 * Trieda modeluje prepojenie jedneho java suboru s pomocnym suborom .sj.
 * @author Matej Nosal
 */
//SsceIntent:Model pre synchronizaciu kodu;
@Model(model = RepresentationOf.VIEW)
public class JavaFile {
    
    private boolean modified = false;

    @Synchronization
    private Position beginInSJ; // in sieve document
    @Synchronization
    private Position endInSJ;
    //SsceIntent:Konstanta;
    private final EditorCookie editorCookie;
    //SsceIntent:Konstanta;
    private final String filePath;
    //SsceIntent:Konstanta;Zobrazenie fragmentu kodu v pomocnom subore;
    private final String fileName;
    //SsceIntent:Zobrazenie fragmentu kodu v pomocnom subore;
    private String packageName;
    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
    private BindingPositions importsBinding; //binding between allImports in file and necesaryImports in sieve file
    
    private final List<GuardingRequest> guardingRequests = new LinkedList<GuardingRequest>();
    private final List<FoldingRequest> foldingRequests = new LinkedList<FoldingRequest>();
    
    @ImportsManagement
    private Imports allImports;
    @ImportsManagement
    private Imports necessaryImports;
    
    private List<CodeSnippet> codeSnippets;

    /**
     * Vytvori model prepojenia jedneho java suboru s pomocnym suborom .sj.
     * @param filePath cesta modelovaneho java suboru.
     * @param fileName meno modelovaneho java suboru.
     * @param editorCookie editorCookie modelovaneho java suboru.
     */
    public JavaFile(String filePath, String fileName, EditorCookie editorCookie) {
        this.editorCookie = editorCookie;
        this.filePath = filePath;
        this.fileName = fileName;
        initialize();
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }
    
    public void addGuardingRequest(GuardingRequest request) {
        this.guardingRequests.add(request);
    }
    
    public List<GuardingRequest> getGuardingRequests() {
        List<GuardingRequest> updatedGuardingRequests = new LinkedList<GuardingRequest>();
        // TODO: zdedene zo snippetov?
        for(GuardingRequest request : this.guardingRequests) {
            updatedGuardingRequests.add(GuardingRequest.create(
                    request.getStartOffset() + this.beginInSJ.getOffset(),
                    request.getEndOffset() + this.endInSJ.getOffset()));
        }
        return updatedGuardingRequests;
    }
    
    public void addFoldingRequest(FoldingRequest request) {
        this.foldingRequests.add(request);
    }
    
    public List<FoldingRequest> getFoldingRequests() {
        List<FoldingRequest> updatedFoldingRequests = new LinkedList<FoldingRequest>();
        // TODO: zdedene zo snippetov?
        for(FoldingRequest request : this.foldingRequests) {
            updatedFoldingRequests.add(FoldingRequest.create(
                    request.getStartOffset() + this.beginInSJ.getOffset(),
                    request.getEndOffset() + this.endInSJ.getOffset()));
        }
        return updatedFoldingRequests;
    }

    private void initialize() {
        this.allImports = new Imports();
        this.necessaryImports = new Imports();
        this.codeSnippets = new ArrayList<CodeSnippet>();
    }

    /**
     * Vrati model prepojenia fragmentu kodu podla offsetu v pomocnom subore .sj, ktory patri do tohto java suboru.
     * @param offset offset v pomocnom subore .sj
     * @return model prepojenia fragmentu kodu, ak na offsete nie je ziaden fragment tak null.
     */
    public CodeSnippet getCodeSnippetBySJOffset(int offset) {
        for (CodeSnippet codeSnippet : codeSnippets) {
            if (codeSnippet.getCodeBinding().isInitialized() && codeSnippet.getCodeBinding().getStartPositionSieveDocument() <= offset && offset <= codeSnippet.getCodeBinding().getEndPositionSieveDocument() + 1) {
                return codeSnippet;
            }
        }
        return null;
    }

    /**
     * Vrati zaciatocnu poziciu (offset) tohto java suboru v pomocnom subore .sj.
     * @return zaciatocnu poziciu (offset) tohto java suboru v pomocnom subore .sj.
     */
    @Synchronization
    public int getEndInSJ() {
        if (endInSJ == null) {
            return -1;
        }
        return endInSJ.getOffset() + 1;
    }

    /**
     * Nastavi koncovu poziciu tohto java suboru v pomocnom subore .sj.
     * @param doc dokument pre pomocny subor .sj.
     * @param enInSJ koncova pozicia tohto java suboru v pomocnom subore .sj.
     * @throws BadLocationException
     */
    //SsceIntent:Praca s pomocnym suborom;
    @Synchronization
    public void setEndInSJ(BaseDocument doc, int enInSJ) throws BadLocationException {
        this.endInSJ = doc.createPosition(enInSJ - 1);
    }

    /**
     * Vrati koncovu poziciu (offset) tohto java suboru v pomocnom subore .sj.
     * @return koncovu poziciu (offset) tohto java suboru v pomocnom subore .sj.
     */
    @Synchronization
    public int getBeginInSJ() {
        if (beginInSJ == null) {
            return -1;
        }
        return beginInSJ.getOffset() - 1;
    }

    /**
     * Nastavi zaciatocnu poziciu tohto java suboru v pomocnom subore .sj.
     * @param doc dokument pre pomocny subor .sj.
     * @param startFile zaciatocnu pozicia tohto java suboru v pomocnom subore .sj.
     * @throws BadLocationException
     */
    @Synchronization
    //SsceIntent:Praca s pomocnym suborom;
    public void setBeginInSJ(BaseDocument doc, int beginInSJ) throws BadLocationException {
        this.beginInSJ = doc.createPosition(beginInSJ + 1);
    }

    /**
     * Vrati model importov pre tento java subor.
     * @return model importov pre tento java subor.
     */
    @ImportsManagement
    public Imports getAllImports() {
        return allImports;
    }

    /**
     * Nastavi model importov pre tento java subor.
     * @param allImports model importov pre tento java subor.
     */
    @ImportsManagement
    public void setAllImports(Imports allImports) {
        this.allImports = allImports;
    }

    /**
     * Vrati zoznam vsetkych modelovanych fragmentov kodu.
     * @return zoznam vsetkych modelovanych fragmentov kodu.
     */
    public List<CodeSnippet> getCodeSnippets() {
        return codeSnippets;
    }

    /**
     * Nastavi zoznam vsetkych modelovanych fragmentov kodu.
     * @param codes zoznam vsetkych modelovanych fragmentov kodu.
     */
    public void setCodeSnippets(List<CodeSnippet> codes) {
        this.codeSnippets = codes;
    }

    /**
     * Vrati model prepojenia usekov pre importy.
     * @return model prepojenia usekov pre importy.
     */
    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
    public BindingPositions getImportsBinding() {
        return importsBinding;
    }

    /**
     * Nastavi model prepojenia usekov pre importy.
     * @param importsBinding model prepojenia usekov pre importy.
     */
    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
    @ImportsManagement
    public void setImportsBinding(BindingPositions importsBinding) {
        this.importsBinding = importsBinding;
    }

    /**
     * Vrati nazov tohto java suboru.
     * @return nazov tohto java suboru.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Vrati nazov balika, v ktorom sa nachadza tento subor.
     * @return nazov balika, v ktorom sa nachadza tento subor.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Nastavi nazov balika, v ktorom sa nachadza tento subor.
     * @param packageName nazov balika, v ktorom sa nachadza tento subor.
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Vrati importy, ktore su nevyhnutne pre fragmenty kodu, ktore tento java subor modeluje.
     * @return importy, ktore su nevyhnutne pre fragmenty kodu, ktore tento java subor modeluje.
     */
    @ImportsManagement
    public Imports getNecessaryImports() {
        return necessaryImports;
    }

    /**
     * Nastavi importy, ktore su nevyhnutne pre fragmenty kodu, ktore tento java subor modeluje.
     * @param necessaryImports importy, ktore su nevyhnutne pre fragmenty kodu, ktore tento java subor modeluje.
     */
    @ImportsManagement
    public void setNecessaryImports(Imports necessaryImports) {
        this.necessaryImports = necessaryImports;
    }

    /**
     * Vrati cestu tohto java suboru.
     * @return cestu tohto java suboru.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Vrati editorCookie tohto java suboru.
     * @return editorCookie tohto java suboru.
     */
    public EditorCookie getEditorCookie() {
        return editorCookie;
    }

    /**
     * Vrati text, ktory uvadza jeden java subor v pomocnom subore .sj.
     * @return text, ktory uvadza jeden java subor v pomocnom subore .sj.
     */
    //SsceIntent:Zobrazenie projekcie kodu v pomocnom subore;
    @SievedDocument
    public String getStartTextForSJDoc() {
        if (this.getPackageName() == null) {
            return "#file ( " + this.fileName + " ){\n";
        } else {
            return "#file ( " + this.getPackageName() + "." + this.fileName + " ){\n";
        }
    }

    /**
     * Vrati text, ktory uzatvara jeden java subor v pomocnom subore .sj.
     * @return text, ktory uzatvara jeden java subor v pomocnom subore .sj.
     */
    //SsceIntent:Zobrazenie projekcie kodu v pomocnom subore;
    @SievedDocument
    public String getEndTextForSJDoc() {
        return "}\n";
    }

    /**
     * Tento model java suboru sa aktualicuje na zaklade java suboru file.
     * @param file java subor, podla ktoreho bude tento java subor aktualizovany.
     */
    public void copy(JavaFile file) {
        this.allImports = file.allImports;
        this.necessaryImports = file.necessaryImports;
        this.importsBinding = file.importsBinding;
        this.codeSnippets = file.codeSnippets;
        this.packageName = file.packageName;
    }

    /**
     * Overi ci prepojenie vsetkych usekov je konzistentne. T.j. vsetky modely prepojenia fragmentov kodu ale aj importov v tomto java subore musia byt konzistentne.
     * @return true, ak prepojenia su konzistentne, v opacnom pripade false.
     */
    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
    public boolean isInitialized() {
        if (beginInSJ == null || endInSJ == null || beginInSJ.getOffset() > endInSJ.getOffset() || !importsBinding.isInitialized() || codeSnippets == null) {
            System.out.println("chyba v1: " + getFilePath());
            System.out.println("chyba getStartPositionJavaDocument: " + importsBinding.getStartPositionJavaDocument());
            System.out.println("chyba getEndPositionJavaDocument: " + importsBinding.getEndPositionJavaDocument());
            System.out.println("chyba getStartPositionSieveDocument: " + importsBinding.getStartPositionSieveDocument());
            System.out.println("chyba getEndPositionSieveDocument: " + importsBinding.getEndPositionSieveDocument());

            return false;
        }
        for (CodeSnippet code : codeSnippets) {
            if (!code.getCodeBinding().isInitialized()) {
                System.out.println("chyba v2: " + getFilePath());
                return false;
            }
        }
        return true;
    }
    
    /**
     * Comparator pre usporiadanie java suborov podla balikov.
     */
    //SsceIntent:Zobrazenie projekcie kodu v pomocnom subore;
    @View(aspect = ViewAspect.PRESENTATION)
    public static final Comparator<JavaFile> SORT_FILES_BY_PACKAGES = new Comparator<JavaFile>() {

        @Override
        public int compare(JavaFile o1, JavaFile o2) {
            int result;
            if (o1.getPackageName() == null && o2.getPackageName() == null) {
                return o1.getFileName().compareTo(o2.getFileName());
            } else if (o1.getPackageName() == null) {
                return -1;
            } else if (o2.getPackageName() == null) {
                return 1;
            } else if ((result = o1.getPackageName().compareTo(o2.getPackageName())) != 0) {
                return result;
            } else {
                return o1.getFileName().compareTo(o2.getFileName());
            }
        }
    };
    /**
     * Comparator pre usporiadanie java suborov podla nazvom suborov.
     */
    //SsceIntent:Zobrazenie projekcie kodu v pomocnom subore;
    @View(aspect = ViewAspect.PRESENTATION)
    public static final Comparator<JavaFile> SORT_FILES_BY_NAMES = new Comparator<JavaFile>() {

        @Override
        public int compare(JavaFile o1, JavaFile o2) {
            int result;
            if ((result = o1.getFileName().compareTo(o2.getFileName())) != 0) {
                return result;
            } else if (o1.getPackageName() == null && o2.getPackageName() == null) {
                return 0;
            } else if (o1.getPackageName() == null) {
                return -1;
            } else if (o2.getPackageName() == null) {
                return 1;
            } else {
                return o1.getPackageName().compareTo(o2.getPackageName());
            }
        }
    };

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("file(").append(filePath).append("){\n");
        builder.append("startFile=").append(beginInSJ.getOffset()).append("  endFile=").append(endInSJ.getOffset()).append("\n");

        builder.append("imports(").append(importsBinding.toString()).append(")\n");
        for (CodeSnippet code : codeSnippets) {
            builder.append("code( binding= ").append(code.getCodeBinding().toString()).append(")\n");
        }

        builder.append("}");
        return builder.toString();
    }
}
