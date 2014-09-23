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
import sk.tuke.kpi.ssce.core.Constants;
import sk.tuke.kpi.ssce.core.SSCEditorCore;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.FoldingRequest;

public class SSCEFoldManager implements FoldManager {

    private FoldOperation operation;
    private Document doc;
    private static final Logger LOG = Logger.getLogger(SSCEFoldManager.class.getName());

    @Override
    public void init(FoldOperation fo) {
        this.operation = fo;
    }

    @Override
    public void initFolds(FoldHierarchyTransaction fht) {
        SwingUtilities.invokeLater(new AddFolds());
    }

    @Override
    public void insertUpdate(DocumentEvent de, FoldHierarchyTransaction fht) {
        SwingUtilities.invokeLater(new AddFolds());
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
    }
    
    private class AddFolds implements Runnable {

        private boolean insideRender;

        public void run() {
            if (!insideRender) {
                insideRender = true;
                operation.getHierarchy().getComponent().getDocument().render(this);
                return;
            }
            doc = operation.getHierarchy().getComponent().getDocument();
            
            SSCEditorCore core = (SSCEditorCore) doc.getProperty(Constants.SSCE_CORE_OBJECT_PROP);

            //List<Integer> fromTo = new LinkedList<Integer>();

            operation.getHierarchy().lock();

            FoldHierarchyTransaction transaction = operation.openTransaction();
            List<FoldingRequest> folds = core.getModel().getFoldingRequests();
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
            } catch (BadLocationException ex) {
                LOG.log(Level.WARNING, null, ex);
            } finally {
                transaction.commit();
            }
            operation.getHierarchy().unlock();

//            for (Mark mark : marks) {
//                NbDocument.markGuarded((StyledDocument) doc,
//                        mark.position.getOffset(),
//                        mark.ending.position.getOffset() - mark.position.getOffset() + 1);
//            }
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
