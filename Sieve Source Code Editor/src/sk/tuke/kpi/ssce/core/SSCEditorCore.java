package sk.tuke.kpi.ssce.core;

import sk.tuke.kpi.ssce.core.binding.JavaFilesMonitor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import sk.tuke.kpi.ssce.core.binding.JavaFilesMonitor.JavaFileEvent;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection.CurrentProjectionChangedEvent;
import sk.tuke.kpi.ssce.core.model.possibleprojections.ProjectConcerns;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.core.model.view.ViewModel;
import sk.tuke.kpi.ssce.core.binding.Binding;
import sk.tuke.kpi.ssce.core.model.possibleprojections.JavaFileConcerns;

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
    private final ListenerForIntentsMapping listenerForIntentsMapping;
    /**
     * Dokument pre predstavujuci pomocny subor.
     */
    //SsceIntent:Praca s pomocnym suborom;
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
    private final CurrentProjection configuration;
    private Project projectContext;
    /**
     * Model pre synchronizaciu java suborov a pomocneho suboru .sj.
     */
    //SsceIntent:Model pre synchronizaciu kodu;
    private final ViewModel model;
    /**
     * Mapovanie zamerov na fragmenty kodu.
     */
    //SsceIntent:Model pre mapovanie zamerov;Notifikacia na zmeny v priradenych zamerov;
    private final ProjectConcerns intentsMapping;

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
    public SSCEditorCore(final DataObject dataObject, Project projectContext) throws IOException {
        model = new ViewModel();
        intentsMapping = new ProjectConcerns();
        configuration = new CurrentProjection();
        this.dataObject = dataObject;

        configuration.addConfigurationChangeListener(new CurrentProjection.CurrentProjectionChangeListener() {
            @Override
            public void projectionChanged(CurrentProjectionChangedEvent event) {
                reloadModel();
                reloadSieveDocument();
            }
        });
        Map<Object, Object> props = new HashMap<Object, Object>();
        props.put(Constants.SSCE_CORE_OBJECT_PROP, this);
        javaFilesMonitor = new JavaFilesMonitor(FileUtil.toFile(projectContext.getProjectDirectory()).getPath(), props);
        sieveDocumentListener = new SieveDocumentListener();
        javaDocumentListener = new JavaDocumentListener();
        listenerForIntentsMapping = new ListenerForIntentsMapping();
        bindingUtilities = new Binding();
        this.model.setEditorCookieSieveDocument(dataObject.getCookie(EditorCookie.class));
        
        
//        DataObject.getRegistry().addChangeListener(new ChangeListener() {
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                System.out.println(">>>>>>>>>>>>>>>>>>> " + e.toString());
//                System.out.println(">>>>>>>>>>>>>>> " + e.getSource());
//                for (DataObject dobj : DataObject.getRegistry().getModified()) {
//                    System.out.println(">>> " + dobj.getName());
//                    System.out.println("::: " + dobj.equals(dataObject));
//                }
//            }
//        });
        
        
        this.sieveDocument = (BaseDocument) this.model.getEditorCookieSieveDocument().openDocument();
        this.sieveDocument.addDocumentListener(this.sieveDocumentListener);
        // vyzera to tak ze document listener je volany v locku a nemal by mutovat dokument
//        this.sieveDocument.addDocumentListener(new DocumentListener() {
//
//            private void saveDocument() {
//                try {
//                    ((BaseDocument)model.getEditorCookieSieveDocument().getDocument()).extWriteLock();
//                    model.getEditorCookieSieveDocument().saveDocument();
//                    ((BaseDocument)model.getEditorCookieSieveDocument().getDocument()).extWriteUnlock();
//                } catch (Throwable ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                saveDocument();
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                saveDocument();
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                saveDocument();
//            }
//        });
        this.projectContext = projectContext;

//        projectContext.getLookup().lookup(Sources.class).getSourceGroups(Source);

        reloadModel();
        reloadIntentsMapping();
        reloadSieveDocument();

        this.javaFilesMonitor.addJavaFileListener(javaDocumentListener);

        this.javaFilesMonitor.addJavaFileListener(listenerForIntentsMapping);

        EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {
            
            // XXX: toto je tu naco?

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
                    JTextComponent component = evt.getNewValue() != null && evt.getNewValue() instanceof JTextComponent ? ((JTextComponent) evt.getNewValue()) : null;
                    if (component != null && component.getDocument() != null && SSCEditorCore.this.equals(component.getDocument().getProperty(Constants.SSCE_CORE_OBJECT_PROP))) {
//                        bindingUtilities.markGuardedSieveDoument((StyledDocument) component.getDocument(), model);
                        System.out.println("--- focus gained + refreshDocumentListening --- time: " + new Date().toString());
                        javaFilesMonitor.refreshDocumentListening();
                    }
                }
            }
        });

    }

    //SsceIntent:Monitorovanie java suborov;Dopyt na zdrojovy kod, konfiguracia zamerov;Prepojenie java suborov s pomocnym suborom .sj;Model pre synchronizaciu kodu;
    private boolean reloadModel() {
//        model.setFiles(this.javaFileUtilities.createJavaFiles(new String[]{projectContext.getProjectDirectory().getPath() + File.separator + "src"}, null));
        model.setFiles(this.bindingUtilities.getJavaFileUtilities().createJavaFiles(this.javaFilesMonitor.getMonitoringJavaFilePaths(), configuration));
        return true;
    }

    //SsceIntent:Monitorovanie java suborov;Model pre mapovanie zamerov;
    private boolean reloadIntentsMapping() {
        intentsMapping.setFiles(this.bindingUtilities.getJavaFileUtilities().createJavaFilesIntents(this.javaFilesMonitor.getMonitoringJavaFilePaths()));
        return true;
    }

    //SsceIntent:Praca s pomocnym suborom;Notifikacia na zmeny v pomocnom subore .sj;
    private boolean reloadSieveDocument() {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                sieveDocumentListener.deactivate();
                bindingUtilities.loadSieveDocument(model);
                sieveDocumentListener.activate();
            }
        });
        return true;
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
        return configuration;
    }

    /**
     * Vrati model pre prepojenie java suborov s pomocnym suborom.
     *
     * @return model pre prepojenie java suborov s pomocnym suborom.
     */
    //SsceIntent:Model pre synchronizaciu kodu;
    public ViewModel getModel() {
        return model;
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
    public ProjectConcerns getIntentsMapping() {
        return intentsMapping;
    }

    //SsceIntent:Notifikacia na zmeny v pomocnom subore .sj;
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
                    JavaFile javaFile = model.getFileBySJOffset(e.getOffset());
                    if (javaFile == null) {
                        return;
                    }

                    //System.out.println("Time = " + new Date().getTime());
                    javaDocumentListener.addIgnoredFilePath(javaFile.getFilePath());

                    bindingUtilities.updateJavaDocument(model, javaFile, e.getOffset());
                    javaDocumentListener.removeIgnoredFilePath(javaFile.getFilePath());
                }
            });

        }
    }

    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
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
            bindingUtilities.updateSieveDocument(Binding.UpdateModelAction.INSERT, model, bindingUtilities.getJavaFileUtilities().createJavaFile(event.getFile(), configuration));
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
                    System.out.println("Skipped event javaFileCreated!");
                    return;
                } else {
                    ignoreList.remove(event.getFile().getPath());
                }
            }
            sieveDocumentListener.deactivate();
            bindingUtilities.updateSieveDocument(Binding.UpdateModelAction.DELETE, model, model.getFileAt(event.getFile().getPath()));
            sieveDocumentListener.activate();
        }

        //SsceIntent:Praca s pomocnym suborom;Prepojenie java suborov s pomocnym suborom .sj;Model pre synchronizaciu kodu;
        @Override
        public void javaFileDocumentChanged(final JavaFileEvent event) {

            Long time = ignoreList.get(event.getFile().getPath());
            if (time != null) {
                if (event.getTime() <= time) {
                    System.out.println("Skipped event javaFileCreated!");
                    return;
                } else {
                    ignoreList.remove(event.getFile().getPath());
                }
            }

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    sieveDocumentListener.deactivate();
                    bindingUtilities.updateSieveDocument(Binding.UpdateModelAction.UPDATE, model, bindingUtilities.getJavaFileUtilities().createJavaFile(event.getFile(), configuration));
                    sieveDocumentListener.activate();

                }
            });
        }
    }

    //SsceIntent:Model pre mapovanie zamerov;Notifikacia na zmeny v priradenych zamerov;
    private class ListenerForIntentsMapping implements JavaFilesMonitor.JavaFileChangeListener {

        @Override
        public void javaFileCreated(JavaFileEvent event) {
            intentsMapping.insertFile(bindingUtilities.getJavaFileUtilities().createJavaFileConcerns(event.getFile()));
        }

        @Override
        public void javaFileDeleted(JavaFileEvent event) {
            intentsMapping.deleteFile(intentsMapping.get(event.getFile().getPath()));
        }

        @Override
        public void javaFileDocumentChanged(JavaFileEvent event) {
            JavaFileConcerns javaFileIntents = bindingUtilities.getJavaFileUtilities().createJavaFileConcerns(event.getFile());
            intentsMapping.updateOrInsertFile(javaFileIntents);

//            if (intentsMapping.updateFile(javaFileIntents) == null) {
//                intentsMapping.insertFile(javaFileIntents);
//            }
        }
    }
}
