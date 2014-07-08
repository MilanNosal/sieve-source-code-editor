package sk.tuke.kpi.ssce.core.model.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.openide.cookies.EditorCookie;

/**
 * Trieda modeluje preopjenie jedeneho java suboru s pomocnym suborom .sj.
 * @author Matej Nosal
 */
//SsceIntent:Model pre synchronizaciu kodu;
public class JavaFile {

    private Position startFile; // in sieve document
    private Position endFile;
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
    private Imports allImports;
    private Imports necesaryImports;
    private List<Code> codes;

    /**
     * Vytvori model prepojenia jedeneho java suboru s pomocnym suborom .sj.
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

    private void initialize() {
        this.allImports = new Imports();
        this.necesaryImports = new Imports();
        this.codes = new ArrayList<Code>();
    }

    /**
     * Vrati model prepojenia fragmentu kodu podla offsetu v pomocnom subore .sj, ktory patri do tohto java suboru.
     * @param offset offset v pomocnom subore .sj
     * @return model prepojenia fragmentu kodu, ak na offsete nie je ziaden fragment tak null.
     */
    public Code getBySieveOffset(int offset) {
        for (Code code : codes) {
            if (code.getCodeBinding().isConsistent() && code.getCodeBinding().getStartPositionSieveDocument() <= offset && offset <= code.getCodeBinding().getEndPositionSieveDocument() + 1) {
                return code;
            }
        }
        return null;
    }

    /**
     * Vrati zaciatocnu poziciu (offset) tohto java suboru v pomocnom subore .sj.
     * @return zaciatocnu poziciu (offset) tohto java suboru v pomocnom subore .sj.
     */
    public int getEndFile() {
        if (endFile == null) {
            return -1;
        }
        return endFile.getOffset() + 1;
    }

    /**
     * Nastavi koncovu poziciu tohto java suboru v pomocnom subore .sj.
     * @param doc dokument pre pomocny subor .sj.
     * @param endFile koncova pozicia tohto java suboru v pomocnom subore .sj.
     * @throws BadLocationException
     */
    //SsceIntent:Praca s pomocnym suborom;
    public void setEndFile(BaseDocument doc, int endFile) throws BadLocationException {
        this.endFile = doc.createPosition(endFile - 1);
    }

    /**
     * Vrati koncovu poziciu (offset) tohto java suboru v pomocnom subore .sj.
     * @return koncovu poziciu (offset) tohto java suboru v pomocnom subore .sj.
     */
    public int getStartFile() {
        if (startFile == null) {
            return -1;
        }
        return startFile.getOffset() - 1;
    }

    /**
     * Nastavi zaciatocnu poziciu tohto java suboru v pomocnom subore .sj.
     * @param doc dokument pre pomocny subor .sj.
     * @param startFile zaciatocnu pozicia tohto java suboru v pomocnom subore .sj.
     * @throws BadLocationException
     */
    //SsceIntent:Praca s pomocnym suborom;
    public void setStartFile(BaseDocument doc, int startFile) throws BadLocationException {
        this.startFile = doc.createPosition(startFile + 1);
    }

    /**
     * Vrati model importov pre tento java subor.
     * @return model importov pre tento java subor.
     */
    public Imports getAllImports() {
        return allImports;
    }

    /**
     * Nastavi model importov pre tento java subor.
     * @param allImports model importov pre tento java subor.
     */
    public void setAllImports(Imports allImports) {
        this.allImports = allImports;
    }

    /**
     * Vrati zoznam vsetkych modelovanych fragmentov kodu.
     * @return zoznam vsetkych modelovanych fragmentov kodu.
     */
    public List<Code> getCodes() {
        return codes;
    }

    /**
     * Nastavi zoznam vsetkych modelovanych fragmentov kodu.
     * @param codes zoznam vsetkych modelovanych fragmentov kodu.
     */
    public void setCodes(List<Code> codes) {
        this.codes = codes;
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
    public Imports getNecessaryImports() {
//        return allImports;
        return necesaryImports;
    }

    /**
     * Nastavi importy, ktore su nevyhnutne pre fragmenty kodu, ktore tento java subor modeluje.
     * @param necesaryImports importy, ktore su nevyhnutne pre fragmenty kodu, ktore tento java subor modeluje.
     */
    public void setNecesaryImports(Imports necesaryImports) {
        this.necesaryImports = necesaryImports;
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
    public String getStartText() {
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
    public String getEndText() {
        return "}\n";
    }

    /**
     * Tento model java suboru sa aktualicuje na zaklade java suboru file.
     * @param file java subor, podla ktoreho bude tento java subor aktualizovany.
     */
    public void copy(JavaFile file) {
        this.allImports = file.allImports;
        this.necesaryImports = file.necesaryImports;
        this.importsBinding = file.importsBinding;
        this.codes = file.codes;
        this.packageName = file.packageName;
    }

    /**
     * Overi ci prepojenie vsetkych usekov je konzistentne. T.j. vsetky modely prepojenia fragmentov kodu ale aj importov v tomto java subore musia byt konzistentne.
     * @return true, ak prepojenia su konzistentne, v opacnom pripade false.
     */
    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
    public boolean isConsistent() {
        if (startFile == null || endFile == null || startFile.getOffset() > endFile.getOffset() || !importsBinding.isConsistent() || codes == null) {
            System.out.println("chyba v1: " + getFilePath());
            System.out.println("chyba getStartPositionJavaDocument: " + importsBinding.getStartPositionJavaDocument());
            System.out.println("chyba getEndPositionJavaDocument: " + importsBinding.getEndPositionJavaDocument());
            System.out.println("chyba getStartPositionSieveDocument: " + importsBinding.getStartPositionSieveDocument());
            System.out.println("chyba getEndPositionSieveDocument: " + importsBinding.getEndPositionSieveDocument());

            return false;
        }
        for (Code code : codes) {
            if (!code.getCodeBinding().isConsistent()) {
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
    public static final Comparator<JavaFile> SORT_BY_PACKAGES = new Comparator<JavaFile>() {

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
    public static final Comparator<JavaFile> SORT_BY_FILES = new Comparator<JavaFile>() {

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
        builder.append("startFile=").append(startFile.getOffset()).append("  endFile=").append(endFile.getOffset()).append("\n");

        builder.append("imports(").append(importsBinding.toString()).append(")\n");
        for (Code code : codes) {
            builder.append("code( binding= ").append(code.getCodeBinding().toString()).append(")\n");
        }

        builder.append("}");
        return builder.toString();
    }
}
