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
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import static sk.tuke.kpi.ssce.core.CompilationUtilities.getCompilationInfo;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;
import sk.tuke.kpi.ssce.core.model.view.CodeSnippet;
import sk.tuke.kpi.ssce.core.model.view.importshandling.Imports;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.core.model.view.importshandling.Import;
import sk.tuke.kpi.ssce.sieving.interfaces.CodeSiever;
import sk.tuke.kpi.ssce.sieving.interfaces.PostProcessingSiever;

/**
 * Trieda reprezentuje nastroj pre pracu s java subormi.
 *
 * @author Matej Nosal
 */
//SsceIntent:Praca s java suborom;
@CodeAnalysis(output = RepresentationOf.VIEW)
public class ViewModelCreator<T extends Concern> {

    private final ConcernExtractor<T> extractor;
    private final CodeSiever<T> siever;
    private final List<PostProcessingSiever> postProcessors;

    public ViewModelCreator(ConcernExtractor<T> extractor, CodeSiever<T> siever,
            List<PostProcessingSiever> postProcessors) {
        this.extractor = extractor;
        this.siever = siever;
        this.postProcessors = postProcessors;
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
    public List<JavaFile<T>> createJavaFiles(Set<String> javaFilePaths, CurrentProjection<T> configuration) {
        List<JavaFile<T>> javaFiles = new ArrayList<JavaFile<T>>();
        JavaFile jf;
        for (String pathFile : javaFilePaths) {
            jf = createJavaFile(new File(pathFile), configuration);
            if (jf != null && jf.getCodeSnippets() != null && !jf.getCodeSnippets().isEmpty()) {
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
    public JavaFile<T> createJavaFile(File javaFile, CurrentProjection<T> configuration) {
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
    private JavaFile<T> extractViewFromDocument(EditorCookie ec, CurrentProjection<T> configuration) {
        BaseDocument doc;
        try {
            doc = (BaseDocument) ec.openDocument();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }

        JavaFile<T> javaFile;

        doc.readLock();
        try {
            //SsceIntent:Syntakticka analyza java dokumentu;
            CompilationInfo info = getCompilationInfo(doc);
            if (info == null) {
                return null;
            }

            CompilationUnitTree cu = info.getCompilationUnit();
            JavaFileVisitor<T> scanner = new JavaFileVisitor<T>(info, extractor, siever, configuration, doc);
            javaFile = scanner.scan(cu, new JavaFile<T>(FileUtil.toFile(info.getFileObject()).getPath(), info.getFileObject().getName(), ec, doc));

            Iterator<PostProcessingSiever> iterator = this.postProcessors.iterator();
            
            while (iterator.hasNext() && javaFile != null) {
                javaFile = iterator.next().process(javaFile, doc);
            }
            
            if(javaFile == null) {
                return null;
            }
            
            
            // start getting necessary and editable imports
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

    @ImportsManagement
    private boolean isInCodeArea(JavaFile<T> jf, int offset) {
        for (CodeSnippet<T> code : jf.getCodeSnippets()) {
            if (code.getCodeBinding().getStartPositionJavaDocument() <= offset && offset < code.getCodeBinding().getEndPositionJavaDocument()) {
                return true;
            }
        }
        return false;
    }

    @ImportsManagement
    private boolean isInImportsArea(JavaFile<T> jf, int offset) {
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
