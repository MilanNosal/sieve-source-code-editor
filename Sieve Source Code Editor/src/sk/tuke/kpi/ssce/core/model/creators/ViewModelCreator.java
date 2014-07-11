package sk.tuke.kpi.ssce.core.model.creators;

import sk.tuke.kpi.ssce.core.CompilationUtilities;
import sk.tuke.kpi.ssce.core.model.view.JavaFileVisitor;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import sk.tuke.kpi.ssce.annotations.concerns.CodeAnalysis;
import sk.tuke.kpi.ssce.annotations.concerns.ImportsManagement;
import sk.tuke.kpi.ssce.annotations.concerns.SourceCodeSieving;
import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import static sk.tuke.kpi.ssce.core.CompilationUtilities.getCompilationInfo;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;
import sk.tuke.kpi.ssce.core.model.view.CodeSnippet;
import sk.tuke.kpi.ssce.core.model.view.importshandling.Imports;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.core.model.view.importshandling.Import;
import sk.tuke.kpi.ssce.sieving.interfaces.CodeSiever;

/**
 * Trieda reprezentuje nastroj pre pracu s java subormi.
 *
 * @author Matej Nosal
 */
//SsceIntent:Praca s java suborom;
@CodeAnalysis(output = RepresentationOf.VIEW)
public class ViewModelCreator {

    private final ConcernExtractor extractor;
    private final CodeSiever siever;

    public ViewModelCreator(ConcernExtractor extractor, CodeSiever siever) {
        this.extractor = extractor;
        this.siever = siever;
    }

    /**
     * Metoda zrealizuje proejkcie kodu na zaklade konfiguracie zamerov (dopyt
     * na kod) a vytvori model prepojenia vsetkych java suborov s pomocnym
     * suborom .sj.
     *
     * @param javaFilePaths cesty java suborov so zdrojovym kodom.
     * @param configuration konfiguracia zamerov (dopyt na kod).
     * @return model model prepojenia vsetkych java suborov s pomocnym suborom
     * .sj.
     */
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;Realizovanie projekcie zdrojoveho kodu;Model pre synchronizaciu kodu;
    @CodeAnalysis(output = RepresentationOf.VIEW)
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

    /**
     * Metoda zrealizuje zaujmovo-orientovanu projekciu zdrojoveho kodu a
     * vytvori model pre prepojenie java subora s pomocnym suborom .sj.
     *
     * @param javaFile java subor nad ktorym sa ma zdrealizovat projekcia kodu.
     * @param configuration konfiguracia zamerov (dopyt na kod).
     * @return zatial nekonzistentny model pre prepojenie java subora s pomocnym
     * suborom .sj.
     */
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;Realizovanie projekcie zdrojoveho kodu;Model pre synchronizaciu kodu;
    @CodeAnalysis(output = RepresentationOf.VIEW)
    public JavaFile createJavaFile(File javaFile, CurrentProjection configuration) {
//        System.out.println("\n------------------------------- " + javaFile.getName());

        FileObject fobj = FileUtil.toFileObject(javaFile);
        DataObject dobj;
        try {
            dobj = DataObject.find(fobj);
            if (dobj != null) {
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                return extractViewFromDocument(ec, configuration);
            }
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // pre ziskanie fragmentov kodu pre danu konfiguraciu
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;Realizovanie projekcie zdrojoveho kodu;Model pre synchronizaciu kodu;
    @CodeAnalysis(output = RepresentationOf.VIEW)
    @SourceCodeSieving
    @ImportsManagement
    private JavaFile extractViewFromDocument(EditorCookie ec, CurrentProjection configuration) {
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
            JavaFileVisitor scanner = new JavaFileVisitor(info, extractor, siever, configuration, doc);
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
                Import import1 = javaFile.getAllImports().findImport(necessaryImport);
                if (editableImportedTypes.contains(necessaryImport)) {
                    import1.setEditable(true);
                }
                javaFile.getNecessaryImports().addImport(import1);
            }
        } finally {
            doc.readUnlock();
        }
        return javaFile;
    }

    //SsceIntent:Realizovanie projekcie zdrojoveho kodu;
    @SourceCodeSieving(postProcessing = true)
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
    @SourceCodeSieving(postProcessing = true)
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

    @ImportsManagement
    private boolean isInCodeArea(JavaFile jf, int offset) {
        for (CodeSnippet code : jf.getCodeSnippets()) {
            if (code.getCodeBinding().getStartPositionJavaDocument() <= offset && offset < code.getCodeBinding().getEndPositionJavaDocument()) {
                return true;
            }
        }
        return false;
    }

    @ImportsManagement
    private boolean isInImportsArea(JavaFile jf, int offset) {
        return jf.getImportsBinding().getStartPositionJavaDocument() <= offset && offset < jf.getImportsBinding().getEndPositionJavaDocument();
    }

    /**
     * Metoda skonstruuje model importov pre dokument doc.
     *
     * @param doc dokument so zdrojovym kodom.
     * @param offset zaciatocny offset useku importov v dokumente doc.
     * @param length dlzka useku importov v dokumente doc.
     * @return model importov pre dokument doc.
     */
    @ImportsManagement
    public Imports getImports(BaseDocument doc, int offset, int length) {
        Imports imports = new Imports();
        CompilationInfo info = CompilationUtilities.getCompilationInfo(doc, offset, length);
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
}
