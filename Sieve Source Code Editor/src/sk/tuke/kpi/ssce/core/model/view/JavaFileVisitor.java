package sk.tuke.kpi.ssce.core.model.view;

import sk.tuke.kpi.ssce.concerns.annotations.CompilerTreeUtils;
import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import sk.tuke.kpi.ssce.core.configuration.CurrentProjection;
import sk.tuke.kpi.ssce.core.model.view.BindingPositions;
import sk.tuke.kpi.ssce.core.model.view.Code;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.concerns.annotations.AnnotationSearchableFactory;
import sk.tuke.kpi.ssce.concerns.interfaces.Searchable;
import sk.tuke.kpi.ssce.concerns.interfaces.SearchableFactory;
import sk.tuke.kpi.ssce.core.utilities.IntentsUtilities;

/**
 * Trieda reprezentuje skener prechadzajuci celou strukturou kompilacneho stromu
 * zdrojoveho kodu java. Sluzi ako nastroj pre zaujmoco-orientovanu projekciu
 * zdrojoveho kodu v jedenom java subore.
 *
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Praca s java suborom;Model pre synchronizaciu kodu;
public class JavaFileVisitor extends TreePathScanner<JavaFile, JavaFile> {

    private final IntentsUtilities intentsUtilities = new IntentsUtilities();
    private final CompilationInfo info;
    private final CompilationUnitTree cu;
    private final SearchableFactory factory;
    private final SourcePositions sp;
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;
    private final CurrentProjection config;
    private final BaseDocument doc;
    private final Stack<String> stack;
    private final Stack<Set<Searchable>> stackOfIntents;
    //SsceIntent:Komentar uchovavajuci zamer;
    //private static Pattern pattern = Pattern.compile(Constants.SSCE_COMMENT_REGEX);

    /**
     * Vytvori novy skener pre kompilacne info a strom, dokument, konfiguraciu
     * ...
     *
     * @param info kompilacne info.
     * @param cu kompilacna jednotka obsahujuca kompilacny strom...
     * @param sp sourcePositions.
     * @param config konfiguracia zamerov (dopyt) na zdrojovy kod.
     * @param doc dokument, nad ktorym sa bude realizovat projekcie zdrojoveho
     * kodu na zaklade konfiguracie (dopytu) zamerov.
     */
    public JavaFileVisitor(CompilationInfo info, CompilationUnitTree cu, SourcePositions sp, CurrentProjection config, BaseDocument doc) {
        this.info = info;
        this.cu = cu;
        this.factory = new AnnotationSearchableFactory(new CompilerTreeUtils((CompilationController) info));
        this.sp = sp;
        this.config = config;
        this.doc = doc;
        this.stack = new Stack<String>();
        this.stackOfIntents = new Stack<Set<Searchable>>();
    }

    /**
     * Metoda prida fragment kodu reprezentujuci deklaraciu typu do vysledku
     * projekcie zdrojoveho kodu na zaklade konfiguracie zamerov.
     *
     * @param node uzol v kompilacnom strome reprezentujuci deklaraciu triedy.
     * @param p vysledok projekcie zdrojoveho kodu pre jeden java subor.
     * @return aktualny vysledok projekcie zdrojoveho kodu pre jeden java subor.
     */
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;Komentar uchovavajuci zamer;
    @Override ()
    public JavaFile visitClass(ClassTree node, JavaFile p) {
        String nameElement = node.getSimpleName().toString();

        int start = (int) sp.getStartPosition(cu, node);
        int end = (int) sp.getEndPosition(cu, node);
        
        stackOfIntents.push(factory.getSearchablesFor(node));
        stack.push(nameElement);
        super.visitClass(node, p);
        stack.pop();

        if (filterCodeByIntents(getIntentsForCode(), config)) {
            try {
                Code code = new Code(NbDocument.findLineColumn((StyledDocument) doc, (int) start), getContextForCode(), nameElement, "TYPE");
                code.setCodeBinding(new BindingPositions(doc.createPosition(start), end - start));
                p.getCodes().add(code);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        stackOfIntents.pop();
        return p;
    }

    /**
     * Metoda prida fragment kodu reprezentujuci deklaraciu metody do vysledku
     * projekcie zdrojoveho kodu na zaklade konfiguracie zamerov.
     *
     * @param node uzol v kompilacnom strome reprezentujuci deklaraciu metody.
     * @param p vysledok projekcie zdrojoveho kodu pre jeden java subor.
     * @return aktualny vysledok projekcie zdrojoveho kodu pre jeden java subor.
     */
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;Komentar uchovavajuci zamer;
    @Override
    public JavaFile visitMethod(MethodTree node, JavaFile p) {

        StringBuilder builder = new StringBuilder();
        builder.append(node.getName()).append("(");
        List<? extends VariableTree> variableTrees = node.getParameters();

        for (int i = 0; i < variableTrees.size(); i++) {
            builder.append(variableTrees.get(i).getType());
            if (i < variableTrees.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");
        String nameElement = builder.toString();

        int start = (int) sp.getStartPosition(cu, node);
        int end = (int) sp.getEndPosition(cu, node);

        stackOfIntents.push(factory.getSearchablesFor(node));
        stack.push(nameElement);
        super.visitMethod(node, p);
        stack.pop();


        if (filterCodeByIntents(getIntentsForCode(), config)) {
            try {
                Code code = new Code(NbDocument.findLineColumn((StyledDocument) doc, (int) start), getContextForCode(), nameElement, "METHOD");
                code.setCodeBinding(new BindingPositions(doc.createPosition(start), end - start));
                p.getCodes().add(code);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        stackOfIntents.pop();
        return p;
    }

    /**
     * Metoda prida fragment kodu reprezentujuci deklaraciu premennej do
     * vysledku projekcie zdrojoveho kodu na zaklade konfiguracie zamerov.
     *
     * @param node uzol v kompilacnom strome reprezentujuci deklaraciu
     * premennej.
     * @param p vysledok projekcie zdrojoveho kodu pre jeden java subor.
     * @return aktualny vysledok projekcie zdrojoveho kodu pre jeden java subor.
     */
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;Komentar uchovavajuci zamer;
    @Override
    public JavaFile visitVariable(VariableTree node, JavaFile p) {
        String nameElement = node.getName().toString();

        int start = (int) sp.getStartPosition(cu, node);
        int end = (int) sp.getEndPosition(cu, node);

        stackOfIntents.push(factory.getSearchablesFor(node));
        stack.push(nameElement);
        super.visitVariable(node, p);
        stack.pop();

        if (filterCodeByIntents(getIntentsForCode(), config)) {
            try {
                Code code = new Code(NbDocument.findLineColumn((StyledDocument) doc, (int) start), getContextForCode(), nameElement, "FIELD");
                code.setCodeBinding(new BindingPositions(doc.createPosition(start), end - start));
                p.getCodes().add(code);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        stackOfIntents.pop();
        return p;
    }

    /**
     * Metoda prida sekciu importov do vysledku projekcie zdrojoveho kodu a
     * nastavi balik v ktorom sa java subor nachadza.
     *
     * @param node uzol v kompilacnom strome.
     * @param p vysledok projekcie zdrojoveho kodu pre jeden java subor.
     * @return aktualny vysledok projekcie zdrojoveho kodu pre jeden java subor.
     */
    @Override
    public JavaFile visitCompilationUnit(CompilationUnitTree node, JavaFile p) {
        super.visitCompilationUnit(node, p);
        if (node.getPackageName() != null) {
            p.setPackageName(node.getPackageName().toString());
        }


        int importsStart = Integer.MAX_VALUE;
        int importsEnd = -1;

        for (ImportTree imp : node.getImports()) {
            p.getAllImports().addImport(imp.getQualifiedIdentifier().toString(), imp.isStatic());

            int start = (int) sp.getStartPosition(cu, imp);
            int end = (int) sp.getEndPosition(cu, imp);

            if (importsStart > start) {
                importsStart = start;
            }

            if (end > importsEnd) {
                importsEnd = end;
            }
        }
        if (importsEnd == (-1) || importsStart == (-1)) {
            if (node.getPackageName() != null) {

                TokenHierarchy th = info.getTokenHierarchy();
                if (th != null) {
                    TokenSequence seq = th.tokenSequence();
                    seq.move((int) sp.getEndPosition(cu, node.getPackageName()));
                    seq.moveNext();
//                    System.out.println("pred cyklom " + seq.token().text().toString());
                    while (!JavaTokenId.SEMICOLON.equals(seq.token().id()) && seq.moveNext()) {
//                        System.out.println("v cykle=   " + seq.token().text().toString());
//                        finding semicolon after package
                    }
                    importsEnd = importsStart = seq.offset() + seq.token().length(); // compute offset for imports after semicolon or on end of doc
                    p.getAllImports().setInsertNewLineBeforeAfterImports(true);
                }

            } else {
                importsEnd = importsStart = doc.getStartPosition().getOffset();
                p.getAllImports().setInsertNewLineBeforeAfterImports(true);
            }
        }


        if (importsEnd != (-1) && importsStart != (-1)) {
            try {
                p.setImportsBinding(new BindingPositions(doc.createPosition(importsStart), importsEnd - importsStart));
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return p;
    }

    /**
     * TODO: zeby sa intents dedili?
     * @return 
     */
    private Set<Searchable> getIntentsForCode() {
        Set<Searchable> intents = new HashSet<Searchable>();
        for (int i = 0; i < stackOfIntents.size(); i++) {
            intents.addAll(stackOfIntents.get(i));
        }
        return intents;
    }

    private String getContextForCode() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < stack.size(); i++) {
            builder.append(stack.get(i));
            if (i < stack.size() - 1) {
                builder.append(".");
            }
        }
        return builder.toString();
    }

    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;Realizovanie projekcie zdrojoveho kodu;
    private boolean filterCodeByIntents(Set<Searchable> codeIntents, CurrentProjection intentsConfig) {

        boolean match = false;

        Set<Searchable> selectedIntents = new HashSet<Searchable>(intentsConfig.getCurrentlySelectedConcerns());
        if (selectedIntents.isEmpty()) {
            match = false;
        } else {
            if (CurrentProjection.MODE_AND.equals(intentsConfig.getMode())) {
                match = true;
                if (selectedIntents.contains(factory.getNilSearchable())) {
                    if (!codeIntents.isEmpty()) {
                        match = false;
                    }
                    selectedIntents.remove(factory.getNilSearchable());
                }
                for (Searchable selectedIntent : selectedIntents) {
                    if (!codeIntents.contains(selectedIntent)) {
                        match = false;
                        break;
                    }
                }
            } else if (CurrentProjection.MODE_OR.equals(intentsConfig.getMode())) {
                match = false;
                if (selectedIntents.contains(factory.getNilSearchable())) {
                    if (codeIntents.isEmpty()) {
                        match = true;
                    }
                }
                for (Searchable selectedIntent : selectedIntents) {
                    if (codeIntents.contains(selectedIntent)) {
                        match = true;
                        break;
                    }
                }
            }
        }
        return match;
    }
    
    
}
