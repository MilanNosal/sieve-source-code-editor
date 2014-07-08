package sk.tuke.kpi.ssce.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import sk.tuke.kpi.ssce.core.SSCEditorCore;
import sk.tuke.kpi.ssce.core.configuration.CurrentProjection;
import sk.tuke.kpi.ssce.core.model.projections.IntentsMapping;
import sk.tuke.kpi.ssce.core.model.projections.IntentsMapping.IntentsChangedEvent;
import sk.tuke.kpi.ssce.concerns.annotations.AnnotationSearchable;
import sk.tuke.kpi.ssce.concerns.interfaces.Searchable;

/**
 *
 * @author Matej Nosal, Milan Nosal
 */
//SsceIntent:Komponent grafickeho rozhrania;
public class SsceIntentFilterPanel extends javax.swing.JPanel implements IntentsMapping.IntentsChangeListener, ActionListener, ListSelectionListener {

    private SSCEditorCore core = null;

    /**
     * Creates new form SsceIntentFilterPanel
     */
    //SsceIntent:Komponent grafickeho rozhrania;Dopyt na zdrojovy kod, konfiguracia zamerov;
    public SsceIntentFilterPanel() {
        initComponents();

        jComboBoxMode.setModel(new DefaultComboBoxModel(new Object[]{CurrentProjection.MODE_AND, CurrentProjection.MODE_OR}));
//        jListIntents.setSelectionModel(new DefaultListSelectionModel() {
//
//            @Override
//            public void setSelectionInterval(int index0, int index1) {
//                if (super.isSelectedIndex(index0)) {
//                    super.removeSelectionInterval(index0, index1);
//                } else {
//                    super.addSelectionInterval(index0, index1);
//                }
//            }
//        });


    }

    /**
     * Nastavi sa nove jadro editora modulu SSCE.
     * @param core nove jadro editora modulu SSCE.
     */
    //SsceIntent:Aktualizacia grafickeho rozhrania;
    public void setSSCEditorCore(SSCEditorCore core) {
        if (this.core != null) {
            if (this.core.equals(core)) {
                //TODO: co treba: actualize
                return;
            } else {
                removeSSCEditorCore();
            }
        }
        if (core == null) {
            return;
        }

        this.core = core;

        refreshdModel();
        refreshdMode();

//        DefaultListModel model = new DefaultListModel();
//        List<String> intents = new ArrayList<String>(this.core.getIntentsMapping().getAllIntents());
//        Collections.sort(intents);
//        for (String intent : intents) {
//            model.addElement(intent);
//        }
//        this.jListIntents.setModel(model);

//        for(int i=0;i<model.size();i++){
//            if(core.getConfiguration().INTENTS.getSelectedIntents().contains(
//                model.get(i))) jListIntents.sets
//        }

//        for (String intentToSelect : core.getConfiguration().INTENTS.getSelectedIntents()) {
//            jListIntents.setSelectedValue(intentToSelect, false);
//        }

//        core.getConfiguration().INTENTS.getSelectedIntents()




        core.getIntentsMapping().addChangeListener(this);

    }

    /**
     * Odstrani sa aktualne jadro editora modulu SSCE.
     */
    //SsceIntent:Aktualizacia grafickeho rozhrania;
    public void removeSSCEditorCore() {
        if (core == null) {
            return;
        }

        this.jListIntents.removeListSelectionListener(this);
        this.jComboBoxMode.removeActionListener(this);

        core.getIntentsMapping().removeChangeListener(this);
        core = null;

    }

    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;
    private void refreshdModel() {
        if (this.core == null) {
            return;
        }

        this.jListIntents.removeListSelectionListener(this);

        DefaultListModel model = new DefaultListModel();
        List<Searchable> intents = new ArrayList<Searchable>(this.core.getIntentsMapping().getAllIntents());
        Collections.sort(intents);
        model.addElement(new AnnotationSearchable(null));
        for (Searchable intent : intents) {
            model.addElement(intent);
        }

        this.jListIntents.setModel(model);

        Set<Searchable> selectedIntents = core.getConfiguration().getCurrentlySelectedConcerns();
        List<Integer> indices = new ArrayList<Integer>();

        for (int i = 0; i < model.size(); i++) {
            for(Searchable intent : selectedIntents) {
                if(intent.equals(model.get(i))) { // TODO: java.lang.ClassCastException: sk.tuke.kpi.nosal.matej.ssce.core.configuration.IntentsConfiguration$1 cannot be cast to java.lang.String
                    indices.add(i);
                }
            }
        }
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indicesArray.length; i++) {
            indicesArray[i] = indices.get(i);
        }

        jListIntents.setSelectedIndices(indicesArray);

//        for (String intentToSelect : core.getConfiguration().INTENTS.getSelectedIntents()) {
//            jListIntents.setSelectedValue(intentToSelect, false);
//        }
        //System.out.println("actual selection refreshing :  " + Arrays.toString(core.getConfiguration().getSelectedIntents().toArray()));


        this.jListIntents.addListSelectionListener(this);

    }

    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;
    private void refreshdMode() {
        if (this.core == null) {
            return;
        }

        this.jComboBoxMode.removeActionListener(this);

        this.jComboBoxMode.setSelectedItem(core.getConfiguration().getMode());


        this.jComboBoxMode.addActionListener(this);

    }

    /**
     * Spracuje zmeny v selekcii listu, ktory obsahuje zamery. Zrealizuje vytvorenie dopytu a aktualizuje konfiguraciu zamerov cim sa odstartuje projekcia kodu.
     * @param event event.
     */
    //This is for listening selection of listIntents
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;Notifikacia zmeny dopytu na zdrojovy kod;
    @Override
    public void valueChanged(ListSelectionEvent event) {
        if (this.core == null) {
            return;
        }
        Object[] objects = jListIntents.getSelectedValues();
        Searchable[] intents = Arrays.copyOf(objects, objects.length, Searchable[].class); // TODO: zrejme problem s java.lang.ArrayStoreException, s declared type dummy
        //List<String> intentsList = Arrays.asList(intents);
        // TODO: spravit to aby JList pracoval priamo s declared (resp. s wrapper)
        //List<Searchable> allIntents = new ArrayList<Searchable>(this.core.getIntentsMapping().getAllIntents());
        Set<Searchable> selectedIntents = new HashSet<Searchable>();
        selectedIntents.addAll(Arrays.asList(intents));

        this.core.getConfiguration().setSelectedIntents(selectedIntents);


        //System.out.println("actual selection :  " + Arrays.toString(objects));
    }

    /**
     * Aktualizuje zoznam zamerov na zaklade zmeny v mapovani zamerov.
     * @param event event.
     */
    //This is for IntentsMapping
    //SsceIntent:Notifikacia na zmeny v priradenych zamerov;
    @Override
    public void intentsChanged(IntentsChangedEvent event) {
//            System.out.println("INTENTS CHANGED       ScceIntentFilterPanel   event.isIntentsSetChanged()="+event.isIntentsSetChanged());
//        if (event.isIntentsSetChanged()) {
        refreshdModel();
//        }
    }

    /**
     * Spracuje zmenu modu a aktualizuje mod konfiguracie zamerov.
     * @param e event.
     */
    //This si for ComboBoxMode
    //SsceIntent:Dopyt na zdrojovy kod, konfiguracia zamerov;Notifikacia zmeny dopytu na zdrojovy kod;
    @Override
    public void actionPerformed(ActionEvent e) {
        if (this.core == null) {
            return;
        }
        if (CurrentProjection.MODE_AND.equals(jComboBoxMode.getSelectedItem())) {
            this.core.getConfiguration().setMode(CurrentProjection.MODE_AND);
        } else if (CurrentProjection.MODE_OR.equals(jComboBoxMode.getSelectedItem())) {
            this.core.getConfiguration().setMode(CurrentProjection.MODE_OR);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jListIntents = new javax.swing.JList();
        jLabelMode = new javax.swing.JLabel();
        jLabelTags = new javax.swing.JLabel();
        jComboBoxMode = new javax.swing.JComboBox();

        jListIntents.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jListIntents);

        jLabelMode.setText(org.openide.util.NbBundle.getMessage(SsceIntentFilterPanel.class, "SsceIntentFilterPanel.jLabelMode.text")); // NOI18N

        jLabelTags.setText(org.openide.util.NbBundle.getMessage(SsceIntentFilterPanel.class, "SsceIntentFilterPanel.jLabelIntents.text")); // NOI18N

        jComboBoxMode.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "And", "Or" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabelTags)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 428, Short.MAX_VALUE)
                        .addComponent(jLabelMode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBoxMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelMode))
                    .addComponent(jLabelTags, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxMode;
    private javax.swing.JLabel jLabelMode;
    private javax.swing.JLabel jLabelTags;
    private javax.swing.JList jListIntents;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
