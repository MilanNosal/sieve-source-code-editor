package sk.tuke.kpi.ssce.nbinterface;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import sk.tuke.kpi.ssce.annotations.concerns.ProjectionSelectionUI;
import sk.tuke.kpi.ssce.annotations.concerns.SSCE_UI;
import sk.tuke.kpi.ssce.projection.provider.ProjectionProvider;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//sk.tuke.kpi.ssce.nbinterface//SSCESiever//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "SSCESieverTopComponent",
        iconBase = "sk/tuke/kpi/ssce/nbinterface/actions/ssce.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "navigator", openAtStartup = false)
@ActionID(category = "Window", id = "sk.tuke.kpi.ssce.nbinterface.SSCESieverTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_SSCESieverAction",
        preferredID = "SSCESieverTopComponent"
)
@Messages({
    "CTL_SSCESieverAction=SSCESiever",
    "CTL_SSCESieverTopComponent=SSCESiever Window",
    "HINT_SSCESieverTopComponent=This is a SSCESiever window"
})
@SSCE_UI
@ProjectionSelectionUI
public final class SSCESieverTopComponent extends TopComponent {

    private final List<ProjectionsChangeListener> listeners = new LinkedList<ProjectionsChangeListener>();

    private ProjectionProvider currentProjectionProvider;

    public SSCESieverTopComponent() {
        initComponents();
        setName(Bundle.CTL_SSCESieverTopComponent());
        setToolTipText(Bundle.HINT_SSCESieverTopComponent());
    }

    public void startProjection(ProjectionProvider provider) {
        ProjectionProvider oldProvider = currentProjectionProvider;
        if (currentProjectionProvider != null) {
            currentProjectionProvider.dispose();
        }
        currentProjectionProvider = provider;
        this.content.setViewportView(currentProjectionProvider.getView());
        fireCurrentProjectionChange(
                new ProjectionsChangedEvent(
                        oldProvider, currentProjectionProvider, ProjectionsChangedEvent.Type.PROJECTION_STARTED
                ));
    }

    public void dispose() {
        ProjectionProvider oldProvider = currentProjectionProvider;
        if (currentProjectionProvider != null) {
            currentProjectionProvider.dispose();
            currentProjectionProvider = null;
        }
        this.content.setViewportView(new NoViewPanel());
        fireCurrentProjectionChange(
                new ProjectionsChangedEvent(
                        oldProvider, null, ProjectionsChangedEvent.Type.PROJECTION_ENDED
                ));
    }
   
    public boolean addCurrentProjectionChangeListener(ProjectionsChangeListener listener) {
        return listeners.add(listener);
    }
    
    public boolean addCurrentProjectionChangeListener(int position, ProjectionsChangeListener listener) {
        listeners.add(position, listener);
        return true;
    }

    public boolean removeCurrentProjectionChangeListener(ProjectionsChangeListener listener) {
        return listeners.remove(listener);
    }
    
    private void fireCurrentProjectionChange(ProjectionsChangedEvent event) {
        if (event == null) {
            return;
        }
        for (ProjectionsChangeListener listener : listeners) {
            listener.projectionsChanged(event);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        content = new javax.swing.JScrollPane();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SSCESieverTopComponent.class, "SSCESieverTopComponent.jLabel1.text")); // NOI18N

        setLayout(new java.awt.BorderLayout());
        add(content, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane content;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        if (currentProjectionProvider != null) {
            currentProjectionProvider.dispose();
            currentProjectionProvider = null;
        }
        this.content.setViewportView(new NoViewPanel());
    }

    @Override
    public void componentClosed() {
        if (currentProjectionProvider != null) {
            currentProjectionProvider.dispose();
            currentProjectionProvider = null;
        }
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
    
    public static interface ProjectionsChangeListener {

        /**
         * Volana ked dojde k zmene v konfiguracii zamerov.
         * @param event event
         */
        public void projectionsChanged(ProjectionsChangedEvent event);
    }
    
    public static class ProjectionsChangedEvent {
        
        public enum Type {
            PROJECTION_STARTED,
            PROJECTION_ENDED
        }
        
        private final Type type;
        private final ProjectionProvider newProvider;
        private final ProjectionProvider oldProvider;

        /**
         * Vytvori event pre zmenu v konfiguracii zamerov.
         * @param newProjection nova konfiguracia zamerov.
         */
        public ProjectionsChangedEvent(
                ProjectionProvider oldProvider,
                ProjectionProvider newProvider,
                Type type) {
            this.type = type;
            this.newProvider = newProvider;
            this.oldProvider = oldProvider;
        }

        public Type getType() {
            return type;
        }

        public ProjectionProvider getNewProvider() {
            return newProvider;
        }

        public ProjectionProvider getOldProvider() {
            return oldProvider;
        }
    }
}
