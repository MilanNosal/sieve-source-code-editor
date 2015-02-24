package sk.tuke.kpi.monitor;

// <editor-fold desc="Imports" defaultstate="collapsed">
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import static sk.tuke.kpi.ssce.core.CompilationUtilities.getCompilationInfo;
// </editor-fold>

@ActionID(
        category = "File",
        id = "sk.tuke.kpi.snapshooter.Snapshooter"
)
@ActionRegistration(
        iconBase = "sk/tuke/kpi/monitor/monitor.png",
        displayName = "#CTL_Snapshooter"
)
@ActionReference(path = "Toolbars/File", position = 2200)
@Messages("CTL_Snapshooter=Snapshooter")
public final class Snapshooter implements ActionListener {

    private final Project context;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean changed = false;
    private boolean secondChange = false;
    private ScheduledFuture<?> writerTaskHandle;

    private String lastElements = "";

    private File logFile;
    private RandomAccessFile raf;
    private static Snapshooter instance;

    public static Snapshooter getInstance() {
        return instance;
    }

    // <editor-fold desc="Monitored documents lifecycle" defaultstate="collapsed">
    public final HashMap<String, BaseDocument> monitoredDocuments = new HashMap<String, BaseDocument>();

    private final FileChangeListener filesChangeListener = new FileChangeListener() {

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(final FileEvent fe) {
            if ("java".equalsIgnoreCase(fe.getFile().getExt())) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        DataObject dobj;

                        try {
                            dobj = DataObject.find(fe.getFile());
                            if (dobj != null) {
                                BaseDocument doc = (BaseDocument) dobj.getLookup().lookup(EditorCookie.class).openDocument();
                                String path = FileUtil.toFile(fe.getFile()).getPath();
                                monitoredDocuments.put(path, doc);
                            }
                        } catch (DataObjectNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
        }

        //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
        @Override
        public void fileChanged(FileEvent fe) {
            changed = true;
        }

        //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
        @Override
        public void fileDeleted(FileEvent fe) {
            if ("java".equalsIgnoreCase(fe.getFile().getExt())) {
                monitoredDocuments.remove(FileUtil.toFile(fe.getFile()).getPath());
            }
        }

        //SsceIntent:Notifikacia na zmeny v java zdrojovom kode;
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            String oldPath = new File(FileUtil.toFile(fe.getFile()).getParentFile(), fe.getName() + "." + fe.getExt()).getPath();
            if ("java".equalsIgnoreCase(fe.getExt())) {
                monitoredDocuments.remove(oldPath);
            }

            if ("java".equalsIgnoreCase(fe.getFile().getExt())) {
                DataObject dobj;
                try {
                    dobj = DataObject.find(fe.getFile());
                    if (dobj != null) {
                        BaseDocument doc = (BaseDocument) dobj.getLookup().lookup(EditorCookie.class).openDocument();
                        String path = FileUtil.toFile(fe.getFile()).getPath();
                        monitoredDocuments.put(path, doc);
                    }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    };
    // </editor-fold>

    // <editor-fold desc="Model serialization" defaultstate="collapsed">   
    private final Map<String, Set<String>> elementModel = new HashMap<String, Set<String>>();
    private final Map<String, Set<Pair>> annModel = new HashMap<String, Set<Pair>>();

    private static class Pair {

        public String annotationMirror;
        public String targetElement;

        public Pair(String annotationMirror, String targetElement) {
            this.annotationMirror = annotationMirror;
            this.targetElement = targetElement;
        }
    }

    private void saveModel() {
        serialize();
        annModel.clear();
        elementModel.clear();
    }

    private void prepareLogFile() throws IOException {
        if (raf == null) {
            String filePath = UserInteractionMonitorPanel.getSnapshotPath();
            logFile = new File(filePath);
            if (logFile.exists()) { // append
                raf = new RandomAccessFile(logFile, "rw");
                if (raf.length() < 49) { // arcane constant for determining valid log file
                    raf.seek(0);
                    raf.write(startLogFileString.getBytes("UTF-8"));
                } else {
                    raf.seek(raf.length() - endLogFileString.length());
                }
            } else { // create new
                raf = new RandomAccessFile(logFile, "rw");
                raf.write(startLogFileString.getBytes("UTF-8"));
            }
        } // here it should be ready for writing a session
    }

    private void serialize() {
        try {
            if (raf == null) {
                prepareLogFile();
            }
            String elements = serializeModelElements();
            if (elements.equals(this.lastElements)) {
                System.out.println(">>> skipping");
                return;
            }
            this.lastElements = elements;
            long millis = System.currentTimeMillis();
            calendar.setTimeInMillis(millis);
            String intro = String.format("\t<snapshot date=\"%1$tF %1$tT\" timestamp=\"%2$d\">\n", calendar, millis);
            raf.write(intro.getBytes("UTF-8"));
            raf.write(serializeModelElements().getBytes("UTF-8"));
            raf.write(serializeModelAnnotations().getBytes("UTF-8"));
            raf.write("\t</snapshot>\n".getBytes("UTF-8"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    // </editor-fold>

    // <editor-fold desc="Snapshot format" defaultstate="collapsed">   
    private final Calendar calendar = Calendar.getInstance();

    private String serializeModelElements() {
        StringBuilder builder = new StringBuilder(5000);
        builder.append("\t\t<elementDriven>\n");
        List<String> list = new ArrayList<String>(elementModel.keySet());
        Collections.sort(list);
        for (String element : list) {
            builder.append("\t\t\t<element>\n\t\t\t\t<name>").append(element).append("</name>\n");
            for (String am : elementModel.get(element)) {
                builder.append("\t\t\t\t<annotation>").append(am).append("</annotation>\n");
            }
            builder.append("\t\t\t</element>\n");
        }
        builder.append("\t\t</elementDriven>\n");
        return builder.toString();
    }

    private String serializeModelAnnotations() {
        StringBuilder builder = new StringBuilder(5000);
        builder.append("\t\t<annotationDriven>\n");
        List<String> list = new ArrayList<String>(annModel.keySet());
        Collections.sort(list);
        for (String annType : list) {
            builder.append("\t\t\t<annotationType name=\"").append(annType).append("\">\n");
            for (Pair pair : annModel.get(annType)) {
                builder.append("\t\t\t\t<annotationMirror>\n\t\t\t\t\t<target>")
                        .append(pair.targetElement).append("</target>\n\t\t\t\t\t<annotation>")
                        .append(pair.annotationMirror)
                        .append("</annotation>\n\t\t\t\t</annotationMirror>\n");
            }
            builder.append("\t\t\t</annotationType>\n");
        }
        builder.append("\t\t</annotationDriven>\n");
        return builder.toString();
    }

    private final String startLogFileString = "<annotationSnapshot>\n";

    private final String endLogFileString = "</annotationSnapshot>\n";
    // </editor-fold>

    // <editor-fold desc="Creation, start and end, lifecycle in general" defaultstate="collapsed">
    private boolean running = false;
    private final File folder;

    public Snapshooter(Project context) {
        this.context = context;
        folder = new File(FileUtil.toFile(context.getProjectDirectory()).getPath());
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (instance != null && this.context.equals(instance.context)) {
            instance.endLogging();
            instance = null;
        } else {
            if (instance != null) {
                instance.endLogging();
            }
            instance = this;
            startLogging();
        }
    }

    private void startLogging() {
        if (running) {
            return;
        }
        running = true;

        FileUtil.addRecursiveListener(filesChangeListener, folder, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return !running;
            }
        });

        initMonitoredDocuments(FileUtil.toFile(context.getProjectDirectory()).getPath());
        snapshot();
        saveModel();

        this.writerTaskHandle = this.scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (changed) {
                    // annulate change in documents
                    changed = false;
                    // but buffer the change that just happened
                    secondChange = true;
                } else if (!changed && secondChange) {
                    snapshot();
                    saveModel();
                    secondChange = false;
                }
            }
        }, 60, 20, TimeUnit.SECONDS);
    }

    void endLogging() {
        if (!running) {
            return;
        }
        this.writerTaskHandle.cancel(false);
        if (raf != null) {
            try {
                if (changed || secondChange) {
                    snapshot();
                    saveModel();
                }
                raf.write(endLogFileString.getBytes("UTF-8"));
                raf.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        running = false;
        FileUtil.removeRecursiveListener(filesChangeListener, folder);
    }
    // </editor-fold>

    // <editor-fold desc="Monitored documents initialization" defaultstate="collapsed">
    private void initMonitoredDocuments(String path) {
        for (List<String> packageFiles : getJavaFilesPaths(new String[]{path}).values()) {
            for (String pathFile : packageFiles) {

                FileObject fobj = FileUtil.toFileObject(new File(pathFile));
                DataObject dobj;

                try {
                    dobj = DataObject.find(fobj);

                    if (dobj != null) {
                        BaseDocument doc = (BaseDocument) dobj.getLookup().lookup(EditorCookie.class
                        ).openDocument();
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

    private Map<String, List<String>> getJavaFilesPaths(String[] rootSourcePaths) {
        Map<String, List<String>> paths = new HashMap<String, List<String>>();
        for (String rootPath : rootSourcePaths) {
            getJavaFilesPathsFromFolder(paths, new File(rootPath));
        }
        return paths;
    }

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
    // </editor-fold>

    // <editor-fold desc="Visitor for annotations extraction, model update" defaultstate="collapsed">
    private void snapshot() {
        List<BaseDocument> list = new LinkedList<BaseDocument>(this.monitoredDocuments.values());
        for (BaseDocument doc : list) {
            doc.readLock();
            CompilationInfo info = getCompilationInfo(doc);
            if (info == null) {
                break;
            }
            (new JavaFileVisitor(info)).scan(info.getCompilationUnit(), null);
            doc.readUnlock();
        }
    }

    private class JavaFileVisitor extends TreePathScanner<Void, Void> {

        private final CompilationInfo info;

        public JavaFileVisitor(CompilationInfo info) {
            this.info = info;
        }

        @Override
        public Void visitClass(ClassTree node, Void p) {
            parse(node);
            Void v = super.visitClass(node, p);
            return v;
        }

        @Override
        public Void visitVariable(VariableTree node, Void p) {
            parse(node);
            return p;
        }

        @Override
        public Void visitMethod(MethodTree node, Void p) {
            parse(node);
            return p;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
            parse(node);
            return super.visitCompilationUnit(node, p); //To change body of generated methods, choose Tools | Templates.
        }

        private void parse(Tree node) {
            Trees trees = info.getTrees();
            TreePath path = TreePath.getPath(info.getCompilationUnit(), node);
            Element element = trees.getElement(path);
            String elementName = tryToBuildFullName(element);
            Set<String> annotationsFor = null;
            if (elementModel.containsKey(elementName)) {
                annotationsFor = elementModel.get(elementName);
            } else if (element.getAnnotationMirrors().size() > 0) {
                annotationsFor = new HashSet<String>();
                elementModel.put(elementName, annotationsFor);
            }
            for (AnnotationMirror am : element.getAnnotationMirrors()) {
                String annTypeName = am.getAnnotationType().asElement().toString();
                String annotationMirrorName = am.toString();
                // for the element model
                annotationsFor.add(annotationMirrorName);

                // for the annotations model
                if (!annModel.containsKey(annTypeName)) {
                    Set<Pair> set = new HashSet<Pair>();
                    set.add(new Pair(annotationMirrorName, elementName));
                    annModel.put(annTypeName, set);
                } else {
                    annModel.get(annTypeName).add(new Pair(annotationMirrorName, elementName));
                }
            }
        }
        
        private String tryToBuildFullName(Element element) {
            String fullName = "";
            boolean firstIteration = true;
            while (element != null && !element.getKind().equals(ElementKind.PACKAGE)) {
                fullName = element.toString() + (firstIteration ? "" : ".") + fullName;
                firstIteration = false;
                element = element.getEnclosingElement();
            }
            return fullName;
        }
    }
    // </editor-fold>
}
