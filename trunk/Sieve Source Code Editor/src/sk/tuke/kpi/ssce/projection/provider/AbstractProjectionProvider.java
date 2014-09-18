package sk.tuke.kpi.ssce.projection.provider;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
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
     *
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
            // XXX: this should not be necessary, but just to be precise
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
        try {
            final DataObject dobj = DataObject.find(fobj);
            if (dobj != null) {
                EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
                
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

    private static final class SJDOSavable extends AbstractSavable
            implements Icon {

        final DataObject obj;

        public SJDOSavable(DataObject obj) {
            this.obj = obj;
        }

        @Override
        public String findDisplayName() {
            return obj.getNodeDelegate().getDisplayName();
        }

        @Override
        protected void handleSave() throws IOException {
            System.out.println(">>>>>>>>>>>>>>> hoooray milnako");
//            SaveCookie sc = obj.getLookup().lookup(SaveCookie.class);
//            if (sc != null) {
//                sc.save();
//            }
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof SJDOSavable) {
                SJDOSavable dos = (SJDOSavable) other;
                return obj.equals(dos.obj);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return obj.hashCode();
        }

        final void remove() {
            unregister();
        }

        final void add() {
            register();
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            icon().paintIcon(c, g, x, y);
        }

        @Override
        public int getIconWidth() {
            return icon().getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return icon().getIconHeight();
        }

        private Icon icon() {
            return ImageUtilities.image2Icon(obj.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
        }
    }
}
