package sk.tuke.kpi.ssce.core.model.availableprojections;

import java.util.*;
import sk.tuke.kpi.ssce.annotations.concerns.AvailableProjectionsChange;
import sk.tuke.kpi.ssce.annotations.concerns.Disposal;
import sk.tuke.kpi.ssce.annotations.concerns.Model;
import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;

/**
 * Trieda sluzi ako nastroj pre mapovanie zamerov na fragmenty kodu (resp. na java subory) celeho zvoleneho projektu.
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Model pre mapovanie zamerov;
@Model(model = RepresentationOf.PROJECTION)
public class ProjectionsModel {

    //SsceIntent:Model pre mapovanie zamerov;
    private final List<JavaFileConcerns> files = new ArrayList<JavaFileConcerns>();
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    private final Set<ConcernsChangeListener> listeners = new HashSet<ConcernsChangeListener>();
    private boolean outOfDate;

    /**
     * Vytvori mapovanie zamerov na fragmenty kodu.
     */
    public ProjectionsModel() {
        outOfDate = true;
    }

    /**
     * Overi ci mapovanie nie je zastarale.
     * @return true, ak je zastarale, inak false.
     */
    public boolean isOutOfDate() {
        return outOfDate;
    }

    /**
     * Nastavi mapovanie za zastarale.
     */
    public void setOutOfDate() {
        this.outOfDate = true;
    }

    /**
     * Prida listenera pre zmeny v mapovani zamerov.
     * @param listener listener reagujuci na zmeny v mapovani zamerov na fragmenty kodu.
     * @return true, ak listener bol pridany, inak false.
     */
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    public boolean addChangeListener(ConcernsChangeListener listener) {
        return listeners.add(listener);
    }

    /**
     * Odoberie listenera pre zmeny v mapovani zamerov.
     * @param listener listener reagujuci na zmeny v mapovani zamerov na fragmenty kodu.
     * @return true, ak listener bol odobrany, inak false.
     */
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    public boolean removeChangeListener(ConcernsChangeListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Vrati mnozinu vsetkych mapovanych zamerov.
     * @return mnozinu vsetkych mapovanych zamerov.
     */
    public Set<Concern> getAllConcerns() {
        Set<Concern> concerns = new HashSet<Concern>();
        for (JavaFileConcerns fileConcerns : files) {
            for (CodeSnippetConcerns codeConcerns : fileConcerns.getCodes()) {
                concerns.addAll(codeConcerns.getConcerns());
            }
        }
        return concerns;
    }

    /**
     * Vrati velkost, pocet mapovanych java suborov.
     * @return velkost, pocet mapovanych java suborov.
     */
    public int size() {
        return files.size();
    }

    /**
     * Vrati mapovanie zamerov pre jeden java subor.
     * @param index index java suboru.
     * @return mapovanie zamerov pre jeden java subor.
     */
    public JavaFileConcerns get(int index) {
        return files.get(index);
    }

    /**
     * Nastavi nove mapovania zamerov pre java subory.
     * @param files mapovania zamerov pre java subory.
     * @return true, ak sa nastavia nove mapovania zamerov, inak false.
     */
    public boolean setFiles(List<JavaFileConcerns> files) {
        try {
            this.files.clear();
            this.outOfDate = false;
            return this.files.addAll(files);
        } finally {
            this.outOfDate = false;
        }
    }

    /**
     * Odstrani cele mapovanie zamerov.
     */
    public void clearFiles() {
        // XXX: check for usages
//        removeAllJavaDocumentListeners();
        try {
            this.files.clear();
        } finally {
            this.outOfDate = false;
        }
    }

    /**
     * Aktualizuje mapovanie zamerov pre zvoleny java subor.
     * @param file nove mapovanie zamerov pre zvoleny java subor.
     * @return ak sa najde stare mapovanie zamerov pre zvoleny java subor, tak vrati nove mapovanie, inak null.
     */
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    @AvailableProjectionsChange(propagation = false)
    public JavaFileConcerns updateFile(JavaFileConcerns file) {
        try {
            JavaFileConcerns javaFile;
            if ((javaFile = get(file.getFilePath())) != null) {
                Set<Concern> oldAllConcerns = getAllConcerns();
                javaFile.copy(file);
                fireConcernsConfigurationChangedEvent(prepareEvent(oldAllConcerns, getAllConcerns()));
            }
            return javaFile;
        } finally {
            this.outOfDate = false;
        }
    }

    /**
     * Aktualizuje alebo vytvori nove mapovanie zamerov pre zvoleny java subor.
     * @param file nove mapovanie zamerov pre zvoleny java subor.
     * @return nove mapovanie zamerov pre zvoleny java subor, alebo null, ak sa nepodari vykovat akciu.
     */
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    @AvailableProjectionsChange(propagation = false)
    public JavaFileConcerns updateOrInsertFile(JavaFileConcerns file) {
        Set<Concern> oldAllIntents = getAllConcerns();
        try {
            JavaFileConcerns javaFile;
            if ((javaFile = get(file.getFilePath())) != null) {
                javaFile.copy(file);
                return javaFile;
            } else {
                if (files.add(file)) {
                    return file;
                }
                return null;
            }

        } finally {
            this.outOfDate = false;
            fireConcernsConfigurationChangedEvent(prepareEvent(oldAllIntents, getAllConcerns()));
        }
    }

    /**
     * Odstrani mapovanie zamerov pre zvoleny java subor.
     * @param file mapovanie zamerov pre zvoleny java subor.
     * @return odstranene mapovanie zamerov pre zvoleny java subor.
     */
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    @AvailableProjectionsChange(propagation = false)
    public JavaFileConcerns deleteFile(JavaFileConcerns file) {
        try {
            JavaFileConcerns javaFile;
            if ((javaFile = get(file.getFilePath())) != null) {
                Set<Concern> oldAllConcerns = getAllConcerns();
                files.remove(javaFile);
                fireConcernsConfigurationChangedEvent(prepareEvent(oldAllConcerns, getAllConcerns()));
                return javaFile;
            }
            return null;
        } finally {
            this.outOfDate = false;
        }
    }

    /**
     * Overi ci toto mapovanie obsahuje mapovanie zamerov pre java subor s cestou javaFilePath.
     * @param javaFilePath cesta na java subor
     * @return true, ak obsahuje, inak false.
     */
    public boolean contains(String javaFilePath) {
        for (JavaFileConcerns jFC : files) {
            if (jFC.getFilePath().equals(javaFilePath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vrati mapovanie zamerov pre java subor s cestou javaFilePath.
     * @param javaFilePath cesta java suboru.
     * @return mapovanie zamerov pre java subor, ak toto mapovanie ho obsahuje, inak null.
     */
    public JavaFileConcerns get(String javaFilePath) {
        for (JavaFileConcerns jFC : files) {
            if (jFC.getFilePath().equals(javaFilePath)) {
                return jFC;
            }
        }
        return null;
    }

    /**
     * Vrati mapovanie zamerov pre java subor, ktory nasleduje v tomto mapovani za suborom s cestou javaFilePath.
     * @param javaFilePath cesta java suboru.
     * @return mapovanie zamerov pre nasledujuci java subor, ak toto mapovanie ho obsahuje, inak null.
     */
    public JavaFileConcerns getNext(String javaFilePath) {
        for (int i = 1; i < files.size(); i++) {
            if (files.get(i - 1).getFilePath().equals(javaFilePath)) {
                return files.get(i);
            }
        }
        return null;
    }

    /**
     * Vrati mapovanie zamerov pre java subor, ktory predchadza v tomto mapovani pred suborom s cestou javaFilePath.
     * @param javaFilePath cesta java suboru.
     * @return mapovanie zamerov pre predchadzajuci java subor, ak toto mapovanie ho obsahuje, inak null.
     */
    public JavaFileConcerns getPrevious(String javaFilePath) {
        for (int i = 1; i < files.size(); i++) {
            if (files.get(i).getFilePath().equals(javaFilePath)) {
                return files.get(i - 1);
            }
        }
        return null;
    }

    /**
     * Vlozi nove mapovanie zamerov pre zvolny java subor.
     * @param file mapovanie zamerov pre java subor.
     * @return mapovanie zamerov pre java subor, ak akcia prebehla uspesne, inak null.
     */
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    @AvailableProjectionsChange(propagation = false)
    public JavaFileConcerns insertFile(JavaFileConcerns file) {
        try {
            if (get(file.getFilePath()) != null) {
                return null;
            }
            Set<Concern> oldAllConcerns = getAllConcerns();

            if (files.add(file)) {
//            Collections.sort(files, JavaFileIntents.SORT_BY_PACKAGES);
                fireConcernsConfigurationChangedEvent(prepareEvent(oldAllConcerns, getAllConcerns()));
                return file;
            }
            return null;
        } finally {
            this.outOfDate = false;
        }
    }

    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    @AvailableProjectionsChange(propagation = true)
    private void fireConcernsConfigurationChangedEvent(ConcernsChangedEvent event) {
        this.outOfDate = false;
        if (event == null) {
            return;
        }
        for (ConcernsChangeListener listener : listeners) {
            listener.concernsChanged(event);
        }
    }

    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    @AvailableProjectionsChange(propagation = true)
    private ConcernsChangedEvent prepareEvent(Set<Concern> oldAllConcerns, Set<Concern> newAllConcerns) {
        Set<Concern> removedConcerns = new HashSet<Concern>(oldAllConcerns);
        removedConcerns.removeAll(newAllConcerns);

        Set<Concern> newConcerns = new HashSet<Concern>(newAllConcerns);
        newConcerns.removeAll(oldAllConcerns);
//        if (newIntents.isEmpty() && removedIntents.isEmpty()) {
//            return null;
//        }
        return new ConcernsChangedEvent(newAllConcerns, newConcerns, removedConcerns);
    }
    
    @Disposal
    public void dispose() {
        this.listeners.clear();
    }

    /**
     * Event pre zmenu v mapovani zamerov na fragmenty kodu.
     */
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    @AvailableProjectionsChange(propagation = true)
    public static class ConcernsChangedEvent {

//        private final File file;
        private final Set<Concern> newConcerns;
        private final Set<Concern> removedConcerns;
        private final Set<Concern> allConcerns;

        /**
         * Vytvori event pre zmenu v mapovani zamerov na fragmenty kodu.
         * @param allConcerns nova mnozina vsetkych zamerov.
         * @param newConcerns  mnozina novo pridanych zamerov.
         * @param removedConcerns mnozina odobratych zamerov.
         */
        public ConcernsChangedEvent(Set<Concern> allConcerns, Set<Concern> newConcerns, Set<Concern> removedConcerns) {
            this.newConcerns = newConcerns;
            this.removedConcerns = removedConcerns;
            this.allConcerns = allConcerns;
        }

        /**
         * Overi ci doslo k zmene mnozny mapovanych zamerov.
         * @return true, ak doslo k zmene celkovej mnoziny zamerov, inak false.
         */
        public boolean isConcernsSetChanged() {
            return !(newConcerns.isEmpty() && removedConcerns.isEmpty());
        }

        /**
         * Vrati mnozinu novo pridanych zamerov.
         * @return mnozinu novo pridanych zamerov.
         */
        public Set<Concern> getNewConcerns() {
            return newConcerns;
        }

        /**
         * Vrati mnozinu odobratych zamerov.
         * @return mnozinu odobratych zamerov.
         */
        public Set<Concern> getRemovedConcerns() {
            return removedConcerns;
        }

        /**
         * Vrati novu mnozinu vsetkych zamerov.
         * @return novu mnozinu vsetkych zamerov.
         */
        public Set<Concern> getAllConcerns() {
            return allConcerns;
        }
    }

    /**
     * Listener reagujuci na zmeny v mapovani zamerov na fragmenty kodu.
     */
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    @AvailableProjectionsChange(propagation = true)
    public static interface ConcernsChangeListener extends EventListener {

        /**
         * Metoda je volana ak doslo k zmene v mapovani zamerov na fragmenty kodu.
         * @param event event
         */
        public void concernsChanged(ProjectionsModel.ConcernsChangedEvent event);
    }
}
