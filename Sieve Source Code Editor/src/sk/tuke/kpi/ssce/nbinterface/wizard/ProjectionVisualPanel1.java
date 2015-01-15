package sk.tuke.kpi.ssce.nbinterface.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import org.openide.util.Lookup;
import sk.tuke.kpi.ssce.annotations.concerns.SSCE_UI;
import sk.tuke.kpi.ssce.projection.provider.ProjectionProviderFactory;

@SSCE_UI
public final class ProjectionVisualPanel1 extends JPanel {

    private final Collection<? extends ProjectionProviderFactory> availableImplementations;
    
    /**
     * Creates new form ProjectionVisualPanel1
     */
    public ProjectionVisualPanel1() {
        Lookup lookup = Lookup.getDefault();
        availableImplementations = lookup.lookupAll(ProjectionProviderFactory.class);
        initComponents();
    }

    @Override
    public String getName() {
        return "Implementation selection";
    }
    
    public ProjectionProviderFactory getSelectedFactory() {
        String selectedImplementation = (String) availableImplementationsCombo.getSelectedItem();
        ProjectionProviderFactory selectedFactory = null;
        for (ProjectionProviderFactory factory : availableImplementations) {
            if (factory.getDisplayName().equals(selectedImplementation)) {
                selectedFactory = factory;
            }
        }
        return selectedFactory;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        availableImplementationsCombo = new javax.swing.JComboBox();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ProjectionVisualPanel1.class, "ProjectionVisualPanel1.jLabel2.text")); // NOI18N

        List<String> names = new ArrayList<String>();
        for (ProjectionProviderFactory projectionProvider : availableImplementations) {
            names.add(projectionProvider.getDisplayName());
        }
        availableImplementationsCombo.setModel(new javax.swing.DefaultComboBoxModel(names.toArray(new String[names.size()])));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(availableImplementationsCombo, 0, 249, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(availableImplementationsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox availableImplementationsCombo;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables
}