package sk.tuke.kpi.ssce.core.model.projections;

import java.util.*;
import sk.tuke.kpi.ssce.concerns.interfaces.Searchable;

/**
 * Trieda sluzi ako nastroj pre mapovanie zamerov na fragmenty kodu (resp. na java subory) celeho zvoleneho projektu.
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Model pre mapovanie zamerov;
public class IntentsMapping {

    //SsceIntent:Model pre mapovanie zamerov;
    private final List<JavaFileIntents> files = new ArrayList<JavaFileIntents>();
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    private final Set<IntentsChangeListener> listeners = new HashSet<IntentsChangeListener>();
    private boolean outOfDate;

    /**
     * Vytvori mapovanie zamerov na fragmenty kodu.
     */
    public IntentsMapping() {
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
    public boolean addChangeListener(IntentsChangeListener listener) {
        return listeners.add(listener);
    }

    /**
     * Odoberie listenera pre zmeny v mapovani zamerov.
     * @param listener listener reagujuci na zmeny v mapovani zamerov na fragmenty kodu.
     * @return true, ak listener bol odobrany, inak false.
     */
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    public boolean removeChangeListener(IntentsChangeListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Vrati mnozinu vsetkych mapovanych zamerov.
     * @return mnozinu vsetkych mapovanych zamerov.
     */
    public Set<Searchable> getAllIntents() {
        Set<Searchable> intents = new HashSet<Searchable>();
        for (JavaFileIntents fileIntents : files) {
            for (CodeIntents codeIntents : fileIntents.getCodes()) {
                intents.addAll(codeIntents.getIntents());
            }
        }
        return intents;
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
    public JavaFileIntents get(int index) {
        return files.get(index);
    }

    /**
     * Nastavi nove mapovania zamerov pre java subory.
     * @param files mapovania zamerov pre java subory.
     * @return true, ak sa nastavia nove mapovania zamerov, inak false.
     */
    public boolean setFiles(List<JavaFileIntents> files) {
        try {
            this.files.clear();
//        boolean success;
//        if (success = this.files.addAll(files)) {
//            addAllJavaDocumentListeners();
//        }
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
    public JavaFileIntents updateFile(JavaFileIntents file) {
        try {
            JavaFileIntents javaFile;
            if ((javaFile = get(file.getFilePath())) != null) {
                Set<Searchable> oldAllIntents = getAllIntents();
                javaFile.copy(file);
                fireIntentsChangedEvent(prepareEvent(oldAllIntents, getAllIntents()));
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
    public JavaFileIntents updateOrInsertFile(JavaFileIntents file) {
        Set<Searchable> oldAllIntents = getAllIntents();
        try {
            JavaFileIntents javaFile;
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
            fireIntentsChangedEvent(prepareEvent(oldAllIntents, getAllIntents()));
        }
    }

//    private void removeJavaDocumentListenerFrom(JavaFile javaFile) {
//        try {
//            javaFile.getEditorCookie().openDocument().removeDocumentListener(javaDocumentListener);
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }
    /**
     * Odstrani mapovanie zamerov pre zvoleny java subor.
     * @param file mapovanie zamerov pre zvoleny java subor.
     * @return odstranene mapovanie zamerov pre zvoleny java subor.
     */
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    public JavaFileIntents deleteFile(JavaFileIntents file) {

        try {
            JavaFileIntents javaFile;
            if ((javaFile = get(file.getFilePath())) != null) {

                Set<Searchable> oldAllIntents = getAllIntents();

                files.remove(javaFile);

                fireIntentsChangedEvent(prepareEvent(oldAllIntents, getAllIntents()));

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
        for (JavaFileIntents jF : files) {
            if (jF.getFilePath().equals(javaFilePath)) {
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
    public JavaFileIntents get(String javaFilePath) {
        for (JavaFileIntents jF : files) {
            if (jF.getFilePath().equals(javaFilePath)) {
                return jF;
            }
        }
        return null;
    }

    /**
     * Vrati mapovanie zamerov pre java subor, ktory nasleduje v tomto mapovani za suborom s cestou javaFilePath.
     * @param javaFilePath cesta java suboru.
     * @return mapovanie zamerov pre nasledujuci java subor, ak toto mapovanie ho obsahuje, inak null.
     */
    public JavaFileIntents getNext(String javaFilePath) {
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
    public JavaFileIntents getPrevious(String javaFilePath) {
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
    public JavaFileIntents insertFile(JavaFileIntents file) {
        try {
            if (get(file.getFilePath()) != null) {
                return null;
            }

            Set<Searchable> oldAllIntents = getAllIntents();

            if (files.add(file)) {
//            Collections.sort(files, JavaFileIntents.SORT_BY_PACKAGES);


                fireIntentsChangedEvent(prepareEvent(oldAllIntents, getAllIntents()));
                return file;
            }
            return null;
        } finally {
            this.outOfDate = false;
        }
    }

    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    private void fireIntentsChangedEvent(IntentsChangedEvent event) {
        this.outOfDate = false;
        if (event == null) {
            return;
        }
        for (IntentsChangeListener listener : listeners) {
            listener.intentsChanged(event);
        }
    }

    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    private IntentsChangedEvent prepareEvent(Set<Searchable> oldAllIntents, Set<Searchable> newAllIntents) {
        Set<Searchable> removedIntents = new HashSet<Searchable>(oldAllIntents);
        removedIntents.removeAll(newAllIntents);

        Set<Searchable> newIntents = new HashSet<Searchable>(newAllIntents);
        newIntents.removeAll(oldAllIntents);
//        if (newIntents.isEmpty() && removedIntents.isEmpty()) {
//            return null;
//        }
        return new IntentsChangedEvent(newAllIntents, newIntents, removedIntents);
    }

    /**
     * Event pre zmenu v mapovani zamerov na fragmenty kodu.
     */
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    public static class IntentsChangedEvent {

//        private final File file;
        private final Set<Searchable> newIntents;
        private final Set<Searchable> removedIntents;
        private final Set<Searchable> allIntents;

        /**
         * Vytvori event pre zmenu v mapovani zamerov na fragmenty kodu.
         * @param allIntents nova mnozina vsetkych zamerov.
         * @param newIntents  mnozina novo pridanych zamerov.
         * @param removedIntents mnozina odobratych zamerov.
         */
        public IntentsChangedEvent(Set<Searchable> allIntents, Set<Searchable> newIntents, Set<Searchable> removedIntents) {
            this.newIntents = newIntents;
            this.removedIntents = removedIntents;
            this.allIntents = allIntents;
        }

        /**
         * Overi ci doslo k zmene mnozny mapovanych zamerov.
         * @return true, ak doslo k zmene celkovej mnoziny zamerov, inak false.
         */
        public boolean isIntentsSetChanged() {
            return !(newIntents.isEmpty() && removedIntents.isEmpty());
        }

        /**
         * Vrati mnozinu novo pridanych zamerov.
         * @return mnozinu novo pridanych zamerov.
         */
        public Set<Searchable> getNewIntents() {
            return newIntents;
        }

        /**
         * Vrati mnozinu odobratych zamerov.
         * @return mnozinu odobratych zamerov.
         */
        public Set<Searchable> getRemovedIntents() {
            return removedIntents;
        }

        /**
         * Vrati novu mnozinu vsetkych zamerov.
         * @return novu mnozinu vsetkych zamerov.
         */
        public Set<Searchable> getAllIntents() {
            return allIntents;
        }
    }

    /**
     * Listener reagujuci na zmeny v mapovani zamerov na fragmenty kodu.
     */
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    public static interface IntentsChangeListener extends EventListener {

        /**
         * Metoda je volana ak doslo k zmene v mapovani zamerov na fragmenty kodu.
         * @param event event
         */
        public void intentsChanged(IntentsMapping.IntentsChangedEvent event);
    }
}
