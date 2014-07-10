package sk.tuke.kpi.ssce.nbinterface.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.ErrorManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import sk.tuke.kpi.ssce.core.Constants;
import sk.tuke.kpi.ssce.core.SSCEditorCore;

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
        System.out.println("project name class = " + context.getClass().getSimpleName());

//        if (context != null && (!context.getClass().getSimpleName().equals("J2SEProject")
//                && !context.getClass().getSimpleName().equals("J2MEProject"))) {
//            
//            String msg = context.getClass().getSimpleName()+ " is not supported type of project!\nSelect J2ME or J2SE projects.";
//            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
//            return;
//        }
        File file = new File(context.getProjectDirectory().getPath() + File.separator + ProjectUtils.getInformation(context).getDisplayName() + ".sj");
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
                //if (doc.getProperty(Constants.SSCE_CORE_OBJECT_PROP) == null) {
                doc.putProperty(Constants.SSCE_CORE_OBJECT_PROP, new SSCEditorCore(dobj, context));
                //}

//                if (ec.getOpenedPanes() == null) {
//                    new SSCEditorCore(ec, context);
//                }
                Action action = findAction("SsceTopComponent");
                if (action != null) {
                    action.actionPerformed(null);
                }

                ec.open();

//                new SsceViewConfigTopComponent().open();
//                new SsceTagManagerTopComponent().open();
            }
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

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
