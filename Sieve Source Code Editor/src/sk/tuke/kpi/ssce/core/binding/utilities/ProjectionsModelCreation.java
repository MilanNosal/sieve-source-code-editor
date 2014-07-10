package sk.tuke.kpi.ssce.core.binding.utilities;

import com.sun.source.tree.CompilationUnitTree;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.editor.BaseDocument;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import sk.tuke.kpi.ssce.annotations.concerns.CodeAnalysis;
import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import static sk.tuke.kpi.ssce.core.binding.utilities.CompilationUtilities.getCompilationInfo;
import sk.tuke.kpi.ssce.core.model.possibleprojections.JavaFileConcerns;
import sk.tuke.kpi.ssce.core.model.possibleprojections.JavaFileConcernsVisitor;

/**
 *
 * @author Milan Nosal, Matej Nosal
 */
@CodeAnalysis(output = RepresentationOf.PROJECTION)
public class ProjectionsModelCreation {
    private final ConcernExtractor extractor;

    public ProjectionsModelCreation(ConcernExtractor extractor) {
        this.extractor = extractor;
    }
    
    /**
     * Metoda vytvori mapovanie zamerov pre cely zdrojovy kod.
     *
     * @param javaFilePaths cesty java suborov so zdrojovym kodom.
     * @return mapovanie zamerov na fragmenty kodu pre cely zdrojovy kod.
     */
    //SsceIntent:Model pre mapovanie zamerov;
    @CodeAnalysis(output = RepresentationOf.PROJECTION)
    public List<JavaFileConcerns> createJavaFilesConcerns(Set<String> javaFilePaths) {
        List<JavaFileConcerns> javaFiles = new ArrayList<JavaFileConcerns>();
        JavaFileConcerns jf;
        for (String pathFile : javaFilePaths) {
            jf = createJavaFileConcerns(new File(pathFile));
            if (jf.getCodes() != null) {
                javaFiles.add(jf);
            }
        }
        return javaFiles;
    }
    
    /**
     * Metoda vytvori mapovanie zamerov na fragmenty kodu pre jeden java subor.
     *
     * @param javaFile java subor pre ktory sa ma vytvorit mapovanie zamerov.
     * @return mapovanie zamerov na fragmenty kodu pre jeden java subor.
     */
    //SsceIntent:Model pre mapovanie zamerov;
    @CodeAnalysis(output = RepresentationOf.PROJECTION)
    public JavaFileConcerns createJavaFileConcerns(File javaFile) {
        FileObject fobj = FileUtil.toFileObject(javaFile);
        DataObject dobj;
        try {
            dobj = DataObject.find(fobj);
            if (dobj != null) {
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                return extractCodeConcernsFromDocument(ec);
            }
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    //iba pre ziskanie vsetkych zamerov pre dokument
    //SsceIntent:Model pre mapovanie zamerov;
    @CodeAnalysis(output = RepresentationOf.PROJECTION)
    private JavaFileConcerns extractCodeConcernsFromDocument(EditorCookie ec) {
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
            JavaFileConcernsVisitor scanner = new JavaFileConcernsVisitor(extractor, info, doc);
            javaFile = scanner.scan(cu, new JavaFileConcerns(
                    FileUtil.toFile(info.getFileObject()).getPath(),
                    info.getFileObject().getName()
            ));
            javaFile.sortCodes();

        } finally {
            doc.readUnlock();
        }
        return javaFile;
    }
}
