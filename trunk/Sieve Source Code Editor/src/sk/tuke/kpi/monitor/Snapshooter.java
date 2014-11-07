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
    private final Map<String, Set<String>> annModel = new HashMap<String, Set<String>>();

    private final Calendar calendar = Calendar.getInstance();

    private String printModel() {
        StringBuilder builder = new StringBuilder();
        List<String> types = new ArrayList<String>(annModel.keySet());
        Collections.sort(types);
        for (String type : types) {
            builder.append("Instances of [@").append(type).append("]:\n");
            for (String am : annModel.get(type)) {
                builder.append('\t').append(am).append('\n');
            }
            builder.append('\n');
        }
        return builder.toString();
    } 

    private void saveModel() {
        System.out.println(printModel());
        annModel.clear();
    }
    // </editor-fold>

    // <editor-fold desc="Creation, start and end, lifecycle in general" defaultstate="collapsed">
    private boolean running = false;
    private File folder;

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

        this.writerTaskHandle = this.scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (changed) {
                    // annulate change in documents
                    changed = false;
                    // but buffer the change that just happened
                    secondChange = true;
                } else if(!changed && secondChange) {
                    snapshot();
                    secondChange = false;
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    void endLogging() {
        if (!running) {
            return;
        }
        running = false;
        this.writerTaskHandle.cancel(true);
        FileUtil.removeRecursiveListener(filesChangeListener, folder);
        if (changed) {
            saveModel();
        }
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
                        BaseDocument doc = (BaseDocument) dobj.getLookup().lookup(EditorCookie.class).openDocument();
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
            (new JavaFileVisitor(info)).scan(info.getCompilationUnit(), annModel);
            doc.readUnlock();
        }

        saveModel();
    }

    private class JavaFileVisitor extends TreePathScanner<Map<String, Set<String>>, Map<String, Set<String>>> {

        private final CompilationInfo info;

        public JavaFileVisitor(CompilationInfo info) {
            this.info = info;
        }

        @Override
        public Map<String, Set<String>> visitClass(ClassTree node, Map<String, Set<String>> p) {
            parse(node, p);
            return super.visitClass(node, p);
        }

        @Override
        public Map<String, Set<String>> visitVariable(VariableTree node, Map<String, Set<String>> p) {
            return parse(node, p);
        }

        @Override
        public Map<String, Set<String>> visitMethod(MethodTree node, Map<String, Set<String>> p) {
            return parse(node, p);
        }

        @Override
        public Map<String, Set<String>> visitCompilationUnit(CompilationUnitTree node, Map<String, Set<String>> p) {
            parse(node, p);
            return super.visitCompilationUnit(node, p); //To change body of generated methods, choose Tools | Templates.
        }

        private Map<String, Set<String>> parse(Tree node, Map<String, Set<String>> p) {
            Trees trees = info.getTrees();
            TreePath path = TreePath.getPath(info.getCompilationUnit(), node);
            Element element = trees.getElement(path);
            for (AnnotationMirror am : element.getAnnotationMirrors()) {
                String key = am.getAnnotationType().asElement().toString();
                String value = am.toString() + " >< " + element.toString();
                if (!p.containsKey(key)) {
                    Set<String> list = new HashSet<String>();
                    list.add(value);
                    p.put(key, list);
                } else {
                    p.get(key).add(value);
                }
            }
            return p;
        }
    }
    // </editor-fold>
}
