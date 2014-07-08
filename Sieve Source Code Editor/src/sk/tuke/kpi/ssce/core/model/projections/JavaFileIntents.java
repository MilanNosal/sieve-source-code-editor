package sk.tuke.kpi.ssce.core.model.projections;

import java.util.*;
import sk.tuke.kpi.ssce.concerns.interfaces.Searchable;

/**
 * Trieda mapuje zamery na fragmenty kodu patriace do jedneho java suboru.
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Model pre mapovanie zamerov;
public class JavaFileIntents {

    //SsceIntent:Konstanta;
    private final String filePath;
    //SsceIntent:Konstanta;
    private final String fileName;
    private String packageName;
    private List<CodeIntents> codes;

    /**
     * Vytvori mapovanivanie zamerov pre jeden java subor.
     * @param filePath cesta java suboru.
     * @param fileName meno java suboru.
     */
    public JavaFileIntents(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
        initialize();
    }

    private void initialize() {
        this.codes = new ArrayList<CodeIntents>();
    }

    /**
     * Vrati vsetky mapovania zamerov na fragmenty kodu patriace do tohto java suboru.
     * @return vsetky mapovania zamerov na fragmenty kodu patriace do tohto java suboru.
     */
    public List<CodeIntents> getCodes() {
        return codes;
    }

    /**
     * Nastavi vsetky mapovania zamerov na fragmenty kodu patriace do tohto java suboru.
     * @param codes vsetky mapovania zamerov na fragmenty kodu patriace do tohto java suboru.
     */
    public void setCodes(List<CodeIntents> codes) {
        this.codes = codes;
    }

    /**
     * Najde a vrati mapovanie zamerov pre jeden fragment kodu podla offsetu.
     * @param offset offset v java subore, podla ktoreho sa ma vyhladat mapovanie zamerov pre jeden fragment kodu.
     * @return mapovanie zamerov pre jeden fragment kodu, alebo null ak sa nenajde.
     */
    public CodeIntents findForOffset(int offset) {
        CodeIntents code;
        for (int i = codes.size() - 1; i >= 0; i--) {// downto - uprednostnit mense useky pred vacsimi // vnorene elementy
            code = codes.get(i);
            if (code.getStartPositionJavaCode().getOffset() <= offset && offset <= code.getStartPositionJavaCode().getOffset() + code.getLengthJavaCode()) {
                return code;
            }
        }
        return null;
    }

    /**
     * Vrati meno tohto java suboru.
     * @return meno tohto java suboru.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Vrati meno balika, v ktorom sa nachadza tento java subor.
     * @return meno balika, v ktorom sa nachadza tento java subor.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Nastavi meno balika, v ktorom sa nachadza tento java subor.
     * @param packageName meno balika, v ktorom sa nachadza tento java subor.
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Vrati cestu tohto java suboru.
     * @return cestu tohto java suboru.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Vrati mnozinu vsetkych zamerov priradenych fragmentov kodu v tomto java subore.
     * @return mnozinu vsetkych zamerov priradenych fragmentov kodu v tomto java subore.
     */
    public Set<Searchable> getAllIntents() {
        Set<Searchable> intents = new HashSet<Searchable>();
        for (CodeIntents code : codes) {
            intents.addAll(code.getIntents());
        }
        return intents;
    }

    /**
     * Aktualizuje mapovanie zamerov pre tento java subor na zaklade noveho mapovania zamerov pre java subor file
     * @param file noveho mapovania zamerov pre tento java subor.
     */
    public void copy(JavaFileIntents file) {
        this.codes = file.codes;
        this.packageName = file.packageName;
    }

    /**
     * Zoradi mapovania zamerov na fragmenty kodu podla pozicie v tomto java subore.
     */
    public void sortCodes() {
        Collections.sort(codes);
    }
}
