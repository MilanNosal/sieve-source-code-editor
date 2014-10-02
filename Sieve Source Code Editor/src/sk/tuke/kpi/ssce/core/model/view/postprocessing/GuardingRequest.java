package sk.tuke.kpi.ssce.core.model.view.postprocessing;

import sk.tuke.kpi.ssce.annotations.concerns.PostProcessing;
import sk.tuke.kpi.ssce.annotations.concerns.View;
import sk.tuke.kpi.ssce.annotations.concerns.enums.PostProcessingType;
import sk.tuke.kpi.ssce.annotations.concerns.enums.ViewAspect;

/**
 *
 * @author Milan Nosáľ
 */
@PostProcessing(type = PostProcessingType.FOLDING)
@View(aspect = ViewAspect.PRESENTATION)
public class GuardingRequest {
    private int startOffset;
    private int endOffset;

    private GuardingRequest(int startOffset, int endOffset) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public static GuardingRequest create(int startOffset, int endOffset) {
        return new GuardingRequest(startOffset, endOffset);
    }
    
    public static GuardingRequest createWithLength(int startOffset, int length) {
        return new GuardingRequest(startOffset, startOffset + length);
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
