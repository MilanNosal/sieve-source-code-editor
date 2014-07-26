package sk.tuke.kpi.ssce.nbinterface;

import java.io.File;
import java.io.IOException;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import sk.tuke.kpi.ssce.core.Constants;

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
public final class SSCESieverTopComponent extends TopComponent {

    private Project currentlySelectedProject = null;
    private Lookup genlokup;

    public SSCESieverTopComponent() {
        initComponents();
        setName(Bundle.CTL_SSCESieverTopComponent());
        setToolTipText(Bundle.HINT_SSCESieverTopComponent());

        genlokup = Utilities.actionsGlobalContext();
        currentlySelectedProject = genlokup.lookup(Project.class);

        //Lookup genlokup = Utilities.actionsGlobalContext();
        Lookup.Result<Project> globalResultOBJ = genlokup.lookup(new Lookup.Template(Project.class)); // or Project.class if you are interested espetialy in Project 
        LookupListener globalListenerOBJ = new LookupListener() {

            @Override
            public void resultChanged(LookupEvent le) {
                currentlySelectedProject = genlokup.lookup(Project.class);
                projectChanged();
            }
        };
        globalResultOBJ.addLookupListener(globalListenerOBJ);
        globalResultOBJ.allInstances();
    }

    private void projectChanged() {
        showCurrentProject();
    }

    private void showCurrentProject() {
        currentProjectTextBox.setText(
                currentlySelectedProject == null ? "None" : ProjectUtils.getInformation(currentlySelectedProject).getDisplayName()
        );
    }

    private void openSJDocumentForCurrentProject(Project currentlySelectedProject) {
        File file = new File(currentlySelectedProject.getProjectDirectory().getPath()
                + File.separator
                + ProjectUtils.getInformation(currentlySelectedProject).getDisplayName() + ".sj");
        if (!file.exists()) {
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
//                ec.open();
            }
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        SSCETabPanel = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        SSCEPanel = new javax.swing.JPanel();
        currentProjectLabel = new javax.swing.JLabel();
        currentProjectTextBox = new javax.swing.JTextField();
        ProjectionPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        SSCEPanel.setPreferredSize(new java.awt.Dimension(321, 223));

        org.openide.awt.Mnemonics.setLocalizedText(currentProjectLabel, org.openide.util.NbBundle.getMessage(SSCESieverTopComponent.class, "SSCESieverTopComponent.currentProjectLabel.text")); // NOI18N

        currentProjectTextBox.setEditable(false);
        currentProjectTextBox.setText(org.openide.util.NbBundle.getMessage(SSCESieverTopComponent.class, "SSCESieverTopComponent.currentProjectTextBox.text")); // NOI18N

        javax.swing.GroupLayout SSCEPanelLayout = new javax.swing.GroupLayout(SSCEPanel);
        SSCEPanel.setLayout(SSCEPanelLayout);
        SSCEPanelLayout.setHorizontalGroup(
            SSCEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SSCEPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(currentProjectLabel)
                .addGap(26, 26, 26)
                .addComponent(currentProjectTextBox, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addContainerGap())
        );
        SSCEPanelLayout.setVerticalGroup(
            SSCEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SSCEPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SSCEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(currentProjectLabel)
                    .addComponent(currentProjectTextBox))
                .addGap(272, 272, 272))
        );

        jScrollPane1.setViewportView(SSCEPanel);

        SSCETabPanel.addTab(org.openide.util.NbBundle.getMessage(SSCESieverTopComponent.class, "SSCESieverTopComponent.jScrollPane1.TabConstraints.tabTitle"), jScrollPane1); // NOI18N

        javax.swing.GroupLayout ProjectionPanelLayout = new javax.swing.GroupLayout(ProjectionPanel);
        ProjectionPanel.setLayout(ProjectionPanelLayout);
        ProjectionPanelLayout.setHorizontalGroup(
            ProjectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 374, Short.MAX_VALUE)
        );
        ProjectionPanelLayout.setVerticalGroup(
            ProjectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 264, Short.MAX_VALUE)
        );

        SSCETabPanel.addTab(org.openide.util.NbBundle.getMessage(SSCESieverTopComponent.class, "SSCESieverTopComponent.ProjectionPanel.TabConstraints.tabTitle"), ProjectionPanel); // NOI18N

        add(SSCETabPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ProjectionPanel;
    private javax.swing.JPanel SSCEPanel;
    private javax.swing.JTabbedPane SSCETabPanel;
    private javax.swing.JLabel currentProjectLabel;
    private javax.swing.JTextField currentProjectTextBox;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
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
}
