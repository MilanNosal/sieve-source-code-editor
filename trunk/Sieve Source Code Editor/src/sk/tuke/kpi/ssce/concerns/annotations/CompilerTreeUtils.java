package sk.tuke.kpi.ssce.concerns.annotations;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.openide.util.Exceptions;

/**
 * Trieda s pomocnymi metodami pre pracu s kompilacnymi jednotkami a AST
 *
 * @author Milan Nosal
 */
public class CompilerTreeUtils {

    private CompilationController cc;

    public CompilerTreeUtils(CompilationController cc) { //, CompilationUnitTree cu, SourcePositions sp) {
        try {
            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            this.cc = cc;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            this.cc = null;
        }
    }

    private Element getElementFor(Tree node) {
        try {
            //if(cc.getPhase() != JavaSource.Phase.ELEMENTS_RESOLVED) {
            cc.toPhase(JavaSource.Phase.UP_TO_DATE);
            //}
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            this.cc = null;
        }
        return cc.getTrees().getElement(TreePath.getPath(cc.getCompilationUnit(), node)); // tu bolo cu
    }

    private boolean isAnnotatedBy(Element element, DeclaredType type) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (mirror.getAnnotationType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Poomcna metoda pre vytiahnutie anotacnych typov, ktorych anotacie anotuju
     * uvedeny uzol.
     *
     * @param node
     * @return
     */
    public Set<DeclaredType> getAnnTypesFor(Tree node) {
        try {
            Element element = this.getElementFor(node);
            Set<DeclaredType> annTypes = new HashSet<DeclaredType>();
            for (AnnotationMirror am : element.getAnnotationMirrors()) { //TODO: nullpointer pri pridani deprecated, variable
                annTypes.add(am.getAnnotationType());
            }

            return annTypes;
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
//            System.err.println(">>>> " + node.toString());
//            System.err.println(">>>>>>>> " + this.getElementFor(node));
            return new HashSet<DeclaredType>();
        }
    }

}
