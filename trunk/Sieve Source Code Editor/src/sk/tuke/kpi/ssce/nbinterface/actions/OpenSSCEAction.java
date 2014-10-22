package sk.tuke.kpi.ssce.nbinterface.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;
import sk.tuke.kpi.ssce.annotations.concerns.IntegrationWithNetBeans;
import sk.tuke.kpi.ssce.annotations.concerns.SSCE_UI;
import sk.tuke.kpi.ssce.nbinterface.SSCESieverTopComponent;
import sk.tuke.kpi.ssce.nbinterface.options.SSCEOptions;
import sk.tuke.kpi.ssce.nbinterface.wizard.ProjectionWizardIterator;
import sk.tuke.kpi.ssce.projection.provider.ProjectionProviderFactory;

//SsceIntent:Spustenie SSC Editora;
@ActionID(category = "File",
        id = "sk.tuke.kpi.nosal.matej.ssce.nbinterface.actions.OpenSSCEAction")
@ActionRegistration(iconBase = "sk/tuke/kpi/ssce/nbinterface/actions/ssce.png",
        displayName = "#CTL_OpenSSCEAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1050, separatorAfter = 1075),
    @ActionReference(path = "Toolbars/File", position = 500),
    @ActionReference(path = "Projects/Actions", position = 475, separatorBefore = 450)
})
@Messages("CTL_OpenSSCEAction=Open SSCE")
@IntegrationWithNetBeans
@SSCE_UI
public final class OpenSSCEAction implements ActionListener {

    private static Project currentlyProjected;

    /**
     * Premenna uchovavajuca kontext (projekt). Sluzi pre testovanie, či zvoleny
     * projekt je podporovany.
     */
    private final Project context;
    
    private boolean enabled = true;

    /**
     * Vytvori novu akciu OpenSSCEAction, ktora sluzi pre spustenie SSCE
     * editora, pre zvoleny projekt.
     *
     * @param context kontext, projekt.
     */
    public OpenSSCEAction(Project context) {
        this.context = context;
    }

    /**
     * Metoda realizuje spustenie SSCE editora.
     *
     * @param ev udalost, ktora vyvolala tuto akciu.
     */
    //SsceIntent:Spustenie SSC Editora;
    @Override
    public void actionPerformed(ActionEvent ev) {
        if (!enabled) {
            StatusDisplayer.getDefault().setStatusText("Projections are already starting, please be patient...");
            return;
        }
        enabled = false;
        if (context.equals(currentlyProjected)) {
            // simple workaround not to allow rerun of the projections upon the same project
            return;
        }
        ProjectionProviderFactory factory = null;
        if (SSCEOptions.useDefaultImplementation()) {
            factory = SSCEOptions.getDefaultImplementation();
        } else {
            ProjectionWizardIterator iterator = new ProjectionWizardIterator();
            WizardDescriptor wiz = new WizardDescriptor(iterator);
            // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
            // {1} will be replaced by WizardDescriptor.Iterator.name()
            wiz.setTitleFormat(new MessageFormat("{0}"));
            wiz.setTitle("Start Projection for " + ProjectUtils.getInformation(context).getDisplayName());
            if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
                factory = iterator.getProviderFactory();
            }
        }
        if (factory != null) {
            currentlyProjected = context;
            startProjections(factory);
        }
    }

    private void startProjections(final ProjectionProviderFactory factory) {
        StatusDisplayer.getDefault().setStatusText("Starting projections, please wait...");

        final SSCESieverTopComponent outputWindow = (SSCESieverTopComponent) WindowManager.getDefault().findTopComponent("SSCESieverTopComponent");
        if (outputWindow != null && !outputWindow.isOpened()) {
            outputWindow.open();
        }
        if (outputWindow != null) {
            outputWindow.dispose();
            outputWindow.requestActive();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    outputWindow.startProjection(factory.createProjectionProviderFor(context));
                    StatusDisplayer.getDefault().setStatusText("Projections are ready!");
                    enabled = true;
                }
            });

        }
    }
}
