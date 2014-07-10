/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package sk.tuke.kpi.ssce.gui.folding;

import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.event.DocumentEvent;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import sk.tuke.kpi.ssce.core.Constants;
import sk.tuke.kpi.ssce.core.SSCEditorCore;
import sk.tuke.kpi.ssce.core.model.view.CodeSnippet;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.core.model.view.ViewModel;
import sk.tuke.kpi.ssce.core.utilities.JavaFileUtilities;
import sk.tuke.kpi.ssce.core.utilities.IntentsUtilities;
import sk.tuke.kpi.ssce.nbinterface.lexer.SieveJavaTokenId;

/**
 * Fold maintainer that creates and updates custom folds.
 *
 * @author Dusan Balek, Miloslav Metelka, Matej Nosáľ
 * @version 1.00
 */
//SsceIntent:Zbalovanie kodu, folding;
final class SieveElementFoldManager implements FoldManager, Runnable {

    private AtomicBoolean releasedManager=new AtomicBoolean(false);
    private static final Logger LOG = Logger.getLogger(SsceIntentFoldManager.class.getName());
    public static final FoldType TAG_FOLD_TYPE = new FoldType("sieve-element-fold"); // NOI18N
    private FoldOperation operation;
    private Document doc;
    private org.netbeans.editor.GapObjectArray markArray = new org.netbeans.editor.GapObjectArray();
    private int minUpdateMarkOffset = Integer.MAX_VALUE;
    private int maxUpdateMarkOffset = -1;
    private List removedFoldList;
//    private HashMap tagFoldId = new HashMap();
    private static final RequestProcessor RP = new RequestProcessor(SsceIntentFoldManager.class.getName());
    private final RequestProcessor.Task task = RP.create(this);

    public void init(FoldOperation operation) {
        this.operation = operation;
    }

    private FoldOperation getOperation() {
        return operation;
    }

    public void initFolds(FoldHierarchyTransaction transaction) {
        doc = getOperation().getHierarchy().getComponent().getDocument();
        task.schedule(300);
    }

    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        processRemovedFolds(transaction);
        task.schedule(300);
    }

    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        processRemovedFolds(transaction);
        removeAffectedMarks(evt, transaction);
        task.schedule(300);
    }

    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    public void removeEmptyNotify(Fold emptyFold) {
        removeFoldNotify(emptyFold);
    }

    public void removeDamagedNotify(Fold damagedFold) {
        removeFoldNotify(damagedFold);
    }

    public void expandNotify(Fold expandedFold) {
    }

    public void release() {
        releasedManager.set(true);
    }

    public void run() {
        if(releasedManager.get()){
            return;
        }
        ((BaseDocument) doc).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(doc);
            if (th != null && th.isActive()) {
                FoldHierarchy hierarchy = getOperation().getHierarchy();
                hierarchy.lock();
                try {
                    FoldHierarchyTransaction transaction = getOperation().openTransaction();
                    try {
                        updateFolds(th.tokenSequence(), transaction);
                    } finally {
                        transaction.commit();
                    }
                } finally {
                    hierarchy.unlock();
                }
            }
        } finally {
            ((BaseDocument) doc).readUnlock();
        }
    }

    private void removeFoldNotify(Fold removedFold) {
        if (removedFoldList == null) {
            removedFoldList = new ArrayList(3);
        }
        removedFoldList.add(removedFold);
    }

    private void removeAffectedMarks(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        int removeOffset = evt.getOffset();
        int markIndex = findMarkIndex(removeOffset);
        if (markIndex < getMarkCount()) {
            FoldMarkInfo mark;
            while (markIndex >= 0 && (mark = getMark(markIndex)).getStartOffset() == removeOffset) {
                mark.release(false, transaction);
                removeMark(markIndex);
                markIndex--;
            }
        }
    }

    private void processRemovedFolds(FoldHierarchyTransaction transaction) {
        if (removedFoldList != null) {
            for (int i = removedFoldList.size() - 1; i >= 0; i--) {
                Fold removedFold = (Fold) removedFoldList.get(i);
                FoldMarkInfo mark = (FoldMarkInfo) getOperation().getExtraInfo(removedFold);
//                if (mark.getId() != null)
//                    tagFoldId.put(mark.getId(), Boolean.valueOf(removedFold.isCollapsed())); // remember the last fold's state before remove
//                FoldMarkInfo endMark = mark.getPairMark(); // get prior releasing
                if (getOperation().isStartDamaged(removedFold) || getOperation().isEndDamaged(removedFold)) { // start mark area was damaged
                    mark.release(true, transaction); // forced remove
                }
//                if (getOperation().isEndDamaged(removedFold)) {
//                    endMark.release(true, transaction);
//                }
            }
        }
        removedFoldList = null;
    }

    private void markUpdate(FoldMarkInfo mark) {
        markUpdate(mark.getStartOffset());
    }

    private void markUpdate(int offset) {
        if (offset < minUpdateMarkOffset) {
            minUpdateMarkOffset = offset;
        }
        if (offset > maxUpdateMarkOffset) {
            maxUpdateMarkOffset = offset;
        }
    }

    private FoldMarkInfo getMark(int index) {
        return (FoldMarkInfo) markArray.getItem(index);
    }

    private int getMarkCount() {
        return markArray.getItemCount();
    }

    private void removeMark(int index) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Removing mark from ind=" + index + ": " + getMark(index)); // NOI18N
        }
        markArray.remove(index, 1);
    }

    private void insertMark(int index, FoldMarkInfo mark) {
        markArray.insertItem(index, mark);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Inserted mark at ind=" + index + ": " + mark); // NOI18N
        }
    }

    private int findMarkIndex(int offset) {
        int markCount = getMarkCount();
        int low = 0;
        int high = markCount - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int midMarkOffset = getMark(mid).getStartOffset();

            if (midMarkOffset < offset) {
                low = mid + 1;
            } else if (midMarkOffset > offset) {
                high = mid - 1;
            } else {
                // mark starting exactly at the given offset found
                // If multiple -> find the one with highest index
                mid++;
                while (mid < markCount && getMark(mid).getStartOffset() == offset) {
                    mid++;
                }
                mid--;
                return mid;
            }
        }
        return low; // return higher index (e.g. for insert)
    }

    private List<FoldMarkInfo> getMarkList(TokenSequence seq) {
        List<FoldMarkInfo> markList = null;

        ViewModel model = null;
        SSCEditorCore core = (SSCEditorCore) doc.getProperty(Constants.SSCE_CORE_OBJECT_PROP);
        if (core != null) {
            model = core.getModel();
        }
        if (model == null || !model.isInitialized()) {
            return markList;
        }

        for (int i = 0; i < model.size(); i++) {
            JavaFile file = model.getFileAt(i);

            FoldMarkInfo fold = null;



            try {
                if (new JavaFileUtilities().getImports((BaseDocument) doc, file.getImportsBinding().getStartPositionSieveDocument(), file.getImportsBinding().getLengthBindingAreaSieveDocument()).getCount() > 0) {
                    fold = new FoldMarkInfo(file.getImportsBinding().getStartPositionSieveDocument(), file.getImportsBinding().getEndPositionSieveDocument(), 0, 0, true, "imports");
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
                fold = null;
            }
            if (fold != null) {
                if (markList == null) {
                    markList = new ArrayList<FoldMarkInfo>();
                }
                markList.add(fold);
            }


            for (CodeSnippet code : file.getCodeSnippets()) {



                seq.move(code.getCodeBinding().getStartPositionSieveDocument());
                while (seq.movePrevious() && !SieveJavaTokenId.CODE.equals(seq.token().id()));

                if (SieveJavaTokenId.CODE.equals(seq.token().id())) {
                    while (seq.moveNext() && !SieveJavaTokenId.COLON.equals(seq.token().id()) && !SieveJavaTokenId.LPAREN.equals(seq.token().id()));
                    if (SieveJavaTokenId.COLON.equals(seq.token().id()) || SieveJavaTokenId.LPAREN.equals(seq.token().id())) {

                        fold = null;
                        try {
                            fold = new FoldMarkInfo(seq.offset(), code.getCodeBinding().getEndPositionSieveDocument() + 1, code.getCodeBinding().getStartPositionSieveDocument() - seq.offset(), 0, true, code.getElementType()+" ~ "+code.getFullElementName());
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                            fold = null;
                        }
                        if (fold != null) {
                            if (markList == null) {
                                markList = new ArrayList<FoldMarkInfo>();
                            }
                            markList.add(fold);
                        }

                    }
                }

//                fold = null;
//                try {
//                    fold = new FoldMarkInfo(code.getCodeBinding().getStartPositionSieveDocument(), code.getCodeBinding().getEndPositionSieveDocument() + 1, true, "...");
//                } catch (BadLocationException ex) {
//                    Exceptions.printStackTrace(ex);
//                    fold = null;
//                }
//                if (fold != null) {
//                    if (markList == null) {
//                        markList = new ArrayList<FoldMarkInfo>();
//                    }
//                    markList.add(fold);
//                }

            }


//            fold = null;
//            try {
//                fold = new FoldMarkInfo(file.getStartFile(), file.getEndFile(), true, "File");
//            } catch (BadLocationException ex) {
//                Exceptions.printStackTrace(ex);
//                fold = null;
//            }
//            if (fold != null) {
//                if (markList == null) {
//                    markList = new ArrayList<FoldMarkInfo>();
//                }
//                markList.add(fold);
//            }

        }



//        for (seq.moveStart(); seq.moveNext();) {
//            Token token = seq.token();
//            FoldMarkInfo info;
//            try {
//                info = scanToken(token);
//            } catch (BadLocationException e) {
//                LOG.log(Level.WARNING, null, e);
//                info = null;
//            }
//
//            if (info != null) {
//                if (markList == null) {
//                    markList = new ArrayList<FoldMarkInfo>();
//                }
//                markList.add(info);
//            }
//        }

        return markList;
    }

    private void processTokenList(TokenSequence seq, FoldHierarchyTransaction transaction) {
        List<FoldMarkInfo> markList = getMarkList(seq);
        int markListSize;
        if (markList != null && ((markListSize = markList.size()) > 0)) {
            // Find the index for insertion
            int offset = ((FoldMarkInfo) markList.get(0)).getStartOffset();
            int arrayMarkIndex = findMarkIndex(offset);
            // Remember the corresponding mark in the array as well
            FoldMarkInfo arrayMark;
            int arrayMarkOffset;
            if (arrayMarkIndex < getMarkCount()) {
                arrayMark = getMark(arrayMarkIndex);
                arrayMarkOffset = arrayMark.getStartOffset();
            } else { // at last mark
                arrayMark = null;
                arrayMarkOffset = Integer.MAX_VALUE;
            }

            for (int i = 0; i < markListSize; i++) {
                FoldMarkInfo listMark = (FoldMarkInfo) markList.get(i);
                int listMarkOffset = listMark.getStartOffset();
                if (i == 0 || i == markListSize - 1) {
                    // Update the update-offsets by the first and last marks in the list
                    markUpdate(listMarkOffset);
                }
                while (listMarkOffset >= arrayMarkOffset) {
                    if (listMarkOffset == arrayMarkOffset) {
                        // At the same offset - likely the same mark
                        //   -> retain the collapsed state
                        listMark.setCollapsed(arrayMark.isCollapsed());
                    }
                    if (!arrayMark.isReleased()) { // make sure that the mark is released
                        arrayMark.release(false, transaction);
                    }
                    removeMark(arrayMarkIndex);
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Removed dup mark from ind=" + arrayMarkIndex + ": " + arrayMark); // NOI18N
                    }
                    if (arrayMarkIndex < getMarkCount()) {
                        arrayMark = getMark(arrayMarkIndex);
                        arrayMarkOffset = arrayMark.getStartOffset();
                    } else { // no more marks
                        arrayMark = null;
                        arrayMarkOffset = Integer.MAX_VALUE;
                    }
                }
                // Insert the listmark
                insertMark(arrayMarkIndex, listMark);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Inserted mark at ind=" + arrayMarkIndex + ": " + listMark); // NOI18N
                }
                arrayMarkIndex++;
            }
        }
    }

    private void updateFolds(TokenSequence seq, FoldHierarchyTransaction transaction) {

        if (seq != null && !seq.isEmpty()) {
            processTokenList(seq, transaction);
        }

        if (maxUpdateMarkOffset == -1) { // no updates
            return;
        }

        // Find the first mark to update and init the prevMark and parentMark prior the loop
        int index = findMarkIndex(minUpdateMarkOffset);
//        FoldMarkInfo prevMark;
//        FoldMarkInfo parentMark;
//        if (index == 0) { // start from begining
//            prevMark = null;
//            parentMark = null;
//        } else {
//            prevMark = getMark(index - 1);
//            parentMark = prevMark.getParentMark();
//        }

        // Iterate through the changed marks in the mark array 
        int markCount = getMarkCount();
        while (index < markCount) { // process the marks
            FoldMarkInfo mark = getMark(index);

            // If the mark was released then it must be removed
            if (mark.isReleased()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Removing released mark at ind=" + index + ": " + mark); // NOI18N
                }
                removeMark(index);
                markCount--;
                continue;
            }

            mark.ensureFoldExists(transaction);


//            // Update mark's status (folds, parentMark etc.)
//            if (mark.isStartMark()) { // starting a new fold
//                if (prevMark == null || prevMark.isStartMark()) { // new level
//                    mark.setParentMark(prevMark); // prevMark == null means root level
//                    parentMark = prevMark;
//
//                } // same level => parent to the parent of the prevMark
//
//            } else { // end mark
//                if (prevMark != null) {
//                    if (prevMark.isStartMark()) { // closing nearest fold
//                        prevMark.setEndMark(mark, false, transaction);
//
//                    } else { // prevMark is end mark - closing its parent fold
//                        if (parentMark != null) {
//                            // mark's parent gets set as well
//                            parentMark.setEndMark(mark, false, transaction);
//                            parentMark = parentMark.getParentMark();
//
//                        } else { // prevMark's parentMark is null (top level)
//                            mark.makeSolitaire(false, transaction);
//                        }
//                    }
//                    
//                } else { // prevMark is null
//                    mark.makeSolitaire(false, transaction);
//                }
//            }
//
//            // Set parent mark of the mark
//            mark.setParentMark(parentMark);
//
//            
//            prevMark = mark;

            index++;
        }

        minUpdateMarkOffset = Integer.MAX_VALUE;
        maxUpdateMarkOffset = -1;

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("MARKS DUMP:\n" + this); //NOI18N
        }
    }

    private final class FoldMarkInfo {

        private Position startPos;
        private Position endPos;
        private int startGuard;
        private int endGuard;
        private boolean collapsed;
        private String description;
        /**
         * Fold that corresponds to this mark (if it's start mark). It can be
         * null if this mark is end mark or if it currently does not have the
         * fold assigned.
         */
        private Fold fold;
        private boolean released;

        private FoldMarkInfo(int startOffset,
                int endOffset, int startGuard, int endGuard, boolean collapsed, String description)
                throws BadLocationException {

            this.startPos = doc.createPosition(startOffset);
            this.endPos = doc.createPosition(endOffset);
            this.startGuard = startGuard;
            this.endGuard = endGuard;
            this.collapsed = collapsed;
            this.description = description;

//            ensureFoldExists(transaction);
        }

        public String getDescription() {
            return description;
        }

//        public boolean isStartMark() {
//            return true;
//        }
        public int getLength() {
            return endPos.getOffset() - startPos.getOffset();
        }

        public int getStartOffset() {
            return startPos.getOffset();
        }

        public int getEndOffset() {
            return endPos.getOffset();
        }

        public boolean isCollapsed() {
            return (fold != null) ? fold.isCollapsed() : collapsed;
        }

        public boolean hasFold() {
            return (fold != null);
        }

        public void setCollapsed(boolean collapsed) {
            this.collapsed = collapsed;
        }

//        public boolean isSolitaire() {
//            return false;//(pairMark == null);
//        }
//        public void makeSolitaire(boolean forced, FoldHierarchyTransaction transaction) {
//            if (!isSolitaire()) {
//                if (isStartMark()) {
//                    setEndMark(null, forced, transaction);
//                } else { // end mark
//                    getPairMark().setEndMark(null, forced, transaction);
//                }
//            }
//        }
        public boolean isReleased() {
            return released;
        }

        /**
         * Release this mark and mark for update.
         */
        public void release(boolean forced, FoldHierarchyTransaction transaction) {
            if (!released) {
                releaseFold(forced, transaction);
                released = true;
                markUpdate(this);
            }
        }

//        public FoldMarkInfo getPairMark() {
//            return pairMark;
//        }
//        private void setPairMark(FoldMarkInfo pairMark) {
//            this.pairMark = pairMark;
//        }
//        public void setEndMark(FoldMarkInfo endMark, boolean forced,
//        FoldHierarchyTransaction transaction) {
//            if (!isStartMark()) {
//                throw new IllegalStateException("Not start mark"); // NOI18N
//            }
//            if (pairMark == endMark) {
//                return;
//            }
//            
//            if (pairMark != null) { // is currently paired to an end mark
//                releaseFold(forced, transaction);
//                pairMark.setPairMark(null);
//            }
//
//            pairMark = endMark;
//            if (endMark != null) {
//                if (!endMark.isSolitaire()) { // make solitaire first
//                    endMark.makeSolitaire(false, transaction); // not forced here
//                }
//                endMark.setPairMark(this);
//                endMark.setParentMark(this.getParentMark());
//                ensureFoldExists(transaction);
//            }
//        }
//        public FoldMarkInfo getParentMark() {
//            return parentMark;
//        }
//        
//        public void setParentMark(FoldMarkInfo parentMark) {
//            this.parentMark = parentMark;
//        }
        private void releaseFold(boolean forced, FoldHierarchyTransaction transaction) {
//            if (isSolitaire() || !isStartMark()) {
//               throw new IllegalStateException();
//            }

            if (fold != null) {
                setCollapsed(fold.isCollapsed()); // serialize the collapsed info
                if (!forced) {
                    getOperation().removeFromHierarchy(fold, transaction);
                }
                fold = null;
            }
        }

        public Fold getFold() {
//            if (isSolitaire()) {
//                return null;
//            }
//            if (!isStartMark()) {
//                return pairMark.getFold();
//            }
            return fold;
        }

        //<editor-fold>
        //</editor-fold>
        public void ensureFoldExists(FoldHierarchyTransaction transaction) {
//            if (isSolitaire() || !isStartMark()) {
//                throw new IllegalStateException();
//            }

            if (fold == null) {
                try {
                    int startOffset = getStartOffset();
                    int guardedLength = getLength();
                    int endOffset = getEndOffset();
                    fold = getOperation().addToHierarchy(
                            TAG_FOLD_TYPE, getDescription(), collapsed,
                            startOffset, endOffset,
                            startGuard, endGuard,
                            this,
                            transaction);
                } catch (BadLocationException e) {
                    LOG.log(Level.WARNING, null, e);
                }
            }
        }
//        public @Override String toString() {
//            StringBuffer sb = new StringBuffer();
//            sb.append(isStartMark() ? 'S' : 'E');  // NOI18N
//            
//            // Check whether this mark (or its pair) has fold
//            if (hasFold() || (!isSolitaire() && getPairMark().hasFold())) {
//                sb.append("F"); // NOI18N
//                
//                // Check fold's status
//                if (isStartMark() && (isSolitaire()
//                        || getStartOffset() != fold.getStartOffset()
//                        || getPairMark().getEndOffset() != fold.getEndOffset())
//                ) {
//                    sb.append("!!<"); // NOI18N
//                    sb.append(fold.getStartOffset());
//                    sb.append(","); // NOI18N
//                    sb.append(fold.getEndOffset());
//                    sb.append(">!!"); // NOI18N
//                }
//            }
//
//            // Append mark's internal status
//            sb.append(" ("); // NOI18N
//            sb.append("o="); // NOI18N
//            sb.append(startPos.getOffset());
//            sb.append(", l="); // NOI18N
//            sb.append(length);
//            sb.append(", d='"); // NOI18N
//            sb.append(description);
//            sb.append('\'');
//            if (getPairMark() != null) {
//                sb.append(", <->"); // NOI18N
//                sb.append(getPairMark().getStartOffset());
//            }
//            if (getParentMark() != null) {
//                sb.append(", ^"); // NOI18N
//                sb.append(getParentMark().getStartOffset());
//            }
//            sb.append(')');
//            
//            return sb.toString();
//        }
    }

    public static final class Factory implements FoldManagerFactory {

        public FoldManager createFoldManager() {
//            System.out.println("\nHello Mattoke\n");
            return new SieveElementFoldManager();
        }
    }
}
