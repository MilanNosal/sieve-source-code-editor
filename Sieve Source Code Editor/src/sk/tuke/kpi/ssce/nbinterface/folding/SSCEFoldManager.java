package sk.tuke.kpi.ssce.nbinterface.folding;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import sk.tuke.kpi.ssce.annotations.concerns.PostProcessing;
import sk.tuke.kpi.ssce.annotations.concerns.View;
import sk.tuke.kpi.ssce.annotations.concerns.enums.PostProcessingType;
import sk.tuke.kpi.ssce.annotations.concerns.enums.ViewAspect;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;
import sk.tuke.kpi.ssce.core.Constants;
import sk.tuke.kpi.ssce.core.SSCEditorCore;
import sk.tuke.kpi.ssce.core.model.view.CodeSnippet;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.core.model.view.ViewModel;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.FoldingRequest;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.interfaces.FoldingProvider;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;

@PostProcessing(type = PostProcessingType.FOLDING)
@View(aspect = ViewAspect.PRESENTATION)
public class SSCEFoldManager implements FoldManager, CurrentProjection.CurrentProjectionChangeListener {

    private FoldOperation operation;
    private static final Logger LOG = Logger.getLogger(SSCEFoldManager.class.getName());
    private boolean done = false;

    @Override
    public void init(FoldOperation fo) {
        this.operation = fo;
    }

    @Override
    public void initFolds(FoldHierarchyTransaction fht) {
        Document doc = operation.getHierarchy().getComponent().getDocument();
        SSCEditorCore core = (SSCEditorCore) doc.getProperty(Constants.SSCE_CORE_OBJECT_PROP);
        core.getCurrentProjection().addCurrentProjectionChangeListener(this);
        SwingUtilities.invokeLater(new AddFolds());
    }

    @Override
    public void insertUpdate(DocumentEvent de, FoldHierarchyTransaction fht) {
        if (!done) {
            SwingUtilities.invokeLater(new AddFolds());
        }
    }

    @Override
    public void removeUpdate(DocumentEvent de, FoldHierarchyTransaction fht) {
        //SwingUtilities.invokeLater(new AddFolds()); -- this should be redundant, as folds are guareded, they cannot be damaged
    }

    @Override
    public void changedUpdate(DocumentEvent de, FoldHierarchyTransaction fht) {
        //SwingUtilities.invokeLater(new AddFolds()); -- this causes inifinite looping because adding guards 
        //                                               apparently calls this notification
    }

    @Override
    public void removeEmptyNotify(Fold fold) {
    }

    @Override
    public void removeDamagedNotify(Fold fold) {
    }

    @Override
    public void expandNotify(Fold fold) {
    }

    @Override
    public void release() {
        Document doc = operation.getHierarchy().getComponent().getDocument();
        SSCEditorCore core = (SSCEditorCore) doc.getProperty(Constants.SSCE_CORE_OBJECT_PROP);
        core.getCurrentProjection().removeCurrentProjectionChangeListener(this);
    }

    @Override
    public void projectionChanged(CurrentProjection.CurrentProjectionChangedEvent event) {
        done = false;
    }

    private class AddFolds implements Runnable {

        private boolean insideRender;

        public void run() {
            if (!insideRender) {
                insideRender = true;
                operation.getHierarchy().getComponent().getDocument().render(this);
                return;
            }
            Document doc = operation.getHierarchy().getComponent().getDocument();

            SSCEditorCore<? extends Concern> core = (SSCEditorCore<? extends Concern>) doc.getProperty(Constants.SSCE_CORE_OBJECT_PROP);

            //List<Integer> fromTo = new LinkedList<Integer>();
            operation.getHierarchy().lock();

            FoldHierarchyTransaction transaction = operation.openTransaction();
            List<FoldingRequest> folds = new LinkedList<FoldingRequest>();
            ViewModel<Concern> model = core.getViewModel();
            for (FoldingProvider provider : core.getFoldingProviders()) {
                folds.addAll(provider.createFolds(model));
                for (JavaFile<Concern> javaFile : model.getFiles()) {
                    folds.addAll(provider.createFolds(javaFile));
                    for (CodeSnippet snippet : javaFile.getCodeSnippets()) {
                        folds.addAll(provider.createFolds(snippet));
                    }
                }
            }
            try {
                for (FoldingRequest fold : folds) {
                    operation.addToHierarchy(new FoldType("SSCEFold"),
                            fold.getDescription(),
                            true,
                            fold.getStartOffset(),
                            fold.getEndOffset(),
                            fold.getGuardedStartLength(),
                            fold.getGuardedEndLength(),
                            fold,
                            transaction);
                }
                done = true;
            } catch (BadLocationException ex) {
                LOG.log(Level.WARNING, null, ex);
            } finally {
                transaction.commit();
            }
            operation.getHierarchy().unlock();
        }
    }

    public static final class Factory implements FoldManagerFactory {

        @Override
        public FoldManager createFoldManager() {
//            System.out.println("\nHello Mattoke\n");
            return new SSCEFoldManager();
        }
    }
}
