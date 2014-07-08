package sk.tuke.kpi.ssce.core.model.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.cookies.EditorCookie;

/**
 * Trieda predstavuje celkovy model prepojenia vsetkych java suborov s pomocnym suborom .sj.
 * @author Matej Nosal
 */
//SsceIntent:Model pre synchronizaciu kodu;
public class Model {

    //SsceIntent:Praca s pomocnym suborom;
    private EditorCookie editorCookieSieveDocument;
//    private DocumentListener javaDocumentListener;
    private final List<JavaFile> files = new ArrayList<JavaFile>();

    /**
     * Vytvori celkovy model prepojenia vsetkych java suborov s pomocnym suborom .sj.
     */
    public Model() {
    }

    /**
     * Vrati editorCookie pre pomocny subor .sj.
     * @return editorCookie pre pomocny subor .sj.
     */
    public EditorCookie getEditorCookieSieveDocument() {
        return editorCookieSieveDocument;
    }

    /**
     * Nastavi editorCookie pre pomocny subor .sj.
     * @param editorCookieSieveDocument editorCookie pre pomocny subor .sj.
     */
    public void setEditorCookieSieveDocument(EditorCookie editorCookieSieveDocument) {
        this.editorCookieSieveDocument = editorCookieSieveDocument;
    }

    /**
     * Vrati velkost (pocet) modelovanych java suborov.
     * @return velkost (pocet) modelovanych java suborov.
     */
    public int size() {
        return files.size();
    }

    /**
     * Vrati modelovany java subor s indexom index.
     * @param index index modelovanyho java suboru.
     * @return modelovany java subor s indexom index.
     */
    public JavaFile get(int index) {
        return files.get(index);
    }

    /**
     * Nastavi nove modelovane java subory tomuto modelu.
     * @param files nove modelovane java subory.
     * @return true, ak sa nastavia nove modelovane java subory, inak false.
     */
    public boolean setFiles(List<JavaFile> files) {
        this.files.clear();
        return this.files.addAll(files);
    }

    /**
     * Odstrani vsetky modelovane java subory z tohto modelu.
     */
    public void clearFiles() {
        this.files.clear();
    }

    /**
     * Aktualizuje modelovany java subor.
     * @param file novy aktualizovany model java suboru.
     * @return aktualizovany model java suboru.
     */
    public JavaFile updateFile(JavaFile file) {
        JavaFile javaFile;
        if ((javaFile = get(file.getFilePath())) != null) {
            javaFile.copy(file);
        }
        return javaFile;
    }

     /**
     * Odstrani modelovany java subor z tohto modelu.
     * @param file model java suboru, ktory ma byt odstraneny z tohto modelu.
     * @return odstraneny model java suboru.
     */
    public JavaFile deleteFile(JavaFile file) {
        JavaFile javaFile;
        if ((javaFile = get(file.getFilePath())) != null) {
            files.remove(javaFile);
//            removeJavaDocumentListenerFrom(javaFile);
            return javaFile;
        }
        return null;
    }

    /**
     * Otestuje ci sa v tomto modely nachadza model java suboru s cestou javaFilePath.
     * @param javaFilePath cesta java suboru.
     * @return true, ak model obsahuje java subor, inak false.
     */
    public boolean contains(String javaFilePath) {
        for (JavaFile jF : files) {
            if (jF.getFilePath().equals(javaFilePath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vrati modelovany java subor so suborovou cestou javaFilePath.
     * @param javaFilePath suborova cesta java suboru
     * @return modelovany java subor so suborovou cestou javaFilePath.
     */
    public JavaFile get(String javaFilePath) {
        for (JavaFile jF : files) {
            if (jF.getFilePath().equals(javaFilePath)) {
                return jF;
            }
        }
        return null;
    }

    /**
     * Vrati modelovany java subor, ktory nasleduje za java suborom s cestou javaFilePath.
     * @param javaFilePath suborova cesta java suboru
     * @return modelovany java subor, ktory naleduje za java suborom s cestou javaFilePath, alebo null ak neexistuje.
     */
    public JavaFile getNext(String javaFilePath) {
        for (int i = 1; i < files.size(); i++) {
            if (files.get(i - 1).getFilePath().equals(javaFilePath)) {
                return files.get(i);
            }
        }
        return null;
    }

    /**
     * Vrati modelovany java subor, ktory predchadza java subor s cestou javaFilePath.
     * @param javaFilePath suborova cesta java suboru
     * @return modelovany java subor, ktory predchadza java subor s cestou javaFilePath, alebo null ak neexistuje.
     */
    public JavaFile getPrevious(String javaFilePath) {
        for (int i = 1; i < files.size(); i++) {
            if (files.get(i).getFilePath().equals(javaFilePath)) {
                return files.get(i - 1);
            }
        }
        return null;
    }

    /**
     * Vlozi do tohto modelu novy java subor.
     * @param file model java subor.
     * @return model java subor, alebo null ak sa nepodari vlozit novy java subor.
     */
    public JavaFile insertFile(JavaFile file) {
        if (get(file.getFilePath()) != null) {
            return null;
        }
        if (files.add(file)) {
            Collections.sort(files, JavaFile.SORT_BY_PACKAGES);
            return file;
        }
        return null;
    }

    /**
     * Overi ci prepojenie vsetkych usekov v tomto modeli je konzistentne. T.j. je mozne realizovat synchroniziaciu vsetkych ueskov v modeli (usekov v pomocnom subore a usekov java suborch).
     * @return true, ak vsetky prepojenia v tomto modeli su konzistentne, v opacnom pripade false.
     */
    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
    public boolean isConsistent() {
        for (JavaFile file : files) {
            if (!file.isConsistent()) {
                return false;
            }
//            System.out.println("Java File (ok): " + file.getFilePath());
        }
        return true;
    }
//    public JavaFile getCode(Position ){
//        
//    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("Model\n");
        for (JavaFile file : files) {
            builder.append(file.toString()).append("\n");
        }
        return builder.toString();
    }

    /**
     * Vrati modelovany java subor podla offsetu v pomocnom subore .sj.
     * @param offset offset v pomocnom subore .sj.
     * @return modelovany java subor podla offsetu v pomocnom subore .sj.
     */
    public JavaFile getByOffset(int offset) { // in sieve document
        for (JavaFile jF : files) {
            if (jF.getStartFile() <= offset && offset <= jF.getEndFile()) {
                return jF;
            }
        }
        return null;
    }
}
