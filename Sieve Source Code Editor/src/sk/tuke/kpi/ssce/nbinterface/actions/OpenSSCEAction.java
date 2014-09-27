package sk.tuke.kpi.ssce.nbinterface.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;

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
public final class OpenSSCEAction implements ActionListener {

    /**
     * Premenna uchovavajuca kontext (projekt). Sluzi pre testovanie, či zvoleny
     * projekt je podporovany.
     */
    private final Project context;

    /**
     * Vytvori novu akciu OpenSSCEAction, ktora sluzi pre spustenie SSCE
     * editora, pre zvoleny projekt.
     *
     * @param context kontext, projekt.
     */
    public OpenSSCEAction(Project context) {
        this.context = context;
    }

//    @Override
//    public boolean isEnabled() {
//        return false;
//    }
    /**
     * Metoda realizuje spustenie SSCE editora. Vytvori SsceCore a vlozi ho do
     * dokumentu pomocneho suboru ako proprety.
     *
     * @param ev udalost, ktora vyvolala tuto akciu.
     */
    //SsceIntent:Spustenie SSC Editora;
    public void actionPerformed(ActionEvent ev) {
        // TODO use context
    }

    /**
     * Metoda najde akciu podla mena akcie.
     *
     * @param actionName meno hladanej akcie.
     * @return hladanu akciu, ak ju najde, v opacnom pripade vrati null.
     */
    //SsceIntent:Vyhladanie akcie;
    public Action findAction(String actionName) {
        FileObject myActionsFolder = FileUtil.getConfigFile("Actions/Window");
        FileObject[] myActionsFolderKids = myActionsFolder.getChildren();
        for (FileObject fileObject : myActionsFolderKids) {
            //Probably want to make this more robust,
            //but the point is that here we find a particular Action:
            if (fileObject.getName().contains(actionName)) {
                try {
                    DataObject dob = DataObject.find(fileObject);
                    InstanceCookie ic = dob.getLookup().lookup(InstanceCookie.class);
                    if (ic != null) {
                        Object instance = ic.instanceCreate();
                        if (instance instanceof Action) {
                            Action a = (Action) instance;
                            return a;
                        }
                    }
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                    return null;
                }
            }
        }
        return null;
    }
}