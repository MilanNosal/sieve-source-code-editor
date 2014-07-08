package sk.tuke.kpi.ssce.core.model.projections;

import sk.tuke.kpi.ssce.concerns.annotations.CompilerTreeUtils;
import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.openide.util.Exceptions;
import sk.tuke.kpi.ssce.core.model.projections.CodeIntents;
import sk.tuke.kpi.ssce.core.model.projections.JavaFileIntents;
import sk.tuke.kpi.ssce.concerns.annotations.AnnotationSearchableFactory;
import sk.tuke.kpi.ssce.concerns.interfaces.SearchableFactory;
import sk.tuke.kpi.ssce.core.utilities.IntentsUtilities;

/**
 * Trieda reprezentuje skener prechadzajuci celou strukturou kompilacneho stromu
 * zdrojoveho kodu java. Sluzi ako nastroj pre ziskanie mapovania zamerov na
 * fragmenty kodu pre jeden java subor.
 *
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Model pre mapovanie zamerov;Praca s java suborom;
public class JavaFileIntentsVisitor extends TreePathScanner<JavaFileIntents, JavaFileIntents> {

    private final IntentsUtilities intentsUtilities = new IntentsUtilities();
    private final CompilationInfo info;
    private final CompilationUnitTree cu;
    private final SourcePositions sp;
    private final BaseDocument doc;
    
    private final SearchableFactory factory;
    //SsceIntent:Komentar uchovavajuci zamer;
    //private static Pattern pattern = Pattern.compile(Constants.SSCE_COMMENT_REGEX);

    /**
     * Vytvori novy skener pre kompilacne info a strom.
     *
     * @param info kompilacne info.
     * @param cu kompilacna jednotka obsahujuca kompilacny strom...
     * @param sp sourcePositions.
     * @param doc dokument, z ktoreho sa vytvori mapovania zamerov na fragmenty
     * kodu pre jeden java subor (dokument).
     */
    public JavaFileIntentsVisitor(CompilationInfo info, CompilationUnitTree cu, SourcePositions sp, BaseDocument doc) {
        this.info = info;
        this.cu = cu;
        this.sp = sp;
        this.doc = doc;
        this.factory = new AnnotationSearchableFactory(new CompilerTreeUtils((CompilationController) info));
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
    @Override
    public JavaFileIntents visitClass(ClassTree node, JavaFileIntents p) {
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
            CodeIntents code = new CodeIntents(p, builder.toString(),
                    doc.createPosition(start), end - start,
                    null, -1,
                    factory.getSearchablesFor(node));

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
    @Override
    public JavaFileIntents visitMethod(MethodTree node, JavaFileIntents p) {
        super.visitMethod(node, p);

        int start = (int) sp.getStartPosition(cu, node);
        int end = (int) sp.getEndPosition(cu, node);

//        String typeClass = null;// class, enum, interface, anotation

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
            CodeIntents code = new CodeIntents(p,
                    builder.toString(),
                    doc.createPosition(start), end - start,
                    null, -1,
                    factory.getSearchablesFor(node));

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
    @Override
    public JavaFileIntents visitVariable(VariableTree node, JavaFileIntents p) {
        super.visitVariable(node, p);


        int start = (int) sp.getStartPosition(cu, node);
        int end = (int) sp.getEndPosition(cu, node);

//        String typeClass = null;// class, enum, interface, anotation

        StringBuilder builder = new StringBuilder();
        for (Modifier modifier : node.getModifiers().getFlags()) {
            builder.append(modifier.toString()).append(" ");
        }
        builder.append(node.getType()).append(" ").append(node.getName());

        try {
            CodeIntents code = new CodeIntents(p,
                    builder.toString(),
                    doc.createPosition(start), end - start,
                    null, -1,
                    factory.getSearchablesFor(node));

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
    @Override
    public JavaFileIntents visitCompilationUnit(CompilationUnitTree node, JavaFileIntents p) {
        super.visitCompilationUnit(node, p);
        if (node.getPackageName() != null) {
            p.setPackageName(node.getPackageName().toString());
        }
        return p;
    }
}