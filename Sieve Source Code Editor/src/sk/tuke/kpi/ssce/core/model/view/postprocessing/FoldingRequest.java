package sk.tuke.kpi.ssce.core.model.view.postprocessing;

import sk.tuke.kpi.ssce.annotations.concerns.SourceCodeSieving;

/**
 *
 * @author Milan Nosáľ
 */
@SourceCodeSieving(postProcessing = true)
public class FoldingRequest {
    private int startOffset;
    private int endOffset;

    private FoldingRequest(int startOffset, int endOffset) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public static FoldingRequest create(int startOffset, int endOffset) {
        return new FoldingRequest(startOffset, endOffset);
    }
    
    public static FoldingRequest createWithLength(int startOffset, int length) {
        return new FoldingRequest(startOffset, startOffset + length);
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
}
