package sk.tuke.kpi.ssce.core.model.possibleprojections;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.openide.util.Exceptions;
import sk.tuke.kpi.ssce.annotations.concerns.CodeAnalysis;
import sk.tuke.kpi.ssce.annotations.concerns.View;
import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;
import sk.tuke.kpi.ssce.annotations.concerns.enums.ViewAspect;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;

/**
 * Trieda reprezentuje skener prechadzajuci celou strukturou kompilacneho stromu
 * zdrojoveho kodu java. Sluzi ako nastroj pre ziskanie mapovania zamerov na
 * fragmenty kodu pre jeden java subor.
 *
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Model pre mapovanie zamerov;Praca s java suborom;
@View(aspect = ViewAspect.CONCERN_EXTRACTION)
@CodeAnalysis(output = RepresentationOf.PROJECTION)
public class JavaFileConcernsVisitor extends TreePathScanner<JavaFileConcerns, JavaFileConcerns> {

    private final CompilationInfo info;
    private final CompilationUnitTree cu;
    private final SourcePositions sp;
    private final BaseDocument doc;    
    
    @View(aspect = ViewAspect.CONCERN_EXTRACTION)
    private final ConcernExtractor extractor;

    /**
     * Vytvori novy skener pre kompilacne info a strom.
     *
     * @param info kompilacne info.
     * @param cu kompilacna jednotka obsahujuca kompilacny strom...
     * @param sp sourcePositions.
     * @param doc dokument, z ktoreho sa vytvori mapovania zamerov na fragmenty
     * kodu pre jeden java subor (dokument).
     */
    public JavaFileConcernsVisitor(ConcernExtractor extractor,
            CompilationInfo info, CompilationUnitTree cu,
            SourcePositions sp, BaseDocument doc) {
        this.info = info;
        this.cu = cu;
        this.sp = sp;
        this.doc = doc;
        this.extractor = extractor;
    }

    /**
     * Metoda prida mapovanie zamerov pre fragment kodu reprezentujuci
     * deklaraciu triedy (typu) do mapovania zamerov jedneho java suboru.
     *
     * @param node uzol v kompilacnom strome reprezentujuci deklaraciu triedy.
     * @param p mapovanie zamerov na fragmenty kodu pre jeden java subor.
     * @return aktualne mapovanie zamerov na fragmenty kodu pre jeden java
     * subor.
     */
    @CodeAnalysis(output = RepresentationOf.PROJECTION)
    @View(aspect = ViewAspect.CONCERN_EXTRACTION)
    @Override
    public JavaFileConcerns visitClass(ClassTree node, JavaFileConcerns p) {
        super.visitClass(node, p);

        int start = (int) sp.getStartPosition(cu, node);
        int end = (int) sp.getEndPosition(cu, node);

        String typeClass = null;// class, enum, interface, anotation

        TokenHierarchy th = info.getTokenHierarchy();
        if (th != null) { // extrahuje typ
            TokenSequence seq = th.tokenSequence();
            seq.move(start);
            while (seq.moveNext() && !JavaTokenId.LBRACE.equals(seq.token().id())) {
                if (JavaTokenId.CLASS.equals(seq.token().id()) || JavaTokenId.ENUM.equals(seq.token().id())) {
                    typeClass = seq.token().text().toString();
                    break;

                } else if (JavaTokenId.INTERFACE.equals(seq.token().id())) {
                    typeClass = seq.token().text().toString();
                    while (seq.movePrevious() && ("comment".equals(seq.token().id().primaryCategory())
                            || "whitespace".equals(seq.token().id().primaryCategory()))) {
                    }
                    if (JavaTokenId.AT.equals(seq.token().id())) {
                        typeClass = seq.token().text().toString() + typeClass;
                    }
                    break;
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        for (Modifier modifier : node.getModifiers().getFlags()) {
            builder.append(modifier.toString()).append(" ");
        }
        builder.append(typeClass).append(" ").append(node.getSimpleName());

        try {
            CodeSnippetConcerns code = new CodeSnippetConcerns(p, builder.toString(),
                    doc.createPosition(start), end - start,
                    extractor.getConcernsFor(node));

            p.getCodes().add(code);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return p;
    }

    /**
     * Metoda prida mapovanie zamerov pre fragment kodu reprezentujuci
     * deklaraciu metody do mapovania zamerov jedneho java suboru.
     *
     * @param node uzol v kompilacnom strome reprezentujuci deklaraciu metody.
     * @param p mapovanie zamerov na fragmenty kodu pre jeden java subor.
     * @return aktualne mapovanie zamerov na fragmenty kodu pre jeden java
     * subor.
     */
    @CodeAnalysis(output = RepresentationOf.PROJECTION)
    @View(aspect = ViewAspect.CONCERN_EXTRACTION)
    @Override
    public JavaFileConcerns visitMethod(MethodTree node, JavaFileConcerns p) {
        super.visitMethod(node, p);

        int start = (int) sp.getStartPosition(cu, node);
        int end = (int) sp.getEndPosition(cu, node);

        StringBuilder builder = new StringBuilder();
        for (Modifier modifier : node.getModifiers().getFlags()) {
            builder.append(modifier.toString()).append(" ");
        }
        builder.append(node.getReturnType()).append(" ").append(node.getName()).append("(");
        List<? extends VariableTree> variableTrees = node.getParameters();
        for (int i = 0; i < variableTrees.size(); i++) {
            builder.append(variableTrees.get(i).getType());
            if (i < variableTrees.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");

        try {
            CodeSnippetConcerns code = new CodeSnippetConcerns(p,
                    builder.toString(),
                    doc.createPosition(start), end - start,
                    extractor.getConcernsFor(node));

            p.getCodes().add(code);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return p;
    }

    /**
     * Metoda prida mapovanie zamerov pre fragment kodu reprezentujuci
     * deklaraciu premennej do mapovania zamerov jedneho java suboru.
     *
     * @param node uzol v kompilacnom strome reprezentujuci deklaraciu
     * premennej.
     * @param p mapovanie zamerov na fragmenty kodu pre jeden java subor.
     * @return aktualne mapovanie zamerov na fragmenty kodu pre jeden java
     * subor.
     */
    @CodeAnalysis(output = RepresentationOf.PROJECTION)
    @View(aspect = ViewAspect.CONCERN_EXTRACTION)
    @Override
    public JavaFileConcerns visitVariable(VariableTree node, JavaFileConcerns p) {
        super.visitVariable(node, p);

        int start = (int) sp.getStartPosition(cu, node);
        int end = (int) sp.getEndPosition(cu, node);

        StringBuilder builder = new StringBuilder();
        for (Modifier modifier : node.getModifiers().getFlags()) {
            builder.append(modifier.toString()).append(" ");
        }
        builder.append(node.getType()).append(" ").append(node.getName());

        try {
            CodeSnippetConcerns code = new CodeSnippetConcerns(p,
                    builder.toString(),
                    doc.createPosition(start), end - start,
                    extractor.getConcernsFor(node));

            p.getCodes().add(code);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return p;
    }

    /**
     * Metoda nastavi mapovaniu zamerov jedneho java suboru nazov balika.
     *
     * @param node uzol v kompilacnom strome.
     * @param p mapovanie zamerov na fragmenty kodu pre jeden java subor.
     * @return aktualne mapovanie zamerov na fragmenty kodu pre jeden java
     * subor.
     */
    @CodeAnalysis(output = RepresentationOf.PROJECTION)
    @Override
    public JavaFileConcerns visitCompilationUnit(CompilationUnitTree node, JavaFileConcerns p) {
        super.visitCompilationUnit(node, p);
        if (node.getPackageName() != null) {
            p.setPackageName(node.getPackageName().toString());
        }
        return p;
    }
}