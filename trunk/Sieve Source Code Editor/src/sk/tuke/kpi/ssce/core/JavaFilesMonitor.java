package sk.tuke.kpi.ssce.core;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Trieda realizuje monitorovanie akychkolvek zmien vo vsetkych java suboroch vo
 * zvolenom projekte.
 *
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Monitorovanie java suborov;
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
    private final HashMap<String, Document> monitoringDocs = new HashMap<String, Document>();
    /**
     * Listener pre zmeny v suboroch.
     */
    //SsceIntent:Monitorovanie zmien v java suboroch;
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
                                monitoringDocs.put(path, doc);

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
//                System.out.println("   >>>>>   fileChanged()" + fe.getFile().getName());

                JavaFilesMonitor.JavaFileEvent event = new JavaFilesMonitor.JavaFileEvent(FileUtil.toFile(fe.getFile()), JavaFilesMonitor.JavaFileEvent.Type.DOCUMENT_CHANGE_EVENT, fe.getTime());
//                notifyChangeDocumentJavaFile(event);
                notifierTask.notifyEvent(event);

//                DataObject dobj;
//                try {
//                    dobj = DataObject.find(fe.getFile());
//                    if (dobj != null) {
//                        StyledDocument doc = dobj.getCookie(EditorCookie.class).openDocument();
//                        Document doc2 = monitoringDocs.put(FileUtil.toFile(fe.getFile()).getPath(), doc);
//                        if (!doc.equals(doc2)) {
//                            System.out.println("             >>>>>   fileChanged()   document was switch  " + fe.getFile().getName());
//                            doc2.removeDocumentListener(changeDocumentListener);
//                            doc.addDocumentListener(changeDocumentListener);
//                        }
//                    }
//                } catch (DataObjectNotFoundException ex) {
//                    Exceptions.printStackTrace(ex);
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
            }
        }

        //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
        @Override
        public void fileDeleted(FileEvent fe) {
//            if ("java".equalsIgnoreCase(fe.getFile().getExt())) {
//                DataObject dobj = null;
//                try {
//                    dobj = DataObject.find(fe.getFile());
//                    if (dobj != null) {
//                        dobj.getCookie(EditorCookie.class).openDocument().removeDocumentListener(changeDocumentListener);
//                    }
//                } catch (DataObjectNotFoundException ex) {
//                    ex.printStackTrace();
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
            if ("java".equalsIgnoreCase(fe.getFile().getExt())) {
                Document doc = monitoringDocs.remove(FileUtil.toFile(fe.getFile()).getPath());
                if (doc != null) {
                    doc.removeDocumentListener(changeDocumentListener);
                }
                JavaFilesMonitor.JavaFileEvent event = new JavaFilesMonitor.JavaFileEvent(FileUtil.toFile(fe.getFile()), JavaFilesMonitor.JavaFileEvent.Type.DELETE_EVENT, fe.getTime());
//                notifyDeleteJavaFile(event);
                notifierTask.notifyEvent(event);
            }
        }

        //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
        @Override
        public void fileRenamed(FileRenameEvent fe) {
//            System.out.println("   >>>>>   fileRenamed()" + fe.getName());

            String oldPath = new File(FileUtil.toFile(fe.getFile()).getParentFile(), fe.getName() + "." + fe.getExt()).getPath();
            if ("java".equalsIgnoreCase(fe.getExt())) {
                Document doc = monitoringDocs.remove(oldPath);
                if (doc != null) {
                    doc.removeDocumentListener(changeDocumentListener);
                }
                JavaFilesMonitor.JavaFileEvent eventDelete = new JavaFilesMonitor.JavaFileEvent(new File(oldPath), JavaFilesMonitor.JavaFileEvent.Type.DELETE_EVENT, fe.getTime());
//                notifyDeleteJavaFile(eventDelete);
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
                        monitoringDocs.put(path, doc);
                    }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                JavaFilesMonitor.JavaFileEvent eventCreate = new JavaFilesMonitor.JavaFileEvent(FileUtil.toFile(fe.getFile()), JavaFilesMonitor.JavaFileEvent.Type.CREATE_EVENT, fe.getTime());
//                notifyCreationJavaFile(eventCreate);
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
//                System.out.println("path = " + path);
                JavaFilesMonitor.JavaFileEvent event = new JavaFilesMonitor.JavaFileEvent(new File(path), JavaFilesMonitor.JavaFileEvent.Type.DOCUMENT_CHANGE_EVENT, new Date().getTime());
//                notifyChangeDocumentJavaFile(event);
                notifierTask.notifyEvent(event);
            } else {
//                System.out.println("path = null");
            }
        }
    };
    /**
     * Listenery, ktore bude JavaFilesMonitor informovat o zmenach.
     */

    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
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
                        monitoringDocs.put(pathFile, doc);
                        doc.putProperty(Constants.COMPILATION_INFO_PROP, getCompilationInfo((BaseDocument) doc));
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
    public Set<String> getMonitoringJavaFilePaths() {
        return monitoringDocs.keySet();
    }

    /**
     * Obnovi sledovanie monitorovanych dokumentov.
     */
    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;Monitorovanie zmien v java dokumentoch;
    public void refreshDocumentListening() {
        for (String filepath : monitoringDocs.keySet()) {
            DataObject dobj;

            try {
                dobj = DataObject.find(FileUtil.toFileObject(new File(filepath)));
                if (dobj != null) {
                    Document oldDoc = monitoringDocs.get(filepath);
                    Document doc = dobj.getLookup().lookup(EditorCookie.class).openDocument();
                    if (oldDoc.equals(doc)) {
                        continue;
                    }
//                    System.out.println("+++ discard   " + filepath);
                    oldDoc.removeDocumentListener(changeDocumentListener);
                    doc.addDocumentListener(changeDocumentListener);
                    putProperties(doc);
                    doc.putProperty(Constants.FILE_NAME_PROP, filepath);
                    monitoringDocs.put(filepath, doc);
                    JavaFilesMonitor.JavaFileEvent event = new JavaFilesMonitor.JavaFileEvent(new File(filepath), JavaFilesMonitor.JavaFileEvent.Type.DOCUMENT_CHANGE_EVENT, new Date().getTime());

//                    notifyChangeDocumentJavaFile(event);
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
     */
    //SsceIntent:Monitorovanie zmien v java dokumentoch;
    public void stop() {
        stop = true;
        for (Document doc : monitoringDocs.values()) {
            doc.removeDocumentListener(changeDocumentListener);
        }
        monitoringDocs.clear();
    }

    /**
     * Prida listenera do tohto monitorovacieho nastroja.
     *
     * @param listener listener sledujuci zmeny v java suborch.
     */
    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
    public void addJavaFileListener(JavaFilesMonitor.JavaFileChangeListener listener) {
        this.javaFileListeners.add(listener);
    }

    /**
     * Odobere listenera z tohto monitorovacieho nastroja.
     *
     * @param listener listener sledujuci zmeny v java suborch.
     */
    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
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
        for (Map.Entry<String, Document> entry : monitoringDocs.entrySet()) {
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
     * Ziska kompilacne info pre zvoleny dokument.
     *
     * XXX: Toto moze byt uzitocne pri tom parsovani, pred zakomponovanim
     * vlastneho parsovania skusit toto. Vid. notifyListeners metodu nizsie
     *
     * @param doc dokument, ku ktoremu sa potrebne ziskat kompilacne info.
     * @return kompilacne info.
     */
    //SsceIntent:Syntakticka analyza java dokumentu;
    private CompilationInfo getCompilationInfo(BaseDocument doc) {

        final Lookup lookup = MimeLookup.getLookup("text/x-java");
        final ParserFactory parserFactory = lookup.lookup(ParserFactory.class);
        if (parserFactory == null) {
            throw new IllegalArgumentException("No parser for mime type: text/x-java");
        }
        Snapshot snapshot = Source.create(doc).createSnapshot();

        Parser p = parserFactory.createParser(Collections.singletonList(snapshot));
        final UserTask task = new UserTask() {

            @Override
            public void run(ResultIterator ri) throws Exception {
            }
        };

        Utilities.acquireParserLock();
        try {
            p.parse(snapshot, task, null);

            CompilationInfo info = CompilationInfo.get(p.getResult(task));
            ((CompilationController) info).toPhase(JavaSource.Phase.PARSED);
            return info;

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            Utilities.releaseParserLock();
        }

        return null;
    }

    /**
     * Task, ktory sluzi pre opozdnene generovanie udalosti. Posle iba jednu
     * namiesto mnohych.
     */
    //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;Monitorovanie java suborov;
    private class NotifierTask implements Runnable {

        private ArrayBlockingQueue<JavaFileEvent> queue;
        private boolean working = false;
        private Map<String, JavaFileEvent> mapEvents;

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
            if (event.getTypeEvent() == JavaFilesMonitor.JavaFileEvent.Type.CREATE_EVENT
                    || event.getTypeEvent() == JavaFilesMonitor.JavaFileEvent.Type.DOCUMENT_CHANGE_EVENT) {
                FileObject fobj = FileUtil.toFileObject(event.getFile());
                DataObject dobj;
                try {
                    dobj = DataObject.find(fobj);
                    if (dobj != null) {
                        BaseDocument bD = (BaseDocument) dobj.getLookup().lookup(EditorCookie.class).openDocument();
                        bD.putProperty(Constants.COMPILATION_INFO_PROP, getCompilationInfo(bD));
                    }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

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
