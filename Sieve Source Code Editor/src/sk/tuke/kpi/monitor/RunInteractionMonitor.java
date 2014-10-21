package sk.tuke.kpi.monitor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import sk.tuke.kpi.monitor.logging.Logger;
import sk.tuke.kpi.monitor.logging.LoggerFactory;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection.CurrentProjectionChangeListener;
import sk.tuke.kpi.ssce.projection.provider.ProjectionProvider;

@ActionID(
        category = "File",
        id = "sk.tuke.user.monitor.RunInteractionMonitor"
)
@ActionRegistration(
        iconBase = "sk/tuke/kpi/monitor/monitor_24.png",
        displayName = "#CTL_RunInteractionMonitor"
)
@ActionReference(path = "Toolbars/File", position = 2100)
@Messages("CTL_RunInteractionMonitor=Run User Interaction Monitor")
public final class RunInteractionMonitor implements ActionListener {

    // Map<EditorCookie, JEditorPane[]> panes = new HashMap<EditorCookie, JEditorPane[]>();
    private Logger logger;

    private final static Set<RunInteractionMonitor> instances = new HashSet<RunInteractionMonitor>(1);

    public static Set<RunInteractionMonitor> getInstances() {
        return instances;
    }

    public RunInteractionMonitor() {
        instances.add(this);
    }

    // <editor-fold desc="Current projection listener" defaultstate="collapsed">
    private final CurrentProjectionChangeListener projectionChangeListener = new CurrentProjection.CurrentProjectionChangeListener() {
        @Override
        public void projectionChanged(CurrentProjection.CurrentProjectionChangedEvent event) {
            String project = ProjectUtils.getInformation(watchedProvider.getProjectContext()).getDisplayName();
            logger.logEntry(Logger.EventType.CURRENT_PROJECTIONS_CHANGED, project, event.toString());
        }
    };
    // </editor-fold>

    // <editor-fold desc="Current projections provider listener" defaultstate="collapsed">
    private ProjectionProvider watchedProvider;
    
    private final PropertyChangeListener currentProjectionsProviderListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue() != null) {
                if (watchedProvider != null) {
                    watchedProvider.getSSCECore().getCurrentProjection().removeCurrentProjectionChangeListener(projectionChangeListener);
                    watchedProvider = null;
                }
                ProjectionProvider pp = (ProjectionProvider) evt.getNewValue();
                logger.logEntry(Logger.EventType.PROJECTIONS_STARTED,
                        ProjectUtils.getInformation(pp.getProjectContext()).getDisplayName(),
                        pp.getDisplayName());
                watchedProvider = pp;
                watchedProvider.getSSCECore().getCurrentProjection().addCurrentProjectionChangeListener(projectionChangeListener);

            } else if (evt.getOldValue() != null) {
                if (watchedProvider != null) {
                    watchedProvider.getSSCECore().getCurrentProjection().removeCurrentProjectionChangeListener(projectionChangeListener);
                    watchedProvider = null;
                }
                ProjectionProvider pp = (ProjectionProvider) evt.getOldValue();
                logger.logEntry(Logger.EventType.PROJECTIONS_ENDED,
                        ProjectUtils.getInformation(pp.getProjectContext()).getDisplayName(),
                        pp.getDisplayName());
            }
        }
    };
    // </editor-fold>

    // <editor-fold desc="Top components focus listener" defaultstate="collapsed">
    private final PropertyChangeListener topComponentActivatedListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName()) 
                    && !evt.getNewValue().getClass().getCanonicalName().equals("org.netbeans.core.multiview.MultiViewCloneableTopComponent")) {
                logger.logEntry(Logger.EventType.FOCUS_GAINED, null /* Global, no relationship with a project */,
                        evt.getNewValue().getClass().getCanonicalName());
            }
        }
    };
    // </editor-fold>

    // <editor-fold desc="Document change listener" defaultstate="collapsed">
    private final DocumentListener documentListener = new DocumentListener() {
        private long timestamp = 0;
        private Document lastEdited = null;

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (!e.getDocument().equals(lastEdited)) {
                logDocumentEvent(e);
                lastEdited = e.getDocument();
            } else if ((timestamp - System.currentTimeMillis()) > 5000) {
                logDocumentEvent(e);
            }
            timestamp = System.currentTimeMillis();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if (!e.getDocument().equals(lastEdited)) {
                logDocumentEvent(e);
                lastEdited = e.getDocument();
            } else if ((timestamp - System.currentTimeMillis()) > 5000) {
                logDocumentEvent(e);
            }
            timestamp = System.currentTimeMillis();
        }

        private void logDocumentEvent(DocumentEvent e) {
            DataObject dobj = NbEditorUtilities.getDataObject(e.getDocument());
            Project owner = FileOwnerQuery.getOwner(dobj.getPrimaryFile());
            String projectName = (owner == null) ? null : ProjectUtils.getInformation(owner).getDisplayName();

            logger.logEntry(Logger.EventType.DOCUMENT_EDITED, projectName, dobj.getPrimaryFile().getPath());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }
    };
    // </editor-fold>

    // <editor-fold desc="Editor focus listener" defaultstate="collapsed">
    private final PropertyChangeListener editorFocusListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
                JTextComponent jTextComponent = (JTextComponent) evt.getNewValue();
                DataObject dobj = NbEditorUtilities.getDataObject(jTextComponent.getDocument());
                Project owner = FileOwnerQuery.getOwner(dobj.getPrimaryFile());
                String projectName = (owner == null) ? null : ProjectUtils.getInformation(owner).getDisplayName();

                logger.logEntry(Logger.EventType.EDITOR_FOCUS_GAINED, projectName, dobj.getPrimaryFile().getPath());

                jTextComponent.getDocument().addDocumentListener(documentListener);
            }
            if (EditorRegistry.FOCUS_LOST_PROPERTY.equals(evt.getPropertyName())) {
                ((JTextComponent) evt.getOldValue()).getDocument().removeDocumentListener(documentListener);
            }
        }
    };
    // </editor-fold>

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (logger != null) {
            endLogging();
            return;
        }

        startLogging();
    }

    // <editor-fold desc="Logging lifecycle" defaultstate="collapsed">
    void endLogging() {
        if (logger != null) {
            EditorRegistry.removePropertyChangeListener(editorFocusListener);
            TopComponent.getRegistry().removePropertyChangeListener(topComponentActivatedListener);
            
            final TopComponent outputWindow = WindowManager.getDefault().findTopComponent("SSCESieverTopComponent");
            if (outputWindow != null) {
                outputWindow.removePropertyChangeListener("currentProjectionProvider", currentProjectionsProviderListener);
            }
            JTextComponent jTextComponent = EditorRegistry.lastFocusedComponent();
            if (jTextComponent != null) {
                jTextComponent.getDocument().removeDocumentListener(documentListener);
            }
            
            if (watchedProvider != null) {
                watchedProvider.getSSCECore().getCurrentProjection().removeCurrentProjectionChangeListener(projectionChangeListener);
                watchedProvider = null;
            }
                    
            logger.endSession();
            logger.endLogging();
            logger = null;

            StatusDisplayer.getDefault().setStatusText("Stopped monitoring");
        }
    }

    private void startLogging() {
        if (logger == null) {
            String txt = "Session name: ";
            String title = "Start a new session";

            NotifyDescriptor.InputLine input
                    = new NotifyDescriptor.InputLine(txt, title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE);
            input.setInputText("session"); // specify a default name
            Object result = DialogDisplayer.getDefault().notify(input);
            if (result != NotifyDescriptor.OK_OPTION) {
                return;
            }
            String userInput = input.getInputText();

            logger = LoggerFactory.getLogger();

            logger.startSession(System.getProperty("user.name"), userInput);

            final TopComponent outputWindow = WindowManager.getDefault().findTopComponent("SSCESieverTopComponent");
            if (outputWindow != null) {
                outputWindow.addPropertyChangeListener("currentProjectionProvider", currentProjectionsProviderListener);
            }

            TopComponent.getRegistry().addPropertyChangeListener(topComponentActivatedListener);
            EditorRegistry.addPropertyChangeListener(editorFocusListener);
        }
    }
    // </editor-fold>
}
