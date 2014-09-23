package sk.tuke.kpi.ssce.core.model.view.postprocessing;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.util.Exceptions;
import sk.tuke.kpi.ssce.annotations.concerns.SourceCodeSieving;

/**
 *
 * @author Milan Nosáľ
 */
@SourceCodeSieving(postProcessing = true)
public class FoldingRequest {
    private int startOffset;
    private int endOffset;
    private String description;
    private int guardedStartLength;
    private int guardedEndLength;

    private FoldingRequest(int startOffset, int endOffset, String description, int guardedStartLength, int guardedEndLength) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.description = description;
        this.guardedStartLength = guardedStartLength;
        this.guardedEndLength = guardedEndLength;
    }

    public static FoldingRequest create(int startOffset, int endOffset, String description, int guardedStartLength, int guardedEndLength) {
        return new FoldingRequest(startOffset, endOffset, description, guardedStartLength, guardedEndLength);
    }
    
    public static FoldingRequest create(int startOffset, int endOffset, String description) {
        return new FoldingRequest(startOffset, endOffset, description, 0, 0);
    }
    
    public static FoldingRequest createWithLength(int startOffset, int length, String description, int guardedStartLength, int guardedEndLength) {
        return new FoldingRequest(startOffset, startOffset + length, description, guardedStartLength, guardedEndLength);
    }
    
    public static FoldingRequest createWithLength(int startOffset, int length, String description) {
        return new FoldingRequest(startOffset, startOffset + length, description, 0, 0);
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int newStartOffset) {
        this.startOffset = newStartOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int newEndOffset) {
        this.endOffset = newEndOffset;
    }
    
    public int getLength() {
        return endOffset - startOffset;
    }

    public void setLength(int newLength) {
        this.endOffset = startOffset + newLength;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getGuardedStartLength() {
        return guardedStartLength;
    }

    public void setGuardedStartLength(int guardedStartLength) {
        this.guardedStartLength = guardedStartLength;
    }

    public int getGuardedEndLength() {
        return guardedEndLength;
    }

    public void setGuardedEndLength(int guardedEndLength) {
        this.guardedEndLength = guardedEndLength;
    }
    
    public void tryAddToFoldHierarchy(FoldOperation operation, FoldType type, FoldHierarchyTransaction transaction) {
        try {
            operation.addToHierarchy(type, description, true, 
                    startOffset, endOffset, 
                    guardedEndLength, guardedEndLength, 
                    this, transaction);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
