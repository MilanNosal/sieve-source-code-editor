package sk.tuke.kpi.ssce.core.utilities;

import sk.tuke.kpi.ssce.core.model.view.JavaFileVisitor;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.SourcePositions;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.modules.parsing.api.*;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import sk.tuke.kpi.ssce.core.Constants;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;
import sk.tuke.kpi.ssce.core.model.view.CodeSnippet;
import sk.tuke.kpi.ssce.core.model.view.importshandling.Imports;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.core.model.possibleprojections.JavaFileConcerns;
import sk.tuke.kpi.ssce.core.model.possibleprojections.JavaFileConcernsVisitor;

/**
 * Trieda reprezentuje nastroj pre pracu s java subormi.
 * @author Matej Nosal
 */
//SsceIntent:Praca s java suborom;
public class JavaFileUtilities {

    /**
     * Metoda vytvori mapovanie zamerov na fragmenty kodu pre jeden java subor.
     * @param javaFile java subor pre ktory sa ma vytvorit mapovanie zamerov.
     * @return mapovanie zamerov na fragmenty kodu pre jeden java subor.
     */
    //SsceIntent:Model pre mapovanie zamerov;
    public JavaFileConcerns createJavaFileIntents(File javaFile) {
//        System.out.println("\n-----------INTENTS-------------------- " + javaFile.getName());

        FileObject fobj = FileUtil.toFileObject(javaFile);
        DataObject dobj;
        try {
            dobj = DataObject.find(fobj);
            if (dobj != null) {
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                return processCodeIntentsInDocument(ec);
            }
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //iba pre ziskanie vsetkych zamerov pre dokument
    //SsceIntent:Model pre mapovanie zamerov;
    private JavaFileConcerns processCodeIntentsInDocument(EditorCookie ec) {
        BaseDocument doc;
        try {
            doc = (BaseDocument) ec.openDocument();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }

        JavaFileConcerns javaFile;

        doc.readLock();
        try {
            CompilationInfo info = getCompilationInfo(doc);
            if (info == null) {
                return null;
            }

            CompilationUnitTree cu = info.getCompilationUnit();
            SourcePositions sp = info.getTrees().getSourcePositions();
            JavaFileConcernsVisitor scanner = new JavaFileConcernsVisitor(info, cu, sp, doc);
            javaFile = scanner.scan(cu, new JavaFileConcerns(FileUtil.toFile(info.getFileObject()).getPath(), info.getFileObject().getName()));
            javaFile.sortCodes();

        } finally {
            doc.readUnlock();
        }
        return javaFile;
    }

    /**
     * Metoda zrealizuje zujmovo-orientovanu projekciu zdrojoveho kodu a vytvori model pre prepojenie java subora s pomocnym suborom .sj.
     * @param javaFile java subor nad ktorym sa ma zdrealizovat projekcia kodu.
     * @param configuration konfiguracia zamerov (dopyt na kod).
     * @return zatial nekonzistentny model pre prepojenie java subora s pomocnym suborom .sj.
     */
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;Realizovanie projekcie zdrojoveho kodu;Model pre synchronizaciu kodu;
    public JavaFile createJavaFile(File javaFile, CurrentProjection configuration) {
//        System.out.println("\n------------------------------- " + javaFile.getName());

        FileObject fobj = FileUtil.toFileObject(javaFile);
        DataObject dobj;
        try {
            dobj = DataObject.find(fobj);
            if (dobj != null) {
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                return processDocument(ec, configuration);
            }
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // pre ziskanie fragmentov kodu pre danu konfiguraciu
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;Realizovanie projekcie zdrojoveho kodu;Model pre synchronizaciu kodu;
    private JavaFile processDocument(EditorCookie ec, CurrentProjection configuration) {
        BaseDocument doc;
        try {
            doc = (BaseDocument) ec.openDocument();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }

        JavaFile javaFile;

        doc.readLock();
        try {
            //SsceIntent:Syntakticka analyza java dokumentu;
            CompilationInfo info = getCompilationInfo(doc);
            if (info == null) {
                return null;
            }

            CompilationUnitTree cu = info.getCompilationUnit();
            SourcePositions sp = info.getTrees().getSourcePositions();
            JavaFileVisitor scanner = new JavaFileVisitor(info, cu, sp, configuration, doc);
            javaFile = scanner.scan(cu, new JavaFile(FileUtil.toFile(info.getFileObject()).getPath(), info.getFileObject().getName(), ec));

            keepOnlyUnguardedCodes(javaFile, doc); //keeps codes which does not contain guarded blocks

            keepOnlyRootCodes(javaFile); // start getting root codes: to remove overlaping in codes


            // start getting necesary and editable imports
            Set<String> allImportedTypes = javaFile.getAllImports().getAllTypeIdentifiers();
            Set<String> editableImportedTypes = new HashSet<String>(allImportedTypes);
            Set<String> necessaryImportedTypes = new HashSet<String>();

            TokenHierarchy th = info.getTokenHierarchy();
            if (th != null && th.isActive()) {
                TokenSequence seq = th.tokenSequence();
                for (seq.moveStart(); seq.moveNext();) {
                    Token token = seq.token();
                    int offset = seq.offset();
                    if (JavaTokenId.IDENTIFIER.equals(token.id()) && !isInImportsArea(javaFile, offset)) {
                        String identifier = token.text().toString();
                        if (isInCodeArea(javaFile, offset)) {
                            if (allImportedTypes.contains(identifier)) {
                                necessaryImportedTypes.add(identifier);
                            }
                        } else {
                            editableImportedTypes.remove(identifier);
                        }
                    }
                }
            }

            javaFile.getAllImports().setEditableAllImports(false);

            for (String necessaryImport : necessaryImportedTypes) {
                Imports.Import import1 = javaFile.getAllImports().findImport(necessaryImport);
                if (editableImportedTypes.contains(necessaryImport)) {
                    import1.setEditable(true);
                }
                javaFile.getNecessaryImports().addImport(import1);
            }
            // end getting necesary and editable imports





        } finally {
            doc.readUnlock();
        }
        return javaFile;
    }

    //SsceIntent:Realizovanie projekcie zdrojoveho kodu;
    private boolean keepOnlyRootCodes(JavaFile file) {
        List<CodeSnippet> codes = file.getCodeSnippets();
        Collections.sort(codes);
        for (int i = 1; i < codes.size();) {
            if (codes.get(i - 1).getCodeBinding().getStartPositionJavaDocument() <= codes.get(i).getCodeBinding().getStartPositionJavaDocument()
                    && codes.get(i - 1).getCodeBinding().getEndPositionJavaDocument() >= codes.get(i).getCodeBinding().getStartPositionJavaDocument()) {
                codes.remove(i);
                continue;
            }
            i++;
        }
        return true;
    }

    //SsceIntent:Realizovanie projekcie zdrojoveho kodu;
    private boolean keepOnlyUnguardedCodes(JavaFile file, BaseDocument doc) {
        if (doc instanceof GuardedDocument) {
            List<CodeSnippet> codes = file.getCodeSnippets();
//            GuardedDocument guardedDocument = (GuardedDocument) doc;
            MarkBlock chain = ((GuardedDocument) doc).getGuardedBlockChain().getChain();

            MarkBlock blk = chain;
            while (blk != null) {
                for (int i = 0; i < codes.size();) {
                    if (codes.get(i).getCodeBinding().getStartPositionJavaDocument() <= blk.getEndOffset() && blk.getStartOffset() <= codes.get(i).getCodeBinding().getEndPositionJavaDocument()) {
//                        System.out.println("Code(" + codes.get(i).getCodeBinding().getStartPositionJavaDocument() + ", " + codes.get(i).getCodeBinding().getEndPositionJavaDocument() + ") was removed from :" + file.getFilePath());
                        codes.remove(i);
                        continue;
                    }
                    i++;
                }
                blk = blk.getNext();
            }
        }
        return true;
    }

    private boolean isInCodeArea(JavaFile jf, int offset) {
        for (CodeSnippet code : jf.getCodeSnippets()) {
            if (code.getCodeBinding().getStartPositionJavaDocument() <= offset && offset < code.getCodeBinding().getEndPositionJavaDocument()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInImportsArea(JavaFile jf, int offset) {
        return jf.getImportsBinding().getStartPositionJavaDocument() <= offset && offset < jf.getImportsBinding().getEndPositionJavaDocument();
    }

    /**
     * Metoda zrealizuje projekciu zdrojoveho kodu celeho projektu, na zaklade priecinkov so zdrojovymi kodmi a konfiguracii zamerov (dopytu na kod).
     * @param rootSourcePaths cesty priecinkov obsahujucich java subory.
     * @param configuration konfiguracia zamerov (dopyt na kod).
     * @return zatial nekonzistentny model pre prepojenie java suborov s pomocnym suborom .sj.
     */
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;Realizovanie projekcie zdrojoveho kodu;Model pre synchronizaciu kodu;
    public List<JavaFile> createJavaFiles(String[] rootSourcePaths, CurrentProjection configuration) {

        List<JavaFile> javaFiles = new ArrayList<JavaFile>();
        JavaFile jf;
        for (List<String> packageFiles : getJavaFilesPaths(rootSourcePaths).values()) {
            for (String pathFile : packageFiles) {
                jf = createJavaFile(new File(pathFile), configuration);
                if (jf.getCodeSnippets() != null && !jf.getCodeSnippets().isEmpty()) {
                    javaFiles.add(jf);
                }
            }
        }
        Collections.sort(javaFiles, JavaFile.SORT_FILES_BY_PACKAGES);
        return javaFiles;
    }

    //SsceIntent:Vyhladanie vsetkych java suborov v priecinkoch;
    private HashMap<String, List<String>> getJavaFilesPaths(String[] rootSourcePaths) {
        HashMap<String, List<String>> paths = new HashMap<String, List<String>>();
        for (String rootPath : rootSourcePaths) {
            getJavaFilesPathsFromFolder(paths, new File(rootPath));
        }
        return paths;
    }

    //SsceIntent:Vyhladanie vsetkych java suborov v priecinkoch;
    private void getJavaFilesPathsFromFolder(HashMap<String, List<String>> paths, File folder) {
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

//    private List<JavaFile> processDocument(BaseDocument doc, String filePath, IntentsConfiguration configuration) {
//        final CompilationInfo info = getCompilationInfo(doc);
//        if (info == null) {
//            return new ArrayList<JavaFile>();
//        }
//
//        final CompilationUnitTree cu = info.getCompilationUnit();
//
//        return new ArrayList<JavaFile>();
//    }

    /**
     * Extrahuje kompilacne info pre zdrojovy kod v dokumente doc. Predtym to tahal
     * Mato z kontextu ako property, teraz to vytvaram zakazdym nanovo. Zda sa,
     * ze v tom starom modeli niekedy nebol syncnuty aktualny stav s kompilacnym info
     * a preto mi vracievalo null.
     * @param doc dukument so zdrojovym kodom.
     * @return kompilacne info pre zdrojovy kod v dokumente doc.
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
     * Skonstruuje kompilacne info pre zdrojovy kode v dokumente doc od offset o dlzke length.
     * @param doc dukument so zdrojovym kodom.
     * @param offset offset zaciatku useku zdrojoveho kodu, pre ktory sa ma kompilacne info vytvorit.
     * @param length dlzka useku zdrojoveho kodu, pre ktory sa ma kompilacne info vytvorit.
     * @return kompilacne info pre zdrojovy kode v dokumente doc od offset o dlzke length.
     */
    //SsceIntent:Syntakticka analyza java dokumentu;
    public CompilationInfo getCompilationInfo(BaseDocument doc, int offset, int length) {
//        
//        JavaSource.forDocument(doc).getClasspathInfo()
//                

        final Lookup lookup = MimeLookup.getLookup("text/x-java");
        final ParserFactory parserFactory = lookup.lookup(ParserFactory.class);
        if (parserFactory == null) {
            throw new IllegalArgumentException("No parser for mime type: text/x-java");
        }
        Snapshot snapshot = Source.create(doc).createSnapshot().create(offset, length, "text/x-java").getSnapshot();

        Parser p = parserFactory.createParser(Collections.singletonList(snapshot));
//        try {
//            return JavaSourceAccessor.getINSTANCE().createCompilationController(Source.create(doc));
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
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (ParseException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        return null;
    }

    /**
     * Metoda skonstruuje model importov pre dokument doc.
     * @param doc dokument so zdrojovym kodom.
     * @param offset zaciatocny offset useku importov v dokumente doc.
     * @param length dlzka useku importov v dokumente doc.
     * @return model importov pre dokument doc.
     */
    public Imports getImports(BaseDocument doc, int offset, int length) {
        Imports imports = new Imports();
        CompilationInfo info = getCompilationInfo(doc, offset, length);
        if (info == null) {
            return null;
        }
        if (info.getCompilationUnit().getImports() != null) {
            for (ImportTree imp : info.getCompilationUnit().getImports()) {
                imports.addImport(imp.getQualifiedIdentifier().toString(), imp.isStatic());
            }
        }
        return imports;
    }

   /**
    * Metoda vytvori mapovanie zamerov pre cely zdrojovy kod.
    * @param javaFilePaths cesty java suborov so zdrojovym kodom.
    * @return mapovanie zamerov na fragmenty kodu pre cely zdrojovy kod.
    */
    //SsceIntent:Model pre mapovanie zamerov;
    public List<JavaFileConcerns> createJavaFilesIntents(Set<String> javaFilePaths) {

        List<JavaFileConcerns> javaFiles = new ArrayList<JavaFileConcerns>();
        JavaFileConcerns jf;
        for (String pathFile : javaFilePaths) {
            jf = createJavaFileIntents(new File(pathFile));
            if (jf.getCodes() != null) {
                javaFiles.add(jf);
            }
        }
//        Collections.sort(javaFiles, JavaFile.SORT_BY_PACKAGES);

        return javaFiles;
    }

    /**
     * Metoda zrealizuje proejkcie kodu na zaklade konfiguracie zamerov (dopyt na kod) a vytvori model prepojenia vsetkych java suborov s pomocnym suborom .sj.
     * @param javaFilePaths cesty java suborov so zdrojovym kodom.
     * @param configuration konfiguracia zamerov (dopyt na kod).
     * @return model model prepojenia vsetkych java suborov s pomocnym suborom .sj.
     */
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;Realizovanie projekcie zdrojoveho kodu;Model pre synchronizaciu kodu;
    public List<JavaFile> createJavaFiles(Set<String> javaFilePaths, CurrentProjection configuration) {

        List<JavaFile> javaFiles = new ArrayList<JavaFile>();
        JavaFile jf;
        for (String pathFile : javaFilePaths) {
            jf = createJavaFile(new File(pathFile), configuration);
            if (jf.getCodeSnippets() != null && !jf.getCodeSnippets().isEmpty()) {
                javaFiles.add(jf);
            }
        }
        Collections.sort(javaFiles, JavaFile.SORT_FILES_BY_PACKAGES);

        return javaFiles;
    }
}
