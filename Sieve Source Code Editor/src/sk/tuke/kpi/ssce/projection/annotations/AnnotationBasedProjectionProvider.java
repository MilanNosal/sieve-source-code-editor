package sk.tuke.kpi.ssce.projection.annotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import org.netbeans.api.project.Project;
import org.openide.util.Exceptions;
import sk.tuke.kpi.ssce.concerns.annotations.AnnotationBasedConcern;
import sk.tuke.kpi.ssce.concerns.annotations.AnnotationBasedConcernExtractor;
import sk.tuke.kpi.ssce.concerns.interfaces.ConcernExtractor;
import sk.tuke.kpi.ssce.core.SSCEditorCore;
import sk.tuke.kpi.ssce.core.model.availableprojections.ProjectionsModel;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.interfaces.FoldingProvider;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.interfaces.GuardingProvider;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.providers.StandardFoldingProvider;
import sk.tuke.kpi.ssce.core.model.view.postprocessing.providers.StandardGuardingProvider;
import sk.tuke.kpi.ssce.projection.annotations.AnnotationBasedProjectionProvider.AnnotationsSelectionTableModel.TableRow;
import sk.tuke.kpi.ssce.projection.provider.ProjectionProvider;
import sk.tuke.kpi.ssce.sieving.annotations.AnnotationBasedSiever;
import sk.tuke.kpi.ssce.sieving.interfaces.PostProcessingSiever;

/**
 *
 * @author Milan
 */
public class AnnotationBasedProjectionProvider extends javax.swing.JPanel implements ProjectionProvider<AnnotationBasedConcern> {

    private SSCEditorCore<AnnotationBasedConcern> core;
    private final Project projectContext;
    private final AnnotationsSelectionTableModel tableModel = new AnnotationsSelectionTableModel();

    /**
     * Creates new form ABProjectionProvider
     */
    public AnnotationBasedProjectionProvider(Project projectContext) {
        this.projectContext = projectContext;
        startProjection();
        tableModel.setNewContent(core.getProjectionsModel().getAllConcerns());
        core.getProjectionsModel().addChangeListener(tableModel);
        initComponents();
        TableColumn sportColumn = annotationsTable.getColumnModel().getColumn(2);
        JComboBox comboBox = new JComboBox();
        comboBox.addItem("equals");
        comboBox.addItem("equalsIC");
        comboBox.addItem("startsWith");
        comboBox.addItem("endsWith");
        comboBox.addItem("contains");
        comboBox.addItem("matches");
        comboBox.addItem(">");
        comboBox.addItem(">=");
        comboBox.addItem("<");
        comboBox.addItem("<=");
        sportColumn.setCellEditor(new DefaultCellEditor(comboBox));
    }

    private void startProjection() {
        ConcernExtractor extractor = new AnnotationBasedConcernExtractor();
        AnnotationBasedSiever siever = new AnnotationBasedSiever();
        try {
            List<GuardingProvider> guards = new LinkedList<GuardingProvider>();
            guards.add(new StandardGuardingProvider());
            List<FoldingProvider> folds = new LinkedList<FoldingProvider>();
            folds.add(new StandardFoldingProvider());
            List<PostProcessingSiever<AnnotationBasedConcern>> postProcessors
                    = new LinkedList<PostProcessingSiever<AnnotationBasedConcern>>();
            core = new SSCEditorCore<AnnotationBasedConcern>(getProjectContext(),
                    extractor, siever, folds, guards, postProcessors);
            core.getCurrentProjection().addCurrentProjectionChangeListener(0, siever);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public SSCEditorCore<AnnotationBasedConcern> getSSCECore() {
        return core;
    }

    @Override
    public Project getProjectContext() {
        return this.projectContext;
    }

    @Override
    public JPanel getView() {
        return this;
    }

    @Override
    public void dispose() {
        core.dispose();
    }
    
    @Override
    public String getDisplayName() {
        return "Annotation-based projections";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        annotationsTable = new javax.swing.JTable();
        andCheckBox = new javax.swing.JCheckBox();
        sieveButton = new javax.swing.JButton();

        annotationsTable.setModel(tableModel);
        annotationsTable.getTableHeader().setReorderingAllowed(false);
        annotationsTable.getColumnModel().getColumn(0).setMinWidth(30);
        annotationsTable.getColumnModel().getColumn(0).setMaxWidth(30);

        annotationsTable.getColumnModel().getColumn(2).setMinWidth(80);
        annotationsTable.getColumnModel().getColumn(2).setMaxWidth(80);
        jScrollPane1.setViewportView(annotationsTable);

        org.openide.awt.Mnemonics.setLocalizedText(andCheckBox, org.openide.util.NbBundle.getMessage(AnnotationBasedProjectionProvider.class, "AnnotationBasedProjectionProvider.andCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(sieveButton, org.openide.util.NbBundle.getMessage(AnnotationBasedProjectionProvider.class, "AnnotationBasedProjectionProvider.sieveButton.text")); // NOI18N
        sieveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sieveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(andCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sieveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sieveButton)
                    .addComponent(andCheckBox))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sieveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sieveButtonActionPerformed
        Set<AnnotationBasedConcern> selectedConcerns = new HashSet<AnnotationBasedConcern>();
        Map<String, Object> params = new HashMap<String, Object>();

        for (TableRow row : this.tableModel.getTableRows()) {
            if (row.use) {
                selectedConcerns.add(row.source);
                if (row.parameter && row.operation != null) {
                    if (!params.containsKey(row.source.getUniquePresentation())) {
                        params.put(row.source.getUniquePresentation(), new LinkedList<AnnotationBasedSiever.Parameter>());
                    }
                    List<AnnotationBasedSiever.Parameter> parameters
                            = (List<AnnotationBasedSiever.Parameter>) params.get(row.source.getUniquePresentation());
                    parameters.add(new AnnotationBasedSiever.Parameter(
                            row.id,
                            row.operation,
                            row.value
                    ));
                }
            }
        }

        params.put("mode", andCheckBox.isSelected() ? "AND" : "OR");
        core.getCurrentProjection().setSelectedConcerns(selectedConcerns, params);
    }//GEN-LAST:event_sieveButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox andCheckBox;
    private javax.swing.JTable annotationsTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton sieveButton;
    // End of variables declaration//GEN-END:variables

    protected static class AnnotationsSelectionTableModel extends AbstractTableModel
            implements ProjectionsModel.ConcernsChangeListener<AnnotationBasedConcern> {

        private final String[] columnNames = {"use", "name", "operation", "value"};
        private final List<TableRow> rowsData = new ArrayList<TableRow>();

        @Override
        public void concernsChanged(ProjectionsModel.ConcernsChangedEvent<AnnotationBasedConcern> event) {
            if (event.isConcernsSetChanged()) {
                setNewContent(event.getAllConcerns());
            }
        }

        public void setNewContent(Set<AnnotationBasedConcern> availableConcerns) {
            rowsData.clear();
            for (AnnotationBasedConcern concern : availableConcerns) {
                String prefix = concern.getUniquePresentation();
                rowsData.add(new TableRow(
                        concern, prefix, prefix,
                        null, "", false));
                for (Element element : concern.getAnnotationType().asElement().getEnclosedElements()) {
                    rowsData.add(
                            new TableRow(
                                    concern,
                                    prefix + "." + element.getSimpleName(),
                                    element.getSimpleName().toString(),
                                    AnnotationBasedSiever.OPERATION.EQUALS,
                                    "", true));
                }
            }
            fireTableStructureChanged();
        }

        public synchronized List<TableRow> getTableRows() {
            return new LinkedList<TableRow>(this.rowsData);
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getRowCount() {
            return rowsData.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            TableRow data = this.rowsData.get(row);
            switch (col) {
                case 0:
                    return data.use;
                case 1:
                    return data.name;
                case 2:
                    return data.operation == null ? "" : data.operation;
                case 3:
                    return data.value;
                default:
                    throw new RuntimeException("too many columns for our simple table");
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case 0:
                    return true;
                case 1:
                    return false;
                default:
                    return this.rowsData.get(row).parameter;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            TableRow data = this.rowsData.get(row);
            switch (col) {
                case 0:
                    data.use = (Boolean) value;
                    break;
                case 2:
                    String string = (String) value;
                    data.operation = AnnotationBasedSiever.OPERATION.getValueOf(string);
                    break;
                case 3:
                    data.value = (String) value;
                    break;
                default:
                    throw new RuntimeException("How could anyone edit something uneditable?");
            }
            fireTableCellUpdated(row, col);
        }

        protected static class TableRow {

            private boolean parameter;
            private boolean use = false;
            private String name;
            private AnnotationBasedSiever.OPERATION operation;
            private String value;
            private final String id;
            private final AnnotationBasedConcern source;

            public TableRow(AnnotationBasedConcern source, String id, String name,
                    AnnotationBasedSiever.OPERATION operation, String value, boolean parameter) {
                this.name = name;
                this.operation = operation;
                this.value = value;
                this.parameter = parameter;
                this.id = id;
                this.source = source;
            }
        }
    }
}
