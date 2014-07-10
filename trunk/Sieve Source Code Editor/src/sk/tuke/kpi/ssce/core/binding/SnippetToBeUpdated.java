package sk.tuke.kpi.ssce.core.binding;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.openide.util.Exceptions;
import sk.tuke.kpi.ssce.annotations.concerns.Synchronization;
import sk.tuke.kpi.ssce.annotations.concerns.enums.Direction;
import sk.tuke.kpi.ssce.core.model.view.CodeSnippet;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;

/**
 * Trieda zapuzdrujuca prepojenie zdrojakov s pohladom.
 * @author Matej Nosal, Milan Nosal
 */
@Synchronization(direction = {Direction.JAVATOSJ, Direction.SJTOJAVA})
class SnippetToBeUpdated {

    private int start = -1;
    private int end = -1;
    private JavaFile javaFile = null;
    private JavaFile imports = null;
    private CodeSnippet code = null;

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public JavaFile getJavaFile() {
        return javaFile;
    }

    public void setJavaFile(JavaFile javaFile) {
        this.javaFile = javaFile;
    }

    public JavaFile getImports() {
        return imports;
    }

    public void setImports(JavaFile imports) {
        this.imports = imports;
    }

    public CodeSnippet getCode() {
        return code;
    }

    public void setCode(CodeSnippet code) {
        this.code = code;
    }
    

    /**
     * Premosti prepojene useky kodu v jave a v sj.
     * @param doc
     * @return
     */
    public boolean updatePositions(BaseDocument doc) {
        if (start != -1 && end != -1) {
            try {
                if (javaFile != null) {
                    javaFile.setBeginInSJ(doc, start);
                    javaFile.setEndInSJ(doc, end);
                } else if (imports != null) {
                    imports.getImportsBinding().setStartPositionSieveDocument(doc, start);
                    imports.getImportsBinding().setEndPositionSieveDocument(doc, end);
                } else if (code != null) {
                    code.getCodeBinding().setStartPositionSieveDocument(doc, start);
                    code.getCodeBinding().setEndPositionSieveDocument(doc, end);
                } else {
                    return false;
                }
                return true;
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }
}
