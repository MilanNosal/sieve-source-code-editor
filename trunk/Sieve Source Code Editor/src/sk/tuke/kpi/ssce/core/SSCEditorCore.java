package sk.tuke.kpi.ssce.core;

import sk.tuke.kpi.ssce.core.binding.JavaFilesMonitor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.document.OnSaveTask;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import sk.tuke.kpi.ssce.annotations.concerns.ChangeMonitoring;
import sk.tuke.kpi.ssce.annotations.concerns.CurrentProjectionChange;
import sk.tuke.kpi.ssce.annotations.concerns.Disposal;
import sk.tuke.kpi.ssce.annotations.concerns.SievedDocument;
import sk.tuke.kpi.ssce.annotations.concerns.Synchronization;
import sk.tuke.kpi.ssce.annotations.concerns.enums.Direction;
import sk.tuke.kpi.ssce.annotations.concerns.enums.Source;
import sk.tuke.kpi.ssce.annotations.concerns.enums.Type;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import sk.tuke.kpi.ssce.core.binding.JavaFilesMonitor.JavaFileEvent;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection.CurrentProjectionChangedEvent;
import sk.tuke.kpi.ssce.core.model.availableprojections.ProjectionsModel;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.core.model.view.ViewModel;
import sk.tuke.kpi.ssce.core.binding.Binding;
import sk.tuke.kpi.ssce.core.model.creators.ProjectionsModelCreator;
import sk.tuke.kpi.ssce.core.model.creators.ViewModelCreator;
import sk.tuke.kpi.ssce.core.model.availableprojections.JavaFileConcerns;
import sk.tuke.kpi.ssce.sieving.interfaces.CodeSiever;

/**
 * Jadro celeho modulu, teda SSCE editora. Je vložene do documentu pre pomocny
 * subor ako property. Realizuje zaujmovo orientovanu projekciu zdrojoveho kodu,
 * synchronizaciu fragmentov kodu a zobrazenie fragmentov kodu v pomocnom
 * subore.
 *
 * @author Matej Nosal
 */
public class SSCEditorCore {

    /**
     * Nastroj pre obojsmerne prepojovanie java suborov s pomocnym suborom.
     */
    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
    private final Binding bindingUtilities;
    /**
     * Nastroj na monitorovanie zmien v zdrojovom kode.
     */
    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;Monitorovanie java suborov;
    private final JavaFilesMonitor javaFilesMonitor;
    /**
     * Listener pre zmeny v pomocnom súbore .sj.
     */
    //SsceIntent:Notifikacia na zmeny v pomocnom subore .sj;
    private final SieveDocumentListener sieveDocumentListener;
    /**
     * Listener pre zmeny v java zdrojovom kode.
     */
    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
    private final JavaDocumentListener javaDocumentListener;
    /**
     * Listener pre zmenu v mapovani zamerov na fragmenty kodu.
     */
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    private final ProjectionsChangeListener concernsMappingListener;
    /**
     * Dokument pre predstavujuci pomocny subor.
     */
    //SsceIntent:Praca s pomocnym suborom;
    @SievedDocument
    private final BaseDocument sieveDocument;

    /**
     * Uvidime na co vsetko to bude dobre, ale zatial to tu davam kvoli tomu,
     * aby som sa vedel chytat na Ctrl + S
     */
    private final DataObject dataObject;

    /**
     * Aktualna konfiguracia (dopyt) zamerov na zdrojovy kod.
     */
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;
    private final CurrentProjection currentProjection;

    private final Project projectContext;

    @Synchronization(direction = Direction.SJTOJAVA)
    private final PropertyChangeListener saveListener;

    private final CurrentProjection.CurrentProjectionChangeListener currentProjectionChangeListener;

    /**
     * Model pre synchronizaciu java suborov a pomocneho suboru .sj.
     */
    //SsceIntent:Model pre synchronizaciu kodu;
    private final ViewModel viewModel;

    /**
     * Mapovanie zamerov na fragmenty kodu.
     */
    //SsceIntent:Model pre mapovanie zamerov;Notifikacia na zmeny v priradenych zamerov;
    private final ProjectionsModel projectionsModel;

    /**
     * Vytvori jadro editora modulu SSCE. Realizuje zaujmovo-oreientovanu
     * projekciu zdrojoveho kodu, synchronizaciu kodu medzi java subormi a
     * pomocnym suborom...
     *
     * @param editorCookie editorCookie pre pomocny subor nazov_projektu.sj, v
     * ktorom bude zobrazovana projekcia kodu.
     * @param projectContext projektovy kontext.
     * @throws IOException ak dojde k nejakej I/O chybe.
     */
    //SsceIntent:Praca s pomocnym suborom;Notifikacia na zmeny v java zdrojovom kode;Notifikacia na zmeny v pomocnom subore .sj;Monitorovanie java suborov;Model pre mapovanie zamerov;Prepojenie java suborov s pomocnym suborom .sj;Notifikacia na zmeny v priradenych zamerov;Model pre synchronizaciu kodu;
    public SSCEditorCore(final DataObject dataObject, Project projectContext,
            ConcernExtractor extractor, CodeSiever siever) throws IOException {

        ProgressHandle handle = ProgressHandleFactory.createHandle("Building SSCE core");

        handle.start(100);
        viewModel = new ViewModel();
        handle.progress("View created", 5);
        projectionsModel = new ProjectionsModel();
        handle.progress("Projections created", 10);
        currentProjection = new CurrentProjection();
        this.dataObject = dataObject;
        handle.progress("Data object created", 20);

        this.currentProjectionChangeListener = new CurrentProjection.CurrentProjectionChangeListener() {
            @Override
            public void projectionChanged(CurrentProjectionChangedEvent event) {
                reloadModel();
                reloadSieveDocument();
            }
        };
        currentProjection.addCurrentProjectionChangeListener(this.currentProjectionChangeListener);

        Map<Object, Object> props = new HashMap<Object, Object>();
        props.put(Constants.SSCE_CORE_OBJECT_PROP, this);
        javaFilesMonitor = new JavaFilesMonitor(FileUtil.toFile(projectContext.getProjectDirectory()).getPath(), props);
        sieveDocumentListener = new SieveDocumentListener();
        javaDocumentListener = new JavaDocumentListener();
        concernsMappingListener = new ProjectionsChangeListener();
        bindingUtilities
                = new Binding(new ViewModelCreator(extractor, siever),
                        new ProjectionsModelCreator(extractor));
        this.viewModel.setEditorCookieSieveDocument(dataObject.getLookup().lookup(EditorCookie.class));

        saveListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(DataObject.PROP_MODIFIED)) {
                    if (!((Boolean) evt.getOldValue()) & ((Boolean) evt.getNewValue())) {
                        // Starts modification
                    } else {
                        // we need to save both when the document is saved, but also when it comes back to unmodified state 
                        SSCEditorCore.this.tryToSaveModifiedJavaFiles();
                    }
                }
            }
        };
        dataObject.addPropertyChangeListener(saveListener);

        handle.progress("Listeners and binding prepared", 30);

        this.sieveDocument = (BaseDocument) this.viewModel.getEditorCookieSieveDocument().openDocument();
        this.sieveDocument.addDocumentListener(this.sieveDocumentListener);

        this.projectContext = projectContext;
        handle.progress("Context prepared", 35);

//        projectContext.getLookup().lookup(Sources.class).getSourceGroups(Source);
        reloadModel();
        handle.progress("Model created", 55);
        reloadCurrentProjections();
        handle.progress("Available projections created", 75);
        reloadSieveDocument();

        handle.progress("Document built", 95);

        this.javaFilesMonitor.addJavaFileListener(javaDocumentListener);

        this.javaFilesMonitor.addJavaFileListener(concernsMappingListener);

        handle.progress("Projection core finished", 99);

        handle.finish();
    }

    public static class CustomOnSaveTask implements OnSaveTask {

        private final Context context;

        public CustomOnSaveTask(Context ctx) {
            context = ctx;
        }

        @Override
        public void performTask() {
            System.out.println(">>> Save performed on " + 
                    NbEditorUtilities.getDataObject(context.getDocument()).toString());
        }

        @Override
        public void runLocked(Runnable r) {
            r.run();
        }

        @Override
        public boolean cancel() {
            return true;
        }

        @MimeRegistration(mimeType = "text/x-sieve-java", service = OnSaveTask.Factory.class, position = 1600)
        public static class CustomOnSaveTaskFactory implements OnSaveTask.Factory {

            @Override
            public OnSaveTask createTask(Context cntxt) {
                return new CustomOnSaveTask(cntxt);
            }

        }
    }

    public DataObject getSJDataObject() {
        return dataObject;
    }

    //SsceIntent:Monitorovanie java suborov;Dopyt na zdrojovy kod, konfiguracia zamerov;Prepojenie java suborov s pomocnym suborom .sj;Model pre synchronizaciu kodu;
    private boolean reloadModel() {
//        model.setFiles(this.javaFileUtilities.createJavaFiles(new String[]{projectContext.getProjectDirectory().getPath() + File.separator + "src"}, null));
        viewModel.setFiles(this.bindingUtilities.getViewModelCreator().createJavaFiles(this.javaFilesMonitor.getMonitoringJavaFilePaths(), currentProjection));
        return true;
    }

    //SsceIntent:Monitorovanie java suborov;Model pre mapovanie zamerov;
    private boolean reloadCurrentProjections() {
        projectionsModel.setFiles(this.bindingUtilities.getProjectionsModelCreator().createJavaFilesConcerns(this.javaFilesMonitor.getMonitoringJavaFilePaths()));
        return true;
    }

    //SsceIntent:Praca s pomocnym suborom;Notifikacia na zmeny v pomocnom subore .sj;
    @SievedDocument
    private boolean reloadSieveDocument() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                sieveDocumentListener.deactivate();
                bindingUtilities.loadSieveDocument(viewModel);
                sieveDocumentListener.activate();
            }
        });
        return true;
    }

    public void tryToSaveModifiedJavaFiles() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (JavaFile javaFile : viewModel.getFiles()) {
                    if (javaFile.isModified() && javaFile.getEditorCookie().isModified()) {
                        try {
                            //javaDocumentListener.addIgnoredFilePath(javaFile.getFilePath());
                            javaFile.getEditorCookie().saveDocument();
                            //javaDocumentListener.removeIgnoredFilePath(javaFile.getFilePath());
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        });
    }

    @Disposal
    public void dispose() {
        // this should be obsolete with JavaFilesMonitor.dispose() call, but just to make sure everything stays consistent
        this.currentProjection.removeCurrentProjectionChangeListener(this.currentProjectionChangeListener);
        this.javaFilesMonitor.removeJavaFileListener(this.javaDocumentListener);
        this.javaFilesMonitor.removeJavaFileListener(this.concernsMappingListener);

        // takes care of javaFilesMonitor but also of javaDocumentListener and concernsMappingListener
        this.javaFilesMonitor.dispose();

        this.currentProjection.dispose();
        this.dataObject.removePropertyChangeListener(this.saveListener);
        this.sieveDocument.removeDocumentListener(this.sieveDocumentListener);

        this.projectionsModel.dispose();
    }

    /**
     * Vrati prepojovaci nastroj, ktory pouziva jadro.
     *
     * @return prepojovaci nastroj.
     */
    //SsceIntent:Prepojenie java suborov s pomocnym suborom .sj;
    public Binding getBindingUtilities() {
        return bindingUtilities;
    }

    /**
     * Vrati monitorovaci nastroj, ktory pouziva jadro na monitorovanie zmien v
     * java suboroch zvoleneho projektu.
     *
     * @return monitorovaci nastroj.
     */
    //SsceIntent:Monitorovanie java suborov;
    public JavaFilesMonitor getJavaFilesMonitor() {
        return javaFilesMonitor;
    }

    /**
     * Vrati konfiguraciu zamerov (dopyt na kod), podla ktorej sa jadro prave
     * riadi.
     *
     * @return konfiguraciu zamerov (dopyt na kod).
     */
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;
    public CurrentProjection getConfiguration() {
        return currentProjection;
    }

    /**
     * Vrati model pre prepojenie java suborov s pomocnym suborom.
     *
     * @return model pre prepojenie java suborov s pomocnym suborom.
     */
    //SsceIntent:Model pre synchronizaciu kodu;
    public ViewModel getModel() {
        return viewModel;
    }

    /**
     * Vrati projektovy kontext jadra modulu SSCE.
     *
     * @return projektovy kontext jadra modulu SSCE.
     */
    public Project getProjectContext() {
        return projectContext;
    }

    /**
     * Vrati mapovanie zazmerov na fragmenty kodu vo zvolenom projekte.
     *
     * @return mapovanie zazmerov na fragmenty kodu.
     */
    //SsceIntent:Model pre mapovanie zamerov;
    public ProjectionsModel getAvailableProjections() {
        return projectionsModel;
    }

    //SsceIntent:Notifikacia na zmeny v pomocnom subore .sj;
    @Synchronization(direction = Direction.SJTOJAVA)
    @ChangeMonitoring(monitoredSource = Source.SJ, typeOfEvents = Type.GENERAL_CHANGE)
    private class SieveDocumentListener implements DocumentListener {

        private boolean active = true;

        public void activate() {
            active = true;
        }

        public void deactivate() {
            active = false;
        }

        public boolean isActive() {
            return active;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            processEvent(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processEvent(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processEvent(e);
        }

        private void processEvent(final DocumentEvent e) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (!active) {
                        return;
                    }
                    JavaFile javaFile = viewModel.getFileBySJOffset(e.getOffset());
                    if (javaFile == null) {
                        return;
                    }

                    //System.out.println("Time = " + new Date().getTime());
                    javaDocumentListener.addIgnoredFilePath(javaFile.getFilePath());

                    bindingUtilities.updateJavaDocument(viewModel, javaFile, e.getOffset());
                    javaFile.setModified(true);
                    javaDocumentListener.removeIgnoredFilePath(javaFile.getFilePath());
                }
            });

        }
    }

    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
    @Synchronization(direction = Direction.JAVATOSJ)
    @ChangeMonitoring(monitoredSource = Source.JAVA, typeOfEvents = Type.GENERAL_CHANGE)
    private class JavaDocumentListener implements JavaFilesMonitor.JavaFileChangeListener {

        private final Map<String, Long> ignoreList = new HashMap<String, Long>();

        public boolean addIgnoredFilePath(String path) {
            ignoreList.put(path, Long.MAX_VALUE);
            return true;
        }

        public boolean removeIgnoredFilePath(String path) {
            ignoreList.put(path, new Date().getTime());
            return true;
        }

        //SsceIntent:Praca s pomocnym suborom;Prepojenie java suborov s pomocnym suborom .sj;Model pre synchronizaciu kodu;
        @Override
        public void javaFileCreated(final JavaFileEvent event) {
            Long time = ignoreList.get(event.getFile().getPath());
            if (time != null) {
                if (event.getTime() <= time) {
                    //System.out.println("Skipped event javaFileCreated!");
                    return;
                } else {
                    ignoreList.remove(event.getFile().getPath());
                }
            }
            sieveDocumentListener.deactivate();
            bindingUtilities.updateSieveDocument(Binding.UpdateModelAction.INSERT, viewModel, bindingUtilities.getViewModelCreator().createJavaFile(event.getFile(), currentProjection));
//            bindingUtilities.updateSieveDocument(BindingUtilities.UpdateModelAction.INSERT, model, javaFileUtilities.createJavaFile(event.getFile(), null));
//            model.insertFile(javaFileUtilities.createJavaFile(event.getFile(), null));
            sieveDocumentListener.activate();
        }

        //SsceIntent:Praca s pomocnym suborom;Prepojenie java suborov s pomocnym suborom .sj;Model pre synchronizaciu kodu;
        @Override
        public void javaFileDeleted(final JavaFileEvent event) {
            Long time = ignoreList.get(event.getFile().getPath());
            if (time != null) {
                if (event.getTime() <= time) {
                    //System.out.println("Skipped event javaFileCreated!");
                    return;
                } else {
                    ignoreList.remove(event.getFile().getPath());
                }
            }
            sieveDocumentListener.deactivate();
            bindingUtilities.updateSieveDocument(Binding.UpdateModelAction.DELETE, viewModel, viewModel.getFileAt(event.getFile().getPath()));
            sieveDocumentListener.activate();
        }

        //SsceIntent:Praca s pomocnym suborom;Prepojenie java suborov s pomocnym suborom .sj;Model pre synchronizaciu kodu;
        @Override
        public void javaFileDocumentChanged(final JavaFileEvent event) {
            Long time = ignoreList.get(event.getFile().getPath());
            if (time != null) {
                if (event.getTime() <= time) {
                    // System.out.println("Skipped event javaFileChanged! " + event.getFile().getName());
                    return;
                } else {
                    ignoreList.remove(event.getFile().getPath());
                }
            }

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    sieveDocumentListener.deactivate();
                    bindingUtilities.updateSieveDocument(Binding.UpdateModelAction.UPDATE, viewModel, bindingUtilities.getViewModelCreator().createJavaFile(event.getFile(), currentProjection));
                    sieveDocumentListener.activate();
                }
            });
        }
    }

    //SsceIntent:Model pre mapovanie zamerov;Notifikacia na zmeny v priradenych zamerov;
    @CurrentProjectionChange(propagation = true)
    @ChangeMonitoring(monitoredSource = Source.JAVA, typeOfEvents = Type.GENERAL_CHANGE)
    private class ProjectionsChangeListener implements JavaFilesMonitor.JavaFileChangeListener {

        @Override
        public void javaFileCreated(JavaFileEvent event) {
            projectionsModel.insertFile(bindingUtilities.getProjectionsModelCreator().createJavaFileConcerns(event.getFile()));
        }

        @Override
        public void javaFileDeleted(JavaFileEvent event) {
            projectionsModel.deleteFile(projectionsModel.get(event.getFile().getPath()));
        }

        @Override
        public void javaFileDocumentChanged(JavaFileEvent event) {
            JavaFileConcerns javaFileIntents = bindingUtilities.getProjectionsModelCreator().createJavaFileConcerns(event.getFile());
            projectionsModel.updateOrInsertFile(javaFileIntents);
        }
    }
}
