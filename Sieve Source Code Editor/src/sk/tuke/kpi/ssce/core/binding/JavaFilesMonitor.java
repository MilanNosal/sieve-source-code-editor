package sk.tuke.kpi.ssce.core.binding;

import sk.tuke.kpi.ssce.core.Constants;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import sk.tuke.kpi.ssce.annotations.concerns.ChangeMonitoring;
import sk.tuke.kpi.ssce.annotations.concerns.enums.Source;
import sk.tuke.kpi.ssce.annotations.concerns.enums.Type;

/**
 * Trieda realizuje monitorovanie akychkolvek zmien vo vsetkych java suboroch vo
 * zvolenom projekte.
 *
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Monitorovanie java suborov;
@ChangeMonitoring(monitoredSource = Source.JAVA)
public class JavaFilesMonitor {

//    private final String[] rootSourcePaths;
    /**
     * Premenna indikujuca ukoncenie monitorovania java suborov.
     */
    private boolean stop;
    /**
     * Dokumenty, ktore su monitorovane.
     */
    //SsceIntent:Monitorovanie zmien v java dokumentoch;
    @ChangeMonitoring(monitoredSource = Source.JAVA)
    private final HashMap<String, Document> monitoredDocuments = new HashMap<String, Document>();
    /**
     * Listener pre zmeny v suboroch.
     */
    //SsceIntent:Monitorovanie zmien v java suboroch;
    @ChangeMonitoring(monitoredSource = Source.JAVA, typeOfEvents = Type.FILES_CHANGE)
    private final FileChangeListener changeListener = new FileChangeListener() {

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(final FileEvent fe) {
            if ("java".equalsIgnoreCase(fe.getFile().getExt())) {

                new Thread(new Runnable() {

                    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        DataObject dobj;

                        try {
                            dobj = DataObject.find(fe.getFile());
                            if (dobj != null) {
                                StyledDocument doc = dobj.getLookup().lookup(EditorCookie.class).openDocument();
                                doc.addDocumentListener(changeDocumentListener);
                                putProperties(doc);
                                String path = FileUtil.toFile(fe.getFile()).getPath();
                                doc.putProperty(Constants.FILE_NAME_PROP, path);
                                monitoredDocuments.put(path, doc);

                            }
                        } catch (DataObjectNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        JavaFilesMonitor.JavaFileEvent event = new JavaFilesMonitor.JavaFileEvent(FileUtil.toFile(fe.getFile()), JavaFilesMonitor.JavaFileEvent.Type.CREATE_EVENT, fe.getTime());
//                        notifyCreationJavaFile(event);
                        notifierTask.notifyEvent(event);
                    }
                }).start();
            }
        }

        //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
        @Override
        public void fileChanged(FileEvent fe) {
            if ("java".equalsIgnoreCase(fe.getFile().getExt())) {
                JavaFilesMonitor.JavaFileEvent event = new JavaFilesMonitor.JavaFileEvent(FileUtil.toFile(fe.getFile()), JavaFilesMonitor.JavaFileEvent.Type.DOCUMENT_CHANGE_EVENT, fe.getTime());
                notifierTask.notifyEvent(event);
            }
        }

        //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
        @Override
        public void fileDeleted(FileEvent fe) {
            if ("java".equalsIgnoreCase(fe.getFile().getExt())) {
                Document doc = monitoredDocuments.remove(FileUtil.toFile(fe.getFile()).getPath());
                if (doc != null) {
                    doc.removeDocumentListener(changeDocumentListener);
                }
                JavaFilesMonitor.JavaFileEvent event = new JavaFilesMonitor.JavaFileEvent(FileUtil.toFile(fe.getFile()), JavaFilesMonitor.JavaFileEvent.Type.DELETE_EVENT, fe.getTime());
                notifierTask.notifyEvent(event);
            }
        }

        //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            String oldPath = new File(FileUtil.toFile(fe.getFile()).getParentFile(), fe.getName() + "." + fe.getExt()).getPath();
            if ("java".equalsIgnoreCase(fe.getExt())) {
                Document doc = monitoredDocuments.remove(oldPath);
                if (doc != null) {
                    doc.removeDocumentListener(changeDocumentListener);
                }
                JavaFilesMonitor.JavaFileEvent eventDelete = new JavaFilesMonitor.JavaFileEvent(new File(oldPath), JavaFilesMonitor.JavaFileEvent.Type.DELETE_EVENT, fe.getTime());
                notifierTask.notifyEvent(eventDelete);
            }

            if ("java".equalsIgnoreCase(fe.getFile().getExt())) {
                DataObject dobj;
                try {
                    dobj = DataObject.find(fe.getFile());
                    if (dobj != null) {
                        StyledDocument doc = dobj.getLookup().lookup(EditorCookie.class).openDocument();
                        doc.addDocumentListener(changeDocumentListener);
                        putProperties(doc);
                        String path = FileUtil.toFile(fe.getFile()).getPath();
                        doc.putProperty(Constants.FILE_NAME_PROP, path);
                        monitoredDocuments.put(path, doc);
                    }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                JavaFilesMonitor.JavaFileEvent eventCreate = new JavaFilesMonitor.JavaFileEvent(FileUtil.toFile(fe.getFile()), JavaFilesMonitor.JavaFileEvent.Type.CREATE_EVENT, fe.getTime());
                notifierTask.notifyEvent(eventCreate);
            }
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    };
    
    /**
     * Listener pre zmeny v dokumentoch.
     */
    //SsceIntent:Monitorovanie zmien v java dokumentoch;
    @ChangeMonitoring(monitoredSource = Source.JAVA, typeOfEvents = Type.DOCUMENT_CHANGE)
    private final DocumentListener changeDocumentListener = new DocumentListener() {

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

        /**
         *
         */
        //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
        private void processEvent(DocumentEvent e) {
            String path = resolveJavaFilePath(e.getDocument());
            if (path != null) {
                JavaFilesMonitor.JavaFileEvent event = new JavaFilesMonitor.JavaFileEvent(new File(path), JavaFilesMonitor.JavaFileEvent.Type.DOCUMENT_CHANGE_EVENT, new Date().getTime());
                notifierTask.notifyEvent(event);
            }
        }
    };
    
    
    /**
     * Listenery, ktore bude JavaFilesMonitor informovat o zmenach.
     */
    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
    @ChangeMonitoring(monitoredSource = Source.JAVA, typeOfEvents = Type.GENERAL_CHANGE)
    private final Set<JavaFilesMonitor.JavaFileChangeListener> javaFileListeners = new HashSet<JavaFilesMonitor.JavaFileChangeListener>();
    /**
     * Priecinok, ktory je monitorovany.
     */
    //SsceIntent:Monitorovanie java suborov;
    private File folder;
    /**
     * Task pre upozornovanie listenerov na zmeny.
     */
    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
    private final NotifierTask notifierTask = new NotifierTask();
    /**
     * Hodnoty, ktore sa budu pridavat do dokumentov.
     */
    //SsceIntent:Monitorovanie zmien v java dokumentoch;
    private final Map<Object, Object> propertiesForDocs;

    /**
     * Zacne monitorovanie vo zvolenom priecinku.
     *
     * @param path cesta pre monitorovany priecinok
     * @param properties hodnoty, ktore budu pridane do dokumentov.
     */
    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;Monitorovanie java suborov;Monitorovanie zmien v java suboroch;Monitorovanie zmien v java dokumentoch;
    @ChangeMonitoring(monitoredSource = Source.JAVA)
    public JavaFilesMonitor(String path, Map<Object, Object> properties) {
        propertiesForDocs = properties;
        Thread notifierThread = new Thread(notifierTask);
        notifierThread.setDaemon(true);
        notifierThread.start();
        stop = false;
        folder = new File(path);
        FileUtil.addRecursiveListener(changeListener, folder, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return stop;
            }
        });
        for (List<String> packageFiles : getJavaFilesPaths(new String[]{path}).values()) {
            for (String pathFile : packageFiles) {

                FileObject fobj = FileUtil.toFileObject(new File(pathFile));
                DataObject dobj;

                try {
                    dobj = DataObject.find(fobj);
                    if (dobj != null) {
                        StyledDocument doc = dobj.getLookup().lookup(EditorCookie.class).openDocument();
                        doc.addDocumentListener(changeDocumentListener);
                        putProperties(doc);
                        doc.putProperty(Constants.FILE_NAME_PROP, pathFile);
                        monitoredDocuments.put(pathFile, doc);
                    }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    /**
     * Vrati vsetky cesty java subor, ktore sa nachadzuju v zvolenych
     * priecinkoch.
     *
     * @param rootSourcePaths priecinky, kde sa maju hladat java subory.
     * @return cesty java suborov v hashmape, kde kľuc je cesta k priecinku a
     * hodnota je zoznam ciest java suborov.
     */
    //SsceIntent:Vyhladanie vsetkych java suborov v priecinkoch;
    private Map<String, List<String>> getJavaFilesPaths(String[] rootSourcePaths) {
        Map<String, List<String>> paths = new HashMap<String, List<String>>();
        for (String rootPath : rootSourcePaths) {
            getJavaFilesPathsFromFolder(paths, new File(rootPath));
        }
        return paths;
    }

    /**
     * Vrati vsetky cesty java subor, ktore sa nachadzuju v zvolenom priecinku.
     * Aktualizuju pritom paths.
     *
     * @param paths do tejto premennej sa ulozia cesty java suborov.
     * @param folder priecinok, v ktorom sa hladaju java subory.
     */
    //SsceIntent:Vyhladanie vsetkych java suborov v priecinkoch;
    private void getJavaFilesPathsFromFolder(Map<String, List<String>> paths, File folder) {
        List<String> files = new ArrayList<String>();
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getPath().endsWith(".java")) {
                files.add(file.getPath());
            }
        }
        paths.put(folder.getPath(), files);

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                getJavaFilesPathsFromFolder(paths, file);
            }
        }
    }

    /**
     * Vrati aktualne monitorovane java subory.
     *
     * @return aktualne monitorovane java subory.
     */
    //SsceIntent:Monitorovanie zmien v java dokumentoch;
    @ChangeMonitoring(monitoredSource = Source.JAVA, typeOfEvents = Type.DOCUMENT_CHANGE)
    public Set<String> getMonitoringJavaFilePaths() {
        return monitoredDocuments.keySet();
    }

    /**
     * Obnovi sledovanie monitorovanych dokumentov.
     */
    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;Monitorovanie zmien v java dokumentoch;
    @ChangeMonitoring(monitoredSource = Source.JAVA, typeOfEvents = Type.DOCUMENT_CHANGE)
    public void refreshDocumentListening() {
        for (String filepath : monitoredDocuments.keySet()) {
            DataObject dobj;

            try {
                dobj = DataObject.find(FileUtil.toFileObject(new File(filepath)));
                if (dobj != null) {
                    Document oldDoc = monitoredDocuments.get(filepath);
                    Document doc = dobj.getLookup().lookup(EditorCookie.class).openDocument();
                    if (oldDoc.equals(doc)) {
                        continue;
                    }
                    oldDoc.removeDocumentListener(changeDocumentListener);
                    doc.addDocumentListener(changeDocumentListener);
                    putProperties(doc);
                    doc.putProperty(Constants.FILE_NAME_PROP, filepath);
                    monitoredDocuments.put(filepath, doc);
                    JavaFilesMonitor.JavaFileEvent event = new JavaFilesMonitor.JavaFileEvent(new File(filepath), JavaFilesMonitor.JavaFileEvent.Type.DOCUMENT_CHANGE_EVENT, new Date().getTime());

                    this.notifierTask.notifyEvent(event);
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Overi aktivnost monitorovania.
     *
     * @return true, ak monitorovanie je ukoncene, v opacnom pripade false.
     */
    public boolean isStoped() {
        return stop;
    }

    /**
     * Ukonci monitorovanie.
     * 
     * XXX: mozno sa zavesit na zavretie suboru a toto volat
     */
    //SsceIntent:Monitorovanie zmien v java dokumentoch;
    public void stop() {
        stop = true;
        for (Document doc : monitoredDocuments.values()) {
            doc.removeDocumentListener(changeDocumentListener);
        }
        monitoredDocuments.clear();
    }

    /**
     * Prida listenera do tohto monitorovacieho nastroja.
     *
     * @param listener listener sledujuci zmeny v java suborch.
     */
    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;    
    @ChangeMonitoring(monitoredSource = Source.JAVA, typeOfEvents = Type.DOCUMENT_CHANGE)
    public void addJavaFileListener(JavaFilesMonitor.JavaFileChangeListener listener) {
        this.javaFileListeners.add(listener);
    }

    /**
     * Odobere listenera z tohto monitorovacieho nastroja.
     *
     * @param listener listener sledujuci zmeny v java suborch.
     */
    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
    @ChangeMonitoring(monitoredSource = Source.JAVA, typeOfEvents = Type.DOCUMENT_CHANGE)
    public void removeJavaFileListener(JavaFilesMonitor.JavaFileChangeListener listener) {
        this.javaFileListeners.remove(listener);
    }

    /**
     * Zisti cestu java suboru pre dokument.
     *
     * @param doc dokument, ku ktoremu sa ma najst cesta k jeho java suboru.
     * @return cestu java suboru.
     */
    public String resolveJavaFilePath(Document doc) {
        for (Map.Entry<String, Document> entry : monitoredDocuments.entrySet()) {
            if (entry.getValue().equals(doc)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Vlozi hodnoty v propertiesForDocs do zvoleneho dokumentu.
     *
     * @param doc cielovy dokument.
     */
    //SsceIntent:Monitorovanie zmien v java dokumentoch;
    private void putProperties(Document doc) {
        for (Map.Entry<Object, Object> entry : propertiesForDocs.entrySet()) {
            doc.putProperty(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Task, ktory sluzi pre opozdnene generovanie udalosti. Posle iba jednu
     * namiesto mnohych.
     */
    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;Monitorovanie java suborov;
    @ChangeMonitoring(monitoredSource = Source.JAVA, typeOfEvents = Type.GENERAL_CHANGE)
    private class NotifierTask implements Runnable {

        private final ArrayBlockingQueue<JavaFileEvent> queue;
        private boolean working = false;
        private final Map<String, JavaFileEvent> mapEvents;

        /**
         * Metoda spracovava zmeny v java suboroch a posle iba jednu namiesto
         * mnohych. Ukonci sa az po zavolani stop().
         */
        @Override
        public void run() {
            JavaFileEvent event;
            this.working = true;
            this.mapEvents.clear();
            while (true) {
                try {
                    if (this.queue.isEmpty()) {
                        this.working = false;
                    }
                    event = this.queue.take();
                    if (stop) {
                        break;
                    }
                    mapEvents.put(event.file.getPath(), event);

                    this.working = true;

                    while (!this.queue.isEmpty()) {
                        event = this.queue.take();
                        mapEvents.put(event.file.getPath(), event);
                    }

                    // XXX: nie je tu zle poradie?
                    Thread.sleep(700);

                    if (this.queue.isEmpty()) {
                        //System.out.println("Notifying events   :)    :)");
                        for (JavaFileEvent e : this.mapEvents.values()) {
                            notifyListeners(e);
                        }
                        this.mapEvents.clear();
                    }
                } catch (InterruptedException e) {
                }

            }
            this.working = false;
        }

        /**
         * Oznami listenerom zmenu v java kode.
         *
         * @param event udalost
         */
        private void notifyListeners(JavaFileEvent event) {
            // XXX: zmazat ak naozaj toto netreba
//            if (event.getTypeEvent() == JavaFilesMonitor.JavaFileEvent.Type.CREATE_EVENT
//                    || event.getTypeEvent() == JavaFilesMonitor.JavaFileEvent.Type.DOCUMENT_CHANGE_EVENT) {
//                FileObject fobj = FileUtil.toFileObject(event.getFile());
//                DataObject dobj;
//                try {
//                    dobj = DataObject.find(fobj);
//                    if (dobj != null) {
//                        BaseDocument bD = (BaseDocument) dobj.getLookup().lookup(EditorCookie.class).openDocument();
//                        bD.putProperty(Constants.COMPILATION_INFO_PROP, getCompilationInfo(bD));
//                    }
//                } catch (DataObjectNotFoundException ex) {
//                    Exceptions.printStackTrace(ex);
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }

            switch (event.getTypeEvent()) {
                case CREATE_EVENT:
                    for (JavaFileChangeListener listener : javaFileListeners) {
                        listener.javaFileCreated(event);
                    }
                    break;
                case DELETE_EVENT:
                    for (JavaFileChangeListener listener : javaFileListeners) {
                        listener.javaFileDeleted(event);
                    }
                    break;
                case DOCUMENT_CHANGE_EVENT:
                    for (JavaFileChangeListener listener : javaFileListeners) {
                        listener.javaFileDocumentChanged(event);
                    }
                    break;
            }
        }

        /**
         * Vytvori sa task pre opozdnene generovanie udalosti.
         */
        private NotifierTask() {
            this.queue = new ArrayBlockingQueue<JavaFileEvent>(10000, true);
            this.mapEvents = new HashMap<String, JavaFileEvent>();
        }

        /**
         * Overi ci task pracuje.
         * Useless as I see it.
         *
         * @return true, ak pracuje, v opacnom pripade false.
         */
        public boolean isWorking() {
            return working;
        }

        /**
         * Prida udalost reprezentujucu zmenu v java kode do fronty, ktora sa
         * spracovava.
         *
         * @param e
         */
        public void notifyEvent(JavaFileEvent e) {
            this.queue.add(e);
        }
    }

    /**
     * Trieda predstavuje udalost na akukolvek zmenu java kodu (java suboru).
     */
    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;Monitorovanie java suborov;
    @ChangeMonitoring(monitoredSource = Source.JAVA, typeOfEvents = Type.GENERAL_CHANGE)
    public static class JavaFileEvent {

        /**
         * Typ zmeny java java kodu.
         */
        public enum Type {

            CREATE_EVENT, DELETE_EVENT, DOCUMENT_CHANGE_EVENT
        }
        /**
         * Zmeneny java subor.
         */
        private final File file;
        /**
         * Typ udalosti.
         */
        private final JavaFilesMonitor.JavaFileEvent.Type typeEvent;
        /**
         * Casova znamka.
         */
        private final long time;

        /**
         * Vytvori novu udalost.
         *
         * @param file zmeny subor.
         * @param typeEvent typ udalosti.
         * @param time cas vzniknutia udalosti.
         */
        public JavaFileEvent(File file, JavaFilesMonitor.JavaFileEvent.Type typeEvent, long time) {
            this.file = file;
            this.typeEvent = typeEvent;
            this.time = time;
        }

        /**
         *
         * @return zmeny subor.
         */
        public File getFile() {
            return file;
        }

        /**
         *
         * @return typ udalosti.
         */
        public JavaFilesMonitor.JavaFileEvent.Type getTypeEvent() {
            return typeEvent;
        }

        /**
         *
         * @return cas vzniku udalosti.
         */
        public long getTime() {
            return time;
        }
    }

    /**
     * Listener pre zmeny v java kode (java suboroch).
     */
    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;Monitorovanie java suborov;
    @ChangeMonitoring(monitoredSource = Source.JAVA, typeOfEvents = Type.DOCUMENT_CHANGE)
    public static interface JavaFileChangeListener extends EventListener {

        /**
         * Metoda je zavolana, keď java subor bol vytvoreny.
         *
         * @param event udalost.
         */
        public void javaFileCreated(JavaFilesMonitor.JavaFileEvent event);

        /**
         * Metoda je zavolana, ked java subor bol zmazany.
         *
         * @param event udalost.
         */
        public void javaFileDeleted(JavaFilesMonitor.JavaFileEvent event);

        /**
         * Metoda je zavolana, ked java subor bol zmeneny.
         *
         * @param event udalost.
         */
        public void javaFileDocumentChanged(JavaFilesMonitor.JavaFileEvent event);
    }
}
