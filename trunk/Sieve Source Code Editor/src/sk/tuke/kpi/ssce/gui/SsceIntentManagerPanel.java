package sk.tuke.kpi.ssce.gui;

import java.io.File;
import java.util.*;
import javax.lang.model.type.DeclaredType;
import javax.swing.DefaultListModel;
import javax.swing.JEditorPane;
import javax.swing.event.*;
import org.netbeans.editor.BaseDocument;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import sk.tuke.kpi.ssce.core.Constants;
import sk.tuke.kpi.ssce.core.SSCEditorCore;
import sk.tuke.kpi.ssce.core.model.view.CodeSnippet;
import sk.tuke.kpi.ssce.core.model.availableprojections.CodeSnippetConcerns;
import sk.tuke.kpi.ssce.core.model.view.JavaFile;
import sk.tuke.kpi.ssce.core.model.availableprojections.ProjectionsModel;
import sk.tuke.kpi.ssce.concerns.interfaces.Concern;

/**
 * Trieda reprezentuje GUI koponent pre preiradovanie zamerov fragmentom kodu.
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Komponent grafickeho rozhrania;
public class SsceIntentManagerPanel extends javax.swing.JPanel implements ProjectionsModel.ConcernsChangeListener, ListSelectionListener, ChangeListener {

    /**
     * Mod v ako pracuje tento komponent.
     */
    public enum Mode {

        /**
         * Praca s pomocnym suborom .sj.
         */
        SIEVE_DOCUMENT,
        /**
         * Praca s java suborom.
         */
        JAVA_DOCUMENT
    }
    private SSCEditorCore core = null;
    private String filePath = null;
    private EditorCookie editorCookie = null;
    private JEditorPane editorPane = null;
    private final Mode mode;
    private CodeSnippetConcerns selectionCodeIntents = null;

    /**
     * Vytvori novy formular s modom mode.
     * @param mode mod.
     */
    //SsceIntent:Komponent grafickeho rozhrania;
    public SsceIntentManagerPanel(Mode mode) {
        initComponents();
        this.mode = mode;
    }

    /**
     * Nastavi nove jadro editora modulu SSCE a novy dokument.
     * @param core nove jadro editora modulu SSCE.
     * @param doc novy dokument
     */
    //SsceIntent:Aktualizacia grafickeho rozhrania;
    public void setSSCEditorCore(SSCEditorCore core, BaseDocument doc) {

        EditorCookie ec = null;
        switch (mode) {
            case JAVA_DOCUMENT:
                ec = getEditorCookie((String) doc.getProperty(Constants.FILE_NAME_PROP));//this.core.getModel().get(this.filePath).getEditorCookie();
                break;
            case SIEVE_DOCUMENT:
                ec = core.getModel().getEditorCookieSieveDocument();
                break;
        }
        if (this.editorCookie != null) {
            if (this.editorCookie.equals(ec)) {
//                refreshModel();
                return;
            } else {
                removeSSCEditorCore();
            }
        }
        if (ec == null) {
            removeSSCEditorCore();
            return;
        }
//        
//
//        if (this.core != null) {
//            if (this.core.equals(core)) {
//                //TODO: co treba: actualize
//                return;
//            } else {
//                removeSSCEditorCore();
//            }
//        }
//        if (core == null) {
//            return;
//        }

        this.core = core;
        this.editorCookie = ec;

        switch (mode) {
            case JAVA_DOCUMENT:
                System.out.println("TEST filePath ====    >>>   " + this.filePath);
                System.out.println("TEST core.getModel().get  ====    >>>   " + this.core.getModel().getFileAt(this.filePath));

                this.filePath = (String) doc.getProperty(Constants.FILE_NAME_PROP);
//                this.editorCookie = getEditorCookie(filePath);//this.core.getModel().get(this.filePath).getEditorCookie();
                break;
            case SIEVE_DOCUMENT:
//                this.editorCookie = this.core.getModel().getEditorCookieSieveDocument();
                this.filePath = null;
                break;
        }
        editorPane = ec.getOpenedPanes() != null ? ec.getOpenedPanes()[0] : null;

        refreshModel();


        this.core.getAvailableProjections().addChangeListener(this);
        if (editorPane != null) {
            editorPane.getCaret().addChangeListener(this);
            System.out.println("textComponent for file = " + editorPane.getDocument().getProperty(Constants.FILE_NAME_PROP));
        }

    }

    //SsceIntent:Praca s java suborom;
    private EditorCookie getEditorCookie(String filePath) {
        FileObject fobj = FileUtil.toFileObject(new File(filePath));
        DataObject dobj;
        try {
            dobj = DataObject.find(fobj);
            if (dobj != null) {
                EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
                return ec;
            }
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Odstrani aktualne jadro editora modulu SSCE a aktualne nastaveny dokument.
     */
    //SsceIntent:Aktualizacia grafickeho rozhrania;
    public void removeSSCEditorCore() {
        if (core == null) {
            return;
        }

        this.listIntents.removeListSelectionListener(this);

        core.getAvailableProjections().removeChangeListener(this);

        if (editorPane != null) {
            editorPane.getCaret().removeChangeListener(this);//CaretListener(this);
        }
        core = null;
        this.editorCookie = null;
        this.filePath = null;
        this.editorPane = null;
        this.selectionCodeIntents = null;

        System.out.println("   removeSSCEditorCore()   ");

    }

    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;Aktualizacia grafickeho rozhrania;
    synchronized private void refreshModel() {
//        if()
//        try {
        if (this.core == null) {
            return;
        }
        if (core.getAvailableProjections().isOutOfDate()) {
            return;
        }
        
        this.textFieldNewIntent.setEnabled(true);
        this.listIntents.setEnabled(true);
        this.listIntents.removeListSelectionListener(this);


        //remember selected code
        selectionCodeIntents = getSelectedCodeIntents();

        if (selectionCodeIntents == null) {
            setDisabledComponents();
            return;
        } else {
            setEnabledComponents();

            this.labelJavaElementFullName.setText(selectionCodeIntents.getCodeHead());// + "   " + selectionCodeIntents.getStartPositionIntentsComment().getOffset() + "   " + selectionCodeIntents.getLengthIntentsComment());
        }


        DefaultListModel model = new DefaultListModel();
        List<Concern> intents = new ArrayList<Concern>(this.core.getAvailableProjections().getAllConcerns());
        Collections.sort(intents);
//        model.addElement(IntentsConfiguration.IntentsConfiguration.UNTAGGED_CODE);
        for (Concern intent : intents) {
            model.addElement(intent);
        }

        this.listIntents.setModel(model);

        Set<Concern> selectedIntents = selectionCodeIntents.getConcerns();//core.getConfiguration().getSelectedIntents();
        List<Integer> indices = new ArrayList<Integer>();

        for (int i = 0; i < model.size(); i++) {
            Object searchable = model.get(i);
            for(Concern intent : selectedIntents) {
                if(intent.equals(searchable)) {
                    indices.add(i);
                    break;
                }
            }
        }
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indicesArray.length; i++) {
            indicesArray[i] = indices.get(i);
        }

        listIntents.setSelectedIndices(indicesArray);

//        for (String intentToSelect : core.getConfiguration().getSelectedIntents()) {
//            jListIntents.setSelectedValue(intentToSelect, false);
//        }
        //System.out.println("actual selection refreshing :  " + Arrays.toString(selectionCodeIntents.getIntents().toArray()));


        this.listIntents.addListSelectionListener(this);
//        }
//        finally {
//        }
    }

    private CodeSnippetConcerns getSelectedCodeIntents() {
        if (editorPane == null) {
            return null;
        }
        String javaFilePath = null;
        int javaFileCaret = -1;

        switch (mode) {
            case JAVA_DOCUMENT:
                javaFilePath = filePath;
                javaFileCaret = editorPane.getCaretPosition();
                break;
            case SIEVE_DOCUMENT:
                JavaFile javaFile = core.getModel().getFileBySJOffset(editorPane.getCaretPosition());
                if (javaFile != null) {
                    javaFilePath = javaFile.getFilePath();
                    CodeSnippet code = javaFile.getCodeSnippetBySJOffset(editorPane.getCaretPosition());
                    if (code == null) {
                        javaFileCaret = -1;
                    } else {
                        javaFileCaret = code.getCodeBinding().getStartPositionJavaDocument() + (editorPane.getCaretPosition() - code.getCodeBinding().getStartPositionSieveDocument());
                    }
                }
                break;
        }


        CodeSnippetConcerns codeIntents = null;
        if (javaFilePath != null && javaFileCaret != -1) {
            codeIntents = this.core.getAvailableProjections().get(javaFilePath).findForOffset(javaFileCaret);
        }

        if (mode == Mode.JAVA_DOCUMENT && (filePath == null)) {
            throw new NullPointerException();
        }

        System.out.println("  CodeIntents codeIntents this.filePath= " + filePath + "    mode=" + mode);

        System.out.println("  CodeIntents codeIntents =     " + codeIntents + "    javaFilePath= " + javaFilePath + "      javaFileCaret = " + javaFileCaret);

        return codeIntents;
    }

    //SsceIntent:Aktualizacia grafickeho rozhrania;
    private void setDisabledComponents() {
        this.labelJavaElementFullName.setText("No Selection");
//        this.textFieldNewIntent.setText("");
//        contentPanel.remove(panelWithIntents);


//        this.listIntents.setBorder(null);
        this.textFieldNewIntent.setVisible(false);
//        this.listIntents.setModel(new DefaultListModel());
        this.jScrollPane1.setVisible(false);//setEnabled(false);
    }

    //SsceIntent:Aktualizacia grafickeho rozhrania;
    private void setEnabledComponents() {
//        contentPanel.add(panelWithIntents, java.awt.BorderLayout.CENTER);
//        this.labelJavaElementFullName.setText("No Selection");
//        this.textFieldNewIntent.setText("");
        this.textFieldNewIntent.setVisible(true);//setEnabled(true);
        this.jScrollPane1.setVisible(true);//setEnabled(true);
//        this.listIntents.setBorder(border);
    }

    /**
     * Zrealizuje na zaklade zmeny v slekecii zamerov v zozname zamerov priradenie zamerov zvolenemu fragmentu kodu.
     * @param event
     */
    //This is for listening selection of listIntents // tu sa ma sledovat zmena suboru
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;Notifikacia zmeny dopytu na zdrojovy kod;
    @Override
    @Deprecated
    public void valueChanged(ListSelectionEvent event) {
        System.err.println("So far read-only. (valueChanged)");
//        this.textFieldNewIntent.setEnabled(false);
//        this.listIntents.setEnabled(false);
//
//
//        if (this.core == null || core.getIntentsMapping().isOutOfDate()) {
//            System.out.println("----->>>>    core.getIntentsMapping().isOutOfDate()");
//
//            return;
//        }
//        Object[] objects = listIntents.getSelectedValues();
//        String[] intents = Arrays.copyOf(objects, objects.length, String[].class);
//        Set<String> selectedIntents = new HashSet<String>(Arrays.asList(intents));
//
//        if (selectionCodeIntents == null) {
//            selectionCodeIntents = getSelectedCodeIntents();
//
//        }
//        if (selectionCodeIntents == null) {
//            System.out.println("----->>>>    selectionCodeIntents == null    in    valueChanged");
//            return;
//        }
//
////        this.editorPane.getCaret().removeChangeListener(this);
////        this.ignoreCaretChange = true;
//        core.getIntentsMapping().setOutOfDate();
//        this.core.getBindingUtilities().updateIntentsComment(selectionCodeIntents, selectedIntents);
//        selectionCodeIntents = null;
//
//        System.out.println("actual selection :  " + Arrays.toString(objects));
////        this.editorPane.getCaret().addChangeListener(this);
    }
//    private boolean ignoreCaretChange = false;

    /**
     * Aktualizuje zoznam zamerom na zaklade zmeny v mapovovani zamerov.
     * @param event
     */
    //This is for IntentsMapping
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    @Override
    public void concernsChanged(ProjectionsModel.ConcernsChangedEvent event) {
        System.out.println("Ssce manager   intentsChanged = " + new Date().getTime());
        refreshModel();
//        ignoreCaretChange = false;
    }

    /**
     * Aktualizuje zvoleny element zdrojoveho kodu na zaklade pozicie kurzora v editore.
     * @param e
     */
    //when careet position is changed
    @Override
    public void stateChanged(ChangeEvent e) {
        if (this.core == null || core.getAvailableProjections().isOutOfDate()) {
            return;
        }
        System.out.println("Ssce manager    astateChanged = " + new Date().getTime());
        refreshModel();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        labelJavaElementFullName = new javax.swing.JLabel();
        contentPanel = new javax.swing.JPanel();
        panelWithTags = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listIntents = new javax.swing.JList();
        textFieldNewIntent = new javax.swing.JTextField();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(SsceIntentManagerPanel.class, "SsceIntentManagerPanel.jLabel1.text")); // NOI18N

        labelJavaElementFullName.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        labelJavaElementFullName.setText(org.openide.util.NbBundle.getMessage(SsceIntentManagerPanel.class, "SsceIntentManagerPanel.labelJavaElementFullName.text")); // NOI18N

        contentPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setViewportView(listIntents);

        textFieldNewIntent.setText(org.openide.util.NbBundle.getMessage(SsceIntentManagerPanel.class, "SsceIntentManagerPanel.textFieldNewIntent.text")); // NOI18N
        textFieldNewIntent.setToolTipText(org.openide.util.NbBundle.getMessage(SsceIntentManagerPanel.class, "SsceIntentManagerPanel.textFieldNewIntent.hint")); // NOI18N
        textFieldNewIntent.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textFieldNewIntentKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textFieldNewIntentKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textFieldNewIntentKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout panelWithTagsLayout = new javax.swing.GroupLayout(panelWithTags);
        panelWithTags.setLayout(panelWithTagsLayout);
        panelWithTagsLayout.setHorizontalGroup(
            panelWithTagsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(textFieldNewIntent, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
        );
        panelWithTagsLayout.setVerticalGroup(
            panelWithTagsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelWithTagsLayout.createSequentialGroup()
                .addComponent(textFieldNewIntent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE))
        );

        contentPanel.add(panelWithTags, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(labelJavaElementFullName))
                    .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(labelJavaElementFullName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void textFieldNewIntentKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldNewIntentKeyReleased
        System.err.println("So far read-only. (textFieldNewIntentKeyReleased)");
//        switch (evt.getKeyCode()) {
//            case KeyEvent.VK_COLON:
//            case KeyEvent.VK_SEMICOLON:
//            case KeyEvent.VK_SPACE:
//                textFieldNewIntent.setText(textFieldNewIntent.getText().replaceAll("[:;]", ""));
//                break;
//        }
//        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
//
//            String newIntent = textFieldNewIntent.getText().trim();
//            if (newIntent.length() == 0) {
//                return;
//            }
//            textFieldNewIntent.setText("");
//
//
//            this.textFieldNewIntent.setEnabled(false);
//            this.listIntents.setEnabled(false);
//
//
//            if (this.core == null || core.getIntentsMapping().isOutOfDate()) {
//                System.out.println("----->>>>    core.getIntentsMapping().isOutOfDate()");
//
//                return;
//            }
//
//            if (selectionCodeIntents == null) {
//                selectionCodeIntents = getSelectedCodeIntents();
//
//            }
//            if (selectionCodeIntents == null) {
//                System.out.println("----->>>>    selectionCodeIntents == null    in    valueChanged");
//                return;
//            }
//
//
//            Set<String> selectedIntents = selectionCodeIntents.getIntents();// new HashSet<String>(Arrays.asList(intents));
//
//            selectedIntents.add(newIntent);
//
////        this.editorPane.getCaret().removeChangeListener(this);
////        this.ignoreCaretChange = true;
//            core.getIntentsMapping().setOutOfDate();
//            this.core.getBindingUtilities().updateIntentsComment(selectionCodeIntents, selectedIntents);
//            selectionCodeIntents = null;
//
//        }
    }//GEN-LAST:event_textFieldNewIntentKeyReleased

    private void textFieldNewIntentKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldNewIntentKeyTyped
        System.err.println("So far read-only. (textFieldNewIntentKeyTyped)");
//        switch (evt.getKeyCode()) {
//            case KeyEvent.VK_COLON:
//            case KeyEvent.VK_SEMICOLON:
//                evt.consume();
//                break;
//        }
    }//GEN-LAST:event_textFieldNewIntentKeyTyped

    private void textFieldNewIntentKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldNewIntentKeyPressed
        System.err.println("So far read-only. (textFieldNewIntentKeyPressed)");
//        switch (evt.getKeyCode()) {
//            case KeyEvent.VK_COLON:
//            case KeyEvent.VK_SEMICOLON:
//                evt.consume();
//                break;
//        }
    }//GEN-LAST:event_textFieldNewIntentKeyPressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelJavaElementFullName;
    private javax.swing.JList listIntents;
    private javax.swing.JPanel panelWithTags;
    private javax.swing.JTextField textFieldNewIntent;
    // End of variables declaration//GEN-END:variables
}
