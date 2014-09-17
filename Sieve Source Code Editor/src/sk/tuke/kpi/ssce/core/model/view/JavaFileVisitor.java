package sk.tuke.kpi.ssce.core.model.view;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePathScanner;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import sk.tuke.kpi.ssce.annotations.concerns.CodeAnalysis;
import sk.tuke.kpi.ssce.annotations.concerns.SourceCodeSieving;
import sk.tuke.kpi.ssce.annotations.concerns.View;
import sk.tuke.kpi.ssce.annotations.concerns.enums.RepresentationOf;
import sk.tuke.kpi.ssce.annotations.concerns.enums.ViewAspect;
import sk.tuke.kpi.ssce.core.projections.CurrentProjection;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import sk.tuke.kpi.ssce.sieving.interfaces.CodeSiever;

/**
 * Trieda reprezentuje skener prechadzajuci celou strukturou kompilacneho stromu
 * zdrojoveho kodu java. Sluzi ako nastroj pre zaujmoco-orientovanu projekciu
 * zdrojoveho kodu v jedenom java subore.
 * XXX: toto v podstate formuje view, tuto to treba upravit
 *
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Praca s java suborom;Model pre synchronizaciu kodu;
@View(aspect = ViewAspect.PRESENTATION)
@SourceCodeSieving
@CodeAnalysis(output = RepresentationOf.VIEW)
public class JavaFileVisitor extends TreePathScanner<JavaFile, JavaFile> {

    private final CompilationInfo info;
    private final CompilationUnitTree cu;
    @View(aspect = ViewAspect.CONCERN_EXTRACTION)
    private final ConcernExtractor extractor;
    private final SourcePositions sp;
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;
    private final CurrentProjection currentProjection;
    private final BaseDocument doc;
    private final Stack<String> contextCounter;
    private final Stack<Set<Concern>> contextOfConcerns;
    
    @SourceCodeSieving
    private final CodeSiever codeSiever;

    /**
     * Vytvori novy skener pre kompilacne info a strom, dokument, konfiguraciu
     * ...
     *
     * @param info kompilacne info.
     * @param cu kompilacna jednotka obsahujuca kompilacny strom...
     * @param sp sourcePositions.
     * @param currentProjection konfiguracia zamerov (dopyt) na zdrojovy kod.
     * @param doc dokument, nad ktorym sa bude realizovat projekcie zdrojoveho
     * kodu na zaklade konfiguracie (dopytu) zamerov.
     */
    public JavaFileVisitor(CompilationInfo info,
            ConcernExtractor extractor, CodeSiever siever,
            CurrentProjection currentProjection, BaseDocument doc) {
        this.info = info;
        this.cu = info.getCompilationUnit();
        this.sp = info.getTrees().getSourcePositions();
        this.extractor = extractor;
        this.currentProjection = currentProjection;
        this.codeSiever = siever;
        this.doc = doc;
        this.contextCounter = new Stack<String>();
        this.contextOfConcerns = new Stack<Set<Concern>>();
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
    @View(aspect = ViewAspect.PRESENTATION)
    @CodeAnalysis(output = RepresentationOf.VIEW)
    @Override
    public JavaFile visitClass(ClassTree node, JavaFile p) {
        String nameElement = node.getSimpleName().toString();

        int start = (int) sp.getStartPosition(cu, node);
        int end = (int) sp.getEndPosition(cu, node);
        
        contextOfConcerns.push(extractor.getConcernsFor(node, info));
        contextCounter.push(nameElement);
        super.visitClass(node, p);
        contextCounter.pop();

        if (codeSiever.sieveCode(contextOfConcerns, currentProjection, extractor, info)) {
            try {
                String initialTab = "";
                int lineNum = NbDocument.findLineNumber((StyledDocument) doc, (int) start);
                int lineOffset = NbDocument.findLineOffset((StyledDocument) doc, lineNum);
                if(start - lineOffset > 0) {
                    initialTab = doc.getText(lineOffset, start - lineOffset);
                } else if(start - lineOffset == 0) {
                    // everything is okay, there is just no tab
                } else {
                    System.err.println("Something went terribly wrong with the initial tab setting. lineNum: " + lineNum + " lineOffset: " + lineOffset + " start: " + start);
                }
                CodeSnippet codeSnippet = new CodeSnippet(initialTab, getContextForCode(), nameElement, "TYPE");
                codeSnippet.setCodeBinding(new BindingPositions(doc.createPosition(start), end - start));
                p.getCodeSnippets().add(codeSnippet);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        contextOfConcerns.pop();
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
    @View(aspect = ViewAspect.PRESENTATION)
    @CodeAnalysis(output = RepresentationOf.VIEW)
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

        contextOfConcerns.push(extractor.getConcernsFor(node, info));
        contextCounter.push(nameElement);
        super.visitMethod(node, p);
        contextCounter.pop();


        if (codeSiever.sieveCode(contextOfConcerns, currentProjection, extractor, info)) {
            try {
                String initialTab = "";
                int lineNum = NbDocument.findLineNumber((StyledDocument) doc, (int) start);
                int lineOffset = NbDocument.findLineOffset((StyledDocument) doc, lineNum);
                if(start - lineOffset > 0) {
                    initialTab = doc.getText(lineOffset, start - lineOffset);
                } else if(start - lineOffset == 0) {
                    // everything is okay, there is just no tab
                } else {
                    System.err.println("Something went terribly wrong with the initial tab setting. lineNum: " + lineNum + " lineOffset: " + lineOffset + " start: " + start);
                }
                CodeSnippet code = new CodeSnippet(initialTab, getContextForCode(), nameElement, "METHOD");
                code.setCodeBinding(new BindingPositions(doc.createPosition(start), end - start));
                p.getCodeSnippets().add(code);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        contextOfConcerns.pop();
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
    @View(aspect = ViewAspect.PRESENTATION)
    @CodeAnalysis(output = RepresentationOf.VIEW)
    @Override
    public JavaFile visitVariable(VariableTree node, JavaFile p) {
        String nameElement = node.getName().toString();

        int start = (int) sp.getStartPosition(cu, node);
        int end = (int) sp.getEndPosition(cu, node);

        contextOfConcerns.push(extractor.getConcernsFor(node, info));
        contextCounter.push(nameElement);
        super.visitVariable(node, p);
        contextCounter.pop();

        if (codeSiever.sieveCode(contextOfConcerns, currentProjection, extractor, info)) {
            try {
                String initialTab = "";
                int lineNum = NbDocument.findLineNumber((StyledDocument) doc, (int) start);
                int lineOffset = NbDocument.findLineOffset((StyledDocument) doc, lineNum);
                if(start - lineOffset > 0) {
                    initialTab = doc.getText(lineOffset, start - lineOffset);
                } else if(start - lineOffset == 0) {
                    // everything is okay, there is just no tab
                } else {
                    System.err.println("Something went terribly wrong with the initial tab setting. lineNum: " + lineNum + " lineOffset: " + lineOffset + " start: " + start);
                }
                CodeSnippet code = new CodeSnippet(initialTab, getContextForCode(), nameElement, "FIELD");
                code.setCodeBinding(new BindingPositions(doc.createPosition(start), end - start));
                p.getCodeSnippets().add(code);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        contextOfConcerns.pop();
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
    @View(aspect = ViewAspect.PRESENTATION)
    @CodeAnalysis(output = RepresentationOf.VIEW)
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

    @CodeAnalysis(output = RepresentationOf.VIEW)
    private String getContextForCode() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < contextCounter.size(); i++) {
            builder.append(contextCounter.get(i));
            if (i < contextCounter.size() - 1) {
                builder.append(".");
            }
        }
        return builder.toString();
    }    
}
