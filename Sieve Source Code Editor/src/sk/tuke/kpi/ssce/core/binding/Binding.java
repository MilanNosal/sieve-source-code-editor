package sk.tuke.kpi.ssce.core.binding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.openide.awt.StatusDisplayer;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import sk.tuke.kpi.ssce.annotations.concerns.PostProcessing;
import sk.tuke.kpi.ssce.annotations.concerns.SievedDocument;
import sk.tuke.kpi.ssce.annotations.concerns.SourceCodeSieving;
import sk.tuke.kpi.ssce.annotations.concerns.Synchronization;
import sk.tuke.kpi.ssce.annotations.concerns.View;
import sk.tuke.kpi.ssce.annotations.concerns.enums.Direction;
import sk.tuke.kpi.ssce.annotations.concerns.enums.PostProcessingType;
import sk.tuke.kpi.ssce.annotations.concerns.enums.ViewAspect;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;
import sk.tuke.kpi.ssce.core.model.creators.ProjectionsModelCreator;
import sk.tuke.kpi.ssce.core.model.creators.ViewModelCreator;
import sk.tuke.kpi.ssce.core.model.view.CodeSnippet;
import sk.tuke.kpi.ssce.core.model.view.importshandling.Imports;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.core.model.view.ViewModel;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.GuardingRequest;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.interfaces.GuardingProvider;

/**
 * Trieda predstavuje nastroj pre realizovanie prepojenia medzi java subormi a
 * pomocnym suborom .sj.
 *
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Praca s pomocnym suborom;Prepojenie java suborov s pomocnym suborom .sj;
@Synchronization
public class Binding<T extends Concern> {

    /**
     * Enum typ pre realizaciu aktualizacie pomocneho suboru .sj.
     */
    @Synchronization
    public enum UpdateModelAction {

        /**
         * Vkladanie
         */
        INSERT,
        /**
         * Odstranenie
         */
        DELETE,
        /**
         * Aktualizacia
         */
        UPDATE
    }
    //SsceIntent:Praca s java suborom;
    @Synchronization(direction = Direction.JAVATOSJ)
    private final ViewModelCreator<T> viewModelCreator;

    private final ProjectionsModelCreator<T> projectionsModelCreator;

    @PostProcessing(type = PostProcessingType.GUARDING)
    private final List<GuardingProvider> guardingProviders;

    /**
     * Vytvori nastroj pre realizovanie prepojenia medzi java subormi a pomocnym
     * suborom .sj.
     */
    public Binding(ViewModelCreator<T> viewModelCreator, ProjectionsModelCreator<T> projectionsModelCreator,
            List<GuardingProvider> guardingProviders) {
        this.viewModelCreator = viewModelCreator;
        this.projectionsModelCreator = projectionsModelCreator;
        this.guardingProviders = guardingProviders;
    }

    /**
     * Vrati nastroj pre pracu s java subormi.
     *
     * @return nastroj pre pracu s java subormi.
     */
    //SsceIntent:Praca s java suborom;
    @Synchronization(direction = Direction.JAVATOSJ)
    public ViewModelCreator<T> getViewModelCreator() {
        return viewModelCreator;
    }

    /**
     * Vrati nastroj pre pracu s java subormi.
     *
     * @return nastroj pre pracu s java subormi.
     */
    //SsceIntent:Praca s java suborom;
    public ProjectionsModelCreator<T> getProjectionsModelCreator() {
        return projectionsModelCreator;
    }

    /**
     * Vyplni pomocny subor .sj fragmentmi kodu modelovanymi modelom. Model sa
     * po vykovani stava konzistentnym.
     *
     * @param model model prepojenia java suborov s pomocnym suborom .sj.
     * @return true, ak pomocny subor je uspesne vyplneny.
     */
    //SsceIntent:Zobrazenie projekcie kodu v pomocnom subore;Zobrazenie importov v pomocnom subore;Zobrazenie fragmentu kodu v pomocnom subore;Model pre synchronizaciu kodu;
    @Synchronization(direction = Direction.JAVATOSJ)
    @SourceCodeSieving
    @SievedDocument
    @View(aspect = ViewAspect.PRESENTATION)
    @PostProcessing(type = PostProcessingType.GUARDING)
    public boolean loadSieveDocument(final ViewModel<T> model) {
        // toto predstavuje vysledny sj dokument
        final StringBuilder buffer = new StringBuilder();

        JavaFile<T> javaFile;

        // zoznam vsetkych sledovanych pozicii v sj subore, sklada ich z fileov a code snipettov
        List<SnippetToBeUpdated> updates = new ArrayList<SnippetToBeUpdated>();

        SnippetToBeUpdated file;
        SnippetToBeUpdated element;
        try {
            for (int i = 0; i < model.size(); i++) {
                javaFile = model.getFileAt(i);
                BaseDocument doc = (BaseDocument) javaFile.getEditorCookie().openDocument();
                doc.readLock();
                try {
                    file = new SnippetToBeUpdated();
                    file.setJavaFile(javaFile);
                    file.setStart(buffer.length());

                    buffer.append(javaFile.getStartTextForSJDoc());

                    element = new SnippetToBeUpdated();
                    element.setImports(javaFile);
                    element.setStart(buffer.length());
                    buffer.append(javaFile.getNecessaryImports().toString());
//                    if (element.start == buffer.length()) {
                    buffer.append("\n");
//                    }
                    element.setEnd(buffer.length() - 1);
                    updates.add(element);

//                    buffer.append("\n");
                    for (CodeSnippet<T> c : javaFile.getCodeSnippets()) {
                        element = new SnippetToBeUpdated();
                        element.setCode(c);
                        buffer.append(c.getStartTextForSJDoc());
                        element.setStart(buffer.length());
                        buffer.append(doc.getText(c.getCodeBinding().getStartPositionJavaDocument(), c.getCodeBinding().getLengthBindingAreaJavaDocument()));

                        element.setEnd(buffer.length() - 1);
                        updates.add(element);

                        buffer.append(c.getEndTextForSJDoc());
                    }

                    buffer.append(javaFile.getEndTextForSJDoc());

                    file.setEnd(buffer.length() - 1);
                    updates.add(file);

                } finally {
                    doc.readUnlock();
                }
            }

            // tu sa vytovri skutocny sj dokument
            BaseDocument sieveDocument = ((BaseDocument) model.getEditorCookieSieveDocument().openDocument());

            // remember caret position and calculate new caret positions
            JEditorPane[] panes = model.getEditorCookieSieveDocument().getOpenedPanes();

            // toto vyzera ako by molhlo byt aj viac sj dokumentov otvorenych naraz
            Integer[] carets = null;
            if (panes != null) {

                int lengthOldFile = sieveDocument.getLength();
                int lengthNewFile = buffer.length();

                carets = new Integer[panes.length];
                for (int i = 0; i < panes.length; i++) {
                    int actualCaret = panes[i].getCaretPosition();
                    if (lengthOldFile == 0) {
                        carets[i] = 0;
                    } else if (0 <= actualCaret && actualCaret <= lengthOldFile) {
                        float ratio = ((float) (actualCaret)) / lengthOldFile;
                        carets[i] = (int) (ratio * lengthNewFile);
                    } else {
                        carets[i] = null;
                    }
                }
            }
            // end calculating caret positions

            sieveDocument.extWriteLock();
            try {
                NbDocument.unmarkGuarded((StyledDocument) sieveDocument, -100, Integer.MAX_VALUE);
                sieveDocument.replace(0, sieveDocument.getLength(), buffer.toString(), null);

                updatePositions(updates, sieveDocument); // make update positions in SieveDocument=> model is corect, fully defined

//                System.out.println(model.toString());
                if (!model.isInitialized()) {
                    throw new RuntimeException("Model is not initialized!");
                }
                //TODO: dokoncit
                processGuardingRequests((StyledDocument) sieveDocument, model);
            } finally {
                sieveDocument.extWriteUnlock();
            }

            //model.getEditorCookieSieveDocument().saveDocument();
            // setting remembered caret positions
            if (carets != null) {
                for (int i = 0; i < panes.length; i++) {
                    if (carets[i] != null && carets[i] >= 0 && carets[i] <= sieveDocument.getLength()) {
                        panes[i].setCaretPosition(carets[i]);
                    }
                }
            }
            return true;

        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    /**
     * Metoda aktualizuje obsah pomocneho suboru .sj a modelu na zaklade zmeny v
     * javaFile.
     *
     * @param action typ aktualizacie.
     * @param model model pre prepojenie java suborov s pomocnym suborom .sj.
     * @param javaFile zmeneny java subor.
     * @return true, ak pomocny subor a model je uspesne aktualizovany.
     */
    //SsceIntent:Zobrazenie projekcie kodu v pomocnom subore;Zobrazenie importov v pomocnom subore;Zobrazenie fragmentu kodu v pomocnom subore;Model pre synchronizaciu kodu;
    @Synchronization(direction = Direction.JAVATOSJ)
    public boolean updateSieveDocument(UpdateModelAction action, ViewModel<T> model, JavaFile<T> javaFile) {
        switch (action) {
            case UPDATE:
                return updateSieveDocument_UpdateAction(model, javaFile);
            case INSERT:
                return updateSieveDocument_InsertAction(model, javaFile);
            case DELETE:
                return updateSieveDocument_DeleteAction(model, javaFile);
            default:
                return false;
        }
    }

    //SsceIntent:Zobrazenie projekcie kodu v pomocnom subore;Zobrazenie importov v pomocnom subore;Zobrazenie fragmentu kodu v pomocnom subore;Model pre synchronizaciu kodu;
    @Synchronization(direction = Direction.JAVATOSJ)
    @PostProcessing(type = PostProcessingType.GUARDING)
    private boolean updateSieveDocument_DeleteAction(ViewModel<T> model, JavaFile<T> javaFile) {

        if (javaFile == null) {
            return false;
        }
        JavaFile<T> jF;
        if ((jF = model.deleteFile(javaFile)) == null) {
            return false;
        }

//        JEditorPane[] panes = model.getEditorCookieSieveDocument().getOpenedPanes();
//        int[] carets = new int[panes.length];
//        for (int i = 0; i < panes.length; i++) {
//            carets[i] = panes[i].getCaretPosition();
//        }
        int startFile = jF.getBeginInSJ();
        int endFile = jF.getEndInSJ();

        try {
            BaseDocument sieveDocument = ((BaseDocument) model.getEditorCookieSieveDocument().openDocument());
            sieveDocument.extWriteLock();
            try {
                NbDocument.unmarkGuarded((StyledDocument) sieveDocument, -100, Integer.MAX_VALUE);
                sieveDocument.remove(startFile, endFile - startFile + 1);
//                model.getEditorCookieSieveDocument().saveDocument();
                //TODO: dokoncit
                // XXX: nespravi sa nekonzistentnym?
                processGuardingRequests((StyledDocument) sieveDocument, model);
                return true;
            } finally {
                sieveDocument.extWriteUnlock();
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    //SsceIntent:Zobrazenie projekcie kodu v pomocnom subore;Zobrazenie importov v pomocnom subore;Zobrazenie fragmentu kodu v pomocnom subore;Model pre synchronizaciu kodu;
    @Synchronization(direction = Direction.JAVATOSJ)
    @PostProcessing(type = PostProcessingType.GUARDING)
    private boolean updateSieveDocument_UpdateAction(ViewModel<T> model, JavaFile<T> javaFile) {
        if (javaFile == null) {
            return false;
        }
        JavaFile<T> jF;
        if (javaFile.getCodeSnippets() == null || javaFile.getCodeSnippets().isEmpty()) {
            return updateSieveDocument_DeleteAction(model, javaFile);
        } else if ((jF = model.updateFile(javaFile)) == null) {
            return updateSieveDocument_InsertAction(model, javaFile);
        }

        StringBuilder buffer = new StringBuilder();

//        return loadSieveDocument(model);
        int startFileOffset = jF.getBeginInSJ();

        List<SnippetToBeUpdated> updates = new ArrayList<SnippetToBeUpdated>();

        SnippetToBeUpdated file;
        SnippetToBeUpdated element;
        try {

            BaseDocument doc = (BaseDocument) jF.getEditorCookie().openDocument();
            doc.readLock();
            try {
                file = new SnippetToBeUpdated();
                file.setJavaFile(jF);
                file.setStart(buffer.length() + startFileOffset);

                buffer.append(jF.getStartTextForSJDoc());

                element = new SnippetToBeUpdated();
                element.setImports(jF);
                element.setStart(buffer.length() + startFileOffset);
                buffer.append(jF.getNecessaryImports().toString());
//                if (element.start == buffer.length() + startFileOffset) {
                buffer.append("\n");
//                }
                element.setEnd(buffer.length() - 1 + startFileOffset);
                updates.add(element);

//                buffer.append("\n");
                for (CodeSnippet<T> c : jF.getCodeSnippets()) {
                    element = new SnippetToBeUpdated();
                    element.setCode(c);
                    buffer.append(c.getStartTextForSJDoc());
                    element.setStart(buffer.length() + startFileOffset);
                    buffer.append(doc.getText((int) c.getCodeBinding().getStartPositionJavaDocument(), (int) c.getCodeBinding().getLengthBindingAreaJavaDocument()));

                    element.setEnd(buffer.length() - 1 + startFileOffset);
                    updates.add(element);

                    buffer.append(c.getEndTextForSJDoc());
                }

                buffer.append(jF.getEndTextForSJDoc());

                file.setEnd(buffer.length() - 1 + startFileOffset);
                updates.add(file);
            } finally {
                doc.readUnlock();
            }

            // tuna updates nesie nanovo vytvoreny usek zdrojaku podla java suboru pre dany subor
            // remember caret position and calculate new caret positions
            JEditorPane[] panes = model.getEditorCookieSieveDocument().getOpenedPanes();
            Integer[] carets = null;

            int startOldFile = jF.getBeginInSJ();
            int endOldFile = jF.getEndInSJ();
            int lengthOldFile = endOldFile - startOldFile + 1;

            if (panes != null) {

                int startNewFile = startOldFile;
                int lengthNewFile = buffer.length();

                carets = new Integer[panes.length];
                for (int i = 0; i < panes.length; i++) {
                    int actualCaret = panes[i].getCaretPosition();
                    if (startOldFile <= actualCaret && actualCaret <= endOldFile) {
                        float ratio = ((float) (actualCaret - startOldFile)) / lengthOldFile;
                        carets[i] = startNewFile + (int) (ratio * lengthNewFile);
                    } else {
                        carets[i] = null;
                    }
                }
            }
            // end calculating caret positions

            BaseDocument sieveDocument = ((BaseDocument) model.getEditorCookieSieveDocument().openDocument());
            sieveDocument.extWriteLock();
            try {
                NbDocument.unmarkGuarded((StyledDocument) sieveDocument, -100, Integer.MAX_VALUE);
//                System.out.println("start=" + startOldFile + ",   length=" + lengthOldFile);
                sieveDocument.replace(startOldFile, lengthOldFile, buffer.toString(), null);
//                model.getEditorCookieSieveDocument().saveDocument();
                updatePositions(updates, sieveDocument); // make update positions in SieveDocument=> model is corect, fully defined

                if (!model.isInitialized()) {
                    throw new RuntimeException("Model is not consistent!");
                }
                //TODO: dokoncit
                // XXX: tiez to vyzera ze treba dorobit update star a end files..
                processGuardingRequests((StyledDocument) sieveDocument, model);

            } finally {
                sieveDocument.extWriteUnlock();
            }

            // setting remembered caret positions
            if (carets != null) {
                for (int i = 0; i < panes.length; i++) {
                    if (carets[i] != null && carets[i] >= 0 && carets[i] <= sieveDocument.getLength()) {
                        panes[i].setCaretPosition(carets[i]);
                    }
                }
            }

            return true;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    //SsceIntent:Zobrazenie projekcie kodu v pomocnom subore;Zobrazenie importov v pomocnom subore;Zobrazenie fragmentu kodu v pomocnom subore;Model pre synchronizaciu kodu;
    @Synchronization(direction = Direction.JAVATOSJ)
    @PostProcessing(type = PostProcessingType.GUARDING)
    private boolean updateSieveDocument_InsertAction(ViewModel<T> model, JavaFile<T> javaFile) {
        StringBuilder buffer = new StringBuilder();

        JavaFile<T> jF;
        if ((jF = model.insertFileToModel(javaFile)) == null) {
            return false;
        }

        int startFileOffset;
        JavaFile<T> fileTmp;
        if ((fileTmp = model.getFileNextTo(jF.getFilePath())) != null) {
            startFileOffset = fileTmp.getBeginInSJ();
        } else if ((fileTmp = model.getFilePreviousTo(jF.getFilePath())) != null) {
            startFileOffset = fileTmp.getEndInSJ() + 1;
        } else {
            startFileOffset = 0;
        }

        List<SnippetToBeUpdated> updates = new ArrayList<SnippetToBeUpdated>();

        SnippetToBeUpdated file;
        SnippetToBeUpdated element;
        try {

            BaseDocument doc = (BaseDocument) jF.getEditorCookie().openDocument();
            doc.readLock();
            try {
                file = new SnippetToBeUpdated();
                file.setJavaFile(jF);
                file.setStart(buffer.length() + startFileOffset);

                buffer.append(jF.getStartTextForSJDoc());

                element = new SnippetToBeUpdated();
                element.setImports(jF);
                element.setStart(buffer.length() + startFileOffset);
                buffer.append(jF.getNecessaryImports().toString());
//                if (element.start == buffer.length() + startFileOffset) {
                buffer.append("\n");
//                }
                element.setEnd(buffer.length() - 1 + startFileOffset);
                updates.add(element);

//                buffer.append("\n");
                for (CodeSnippet<T> c : jF.getCodeSnippets()) {
                    element = new SnippetToBeUpdated();
                    element.setCode(c);
                    buffer.append(c.getStartTextForSJDoc());
                    element.setStart(buffer.length() + startFileOffset);
                    buffer.append(doc.getText((int) c.getCodeBinding().getStartPositionJavaDocument(), (int) c.getCodeBinding().getLengthBindingAreaJavaDocument()));

                    element.setEnd(buffer.length() - 1 + startFileOffset);
                    updates.add(element);

                    buffer.append(c.getEndTextForSJDoc());
                }

                buffer.append(jF.getEndTextForSJDoc());

                file.setEnd(buffer.length() - 1 + startFileOffset);
                updates.add(file);
            } finally {
                doc.readUnlock();
            }

            BaseDocument sieveDocument = ((BaseDocument) model.getEditorCookieSieveDocument().openDocument());
            sieveDocument.extWriteLock();
            try {
                NbDocument.unmarkGuarded((StyledDocument) sieveDocument, -100, Integer.MAX_VALUE);
                sieveDocument.insertString(startFileOffset, buffer.toString(), null);
//                model.getEditorCookieSieveDocument().saveDocument();
                updatePositions(updates, sieveDocument); // make update positions in SieveDocument=> model is corect, fully defined

                if (!model.isInitialized()) {
                    throw new RuntimeException("Model is not consistent!");
                }
                //TODO: dokoncit
                // XXX: to iste co vyssie
                processGuardingRequests((StyledDocument) sieveDocument, model);

                return true;
            } finally {
                sieveDocument.extWriteUnlock();
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;Model pre synchronizaciu kodu;
    @Synchronization(direction = Direction.JAVATOSJ)
    private boolean updatePositions(List<SnippetToBeUpdated> updatePositions, BaseDocument doc) {
        for (SnippetToBeUpdated tup : updatePositions) {
            if (!tup.updatePositions(doc)) {
                return false;
            }
        }
        return true;
    }

    @SievedDocument
    @PostProcessing(type = PostProcessingType.GUARDING)
    public void processGuardingRequests(StyledDocument document, ViewModel<T> model) {
        List<GuardingRequest> guards = new LinkedList<GuardingRequest>();
        for (GuardingProvider provider : guardingProviders) {
            guards.addAll(provider.createGuards(model));
            for (JavaFile<T> javaFile : model.getFiles()) {
                guards.addAll(provider.createGuards(javaFile));
                for (CodeSnippet<T> snippet : javaFile.getCodeSnippets()) {
                    guards.addAll(provider.createGuards(snippet));
                }
            }
        }

        NbDocument.unmarkGuarded(document, -100, Integer.MAX_VALUE);
        for (GuardingRequest request : guards) {
            NbDocument.markGuarded(document, request.getStartOffset(), request.getLength());
        }
    }

    /**
     * Metoda aktualizuje obsah java suboru a modelu na zaklade zmeny v pomocnom
     * subore .sj.
     *
     * @param model model prepojenia java suborov s pomocnym suborom .sj.
     * @param javaFile java subor, ktory ma byt aktualizovany.
     * @param offsetJF offset v pomocnom subore .sj, kde doslo k zmene.
     * @return true, ak java subor a model je uspesne aktualizovany.
     */
    //SsceIntent:Praca s java suborom;Model pre synchronizaciu kodu;
    @Synchronization(direction = Direction.SJTOJAVA)
    public boolean updateJavaDocument(ViewModel<T> model, JavaFile<T> javaFile, int offsetJF) {
        BaseDocument sieveDoc;
        BaseDocument javaDoc;
        try {
            sieveDoc = (BaseDocument) model.getEditorCookieSieveDocument().openDocument();
            javaDoc = (BaseDocument) javaFile.getEditorCookie().openDocument();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }

        // robi sa aj update import sekcie
        if (javaFile.getImportsBinding().getStartPositionSieveDocument() <= offsetJF && offsetJF <= javaFile.getImportsBinding().getEndPositionSieveDocument()) {

            sieveDoc.readLock();
            try {
                Imports imports = viewModelCreator.getImports(sieveDoc, javaFile.getImportsBinding().getStartPositionSieveDocument(), javaFile.getImportsBinding().getEndPositionSieveDocument() - javaFile.getImportsBinding().getStartPositionSieveDocument() + 1);
                if (imports == null) {
                    return false;
                }

                imports.setEditableAllImports(true);

                javaFile.getAllImports().removeImports(javaFile.getNecessaryImports());
                javaFile.getNecessaryImports().removeAllEditableImports();
                javaFile.getNecessaryImports().addImports(imports);
                javaFile.getAllImports().addImports(javaFile.getNecessaryImports());

            } finally {
                sieveDoc.readUnlock();
            }

            // remember caret position and calculate new caret positions
            JEditorPane[] panes = javaFile.getEditorCookie().getOpenedPanes();
            Integer[] carets = null;
            if (panes != null) {
                int startOld = javaFile.getImportsBinding().getStartPositionJavaDocument();
                int endOld = javaFile.getImportsBinding().getEndPositionJavaDocument();
                int lengthOld = javaFile.getImportsBinding().getLengthBindingAreaJavaDocument();

                int startNew = startOld;
                int lengthNew = javaFile.getAllImports().toString().length();

                carets = new Integer[panes.length];
                for (int i = 0; i < panes.length; i++) {
                    int actualCaret = panes[i].getCaretPosition();
                    if (startOld <= actualCaret && actualCaret < endOld) {
                        float ratio = ((float) (actualCaret - startOld)) / lengthOld;
                        carets[i] = startNew + (int) (ratio * lengthNew);
                    } else {
                        carets[i] = null;
                    }
                }
            }
            // end calculating caret positions

            javaDoc.extWriteLock();
            try {
                String text = javaFile.getAllImports().toString();
                int startPosition = javaFile.getImportsBinding().getStartPositionJavaDocument();
                int endPosition; //= javaFile.getImportsBinding().getEndPositionJavaDocument();
//                System.out.println("Replace java file imports: start= " + startPosition + "  end= " + endPosition);

                javaDoc.replace(startPosition, (int) javaFile.getImportsBinding().getLengthBindingAreaJavaDocument(), text, null);
                endPosition = startPosition + text.length();
                javaFile.getImportsBinding().setStartPositionJavaDocument(javaDoc, startPosition);
                javaFile.getImportsBinding().setLengthJavaDocument(endPosition - startPosition);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            } finally {
                javaDoc.extWriteUnlock();
            }

            // setting remembered caret positions
            if (carets != null) {
                for (int i = 0; i < panes.length; i++) {
                    if (carets[i] != null && carets[i] >= 0 && carets[i] <= javaDoc.getLength()) {
                        panes[i].setCaretPosition(carets[i]);
                    }
                }
            }

            return true;
        }

        for (CodeSnippet<T> code : javaFile.getCodeSnippets()) {
            if (code.getCodeBinding().getStartPositionSieveDocument() <= offsetJF && offsetJF <= code.getCodeBinding().getEndPositionSieveDocument() + 1) {
                String text;
                sieveDoc.readLock();
                try {
                    text = sieveDoc.getText(code.getCodeBinding().getStartPositionSieveDocument(), code.getCodeBinding().getEndPositionSieveDocument() - code.getCodeBinding().getStartPositionSieveDocument() + 1);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                    return false;
                } finally {
                    sieveDoc.readUnlock();
                }

                // remember caret position and calculate new caret positions
                JEditorPane[] panes = javaFile.getEditorCookie().getOpenedPanes();
                Integer[] carets = null;
                if (panes != null) {
                    int startOld = code.getCodeBinding().getStartPositionJavaDocument();
                    int endOld = code.getCodeBinding().getEndPositionJavaDocument();
                    int lengthOld = code.getCodeBinding().getLengthBindingAreaJavaDocument();

                    int startNew = startOld;
                    int lengthNew = text.length();

                    carets = new Integer[panes.length];
                    for (int i = 0; i < panes.length; i++) {
                        int actualCaret = panes[i].getCaretPosition();
                        if (startOld <= actualCaret && actualCaret < endOld) {
                            float ratio = ((float) (actualCaret - startOld)) / lengthOld;
                            carets[i] = startNew + (int) (ratio * lengthNew);
                        } else {
                            carets[i] = null;
                        }
                    }
                }
                // end calculating caret positions

                javaDoc.extWriteLock();
                try {
                    int startPosition = (int) code.getCodeBinding().getStartPositionJavaDocument();
                    javaDoc.replace(startPosition, (int) code.getCodeBinding().getLengthBindingAreaJavaDocument(), text, null);
                    int endPosition = startPosition + text.length();
                    code.getCodeBinding().setStartPositionJavaDocument(javaDoc, startPosition);
                    code.getCodeBinding().setLengthJavaDocument(endPosition - startPosition);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                    return false;
                } finally {
                    javaDoc.extWriteUnlock();
                }

                // setting remembered caret positions
                if (carets != null) {
                    for (int i = 0; i < panes.length; i++) {
                        if (carets[i] != null && carets[i] >= 0 && carets[i] <= javaDoc.getLength()) {
                            panes[i].setCaretPosition(carets[i]);
                        }
                    }
                }

                return true;
            }
        }
        return false;
    }
}
