package sk.tuke.kpi.ssce.projection.provider;

import java.awt.LayoutManager;
import java.io.File;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import sk.tuke.kpi.ssce.annotations.concerns.ProvidersPluginSystem;
import sk.tuke.kpi.ssce.annotations.concerns.SSCE_UI;
import sk.tuke.kpi.ssce.annotations.concerns.SievedDocument;
import sk.tuke.kpi.ssce.core.Constants;

/**
 *
 * @author Milan
 */
@SSCE_UI
@ProvidersPluginSystem
public abstract class AbstractProjectionProvider
        extends JPanel implements ProjectionProvider {
    
    private final Project projectContext;

    public AbstractProjectionProvider(Project projectContext) {
        this.projectContext = projectContext;
    }

    public AbstractProjectionProvider(Project projectContext, LayoutManager layout) {
        super(layout);
        this.projectContext = projectContext;
    }

    public AbstractProjectionProvider(Project projectContext, boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        this.projectContext = projectContext;
    }

    public AbstractProjectionProvider(Project projectContext, LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        this.projectContext = projectContext;
    }
    
    @Override
    public Project getProjectContext() {
        return projectContext;
    }

    /**
     * Opens a SJ document for a given project and returns its data object.
     * @param projectContext
     * @return 
     */
    @SievedDocument
    protected DataObject openSJDocumentForGivenProject() {//Project projectContext) {
        String parentDirectory = projectContext.getProjectDirectory().getPath();
        String projectName = ProjectUtils.getInformation(projectContext).getDisplayName();
        File file = new File(parentDirectory
                + File.separator
                + projectName + ".sj");
        if (file.exists()) {
            // XXX: toto by mohlo hypoteticky vyriesit problem s opakovanim
            file.delete();
        }
        try {
            file.createNewFile();
            // TODO use context
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        file.deleteOnExit();

        FileObject fobj = FileUtil.toFileObject(file);
        DataObject dobj = null;

        try {
            dobj = DataObject.find(fobj);
            if (dobj != null) {
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                StyledDocument doc = ec.openDocument();
                // XXX: toto by mohlo hypoteticky vyriesit problem s opakovanim
                if (doc.getProperty(Constants.SSCE_CORE_OBJECT_PROP) == null) {
                    // XXX: delete existing container
                }

                StatusDisplayer.getDefault().setStatusText("Should open the editor for " + file.getName());

                // XXX: create core and open document
                // doc.putProperty(Constants.SSCE_CORE_OBJECT_PROP, new SSCEditorCore(dobj, context));
//                Action action = findAction("SsceTopComponent");
//                if (action != null) {
//                    action.actionPerformed(null);
//                }
//
                ec.open();
                return dobj;
            }
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
