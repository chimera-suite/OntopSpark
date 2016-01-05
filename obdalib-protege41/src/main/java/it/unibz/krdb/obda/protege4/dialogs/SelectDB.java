package it.unibz.krdb.obda.protege4.dialogs;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;

import java.util.Iterator;

import javax.swing.JFrame;

/*
 * SelectDB.java
 * 
 * Created on 27-set-2010, 9.41.00
 */

/**
 * This is a dialog for select a Database as a target for an ABox dump
 * 
 * @author obda
 */
public class SelectDB extends javax.swing.JDialog {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -1787461016329735072L;
	private OBDAModel		apic				= null;
	private SetParametersDialog	dialog				= null;

	String						selectedSource		= null;

	// private ABoxToDBDumper dumper = null;
	// private Set<OWLOntology> ontologies = null;

	/** Creates new form SelectDB */
	public SelectDB(java.awt.Frame parent, boolean modal, OBDAModel apic) {
		super(parent, modal);
		this.apic = apic;
		// this.dumper = dumper;
		// this.ontologies = ontologies;
		initComponents();
		addListener();
		// apic.addDatasourceControllerListener(this);
		addExistingDataSourcesToCombo();
	}

	/***
	 * Indicates if the user wanted to eliminated any existing ABox database
	 * before making the dump.
	 * 
	 * @return
	 */
	public boolean isOverrideSelected() {
		return jCheckBoxOverride.isSelected();
	}

	private void addExistingDataSourcesToCombo() {

		Iterator<OBDADataSource> it = apic.getSources().iterator();
		while (it.hasNext()) {
			jComboBox1.addItem(it.next().getSourceID());
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		jPanel1 = new javax.swing.JPanel();
		jButtonOK = new javax.swing.JButton();
		jButtonNew = new javax.swing.JButton();
		jComboBox1 = new javax.swing.JComboBox();
		jLabel1 = new javax.swing.JLabel();
		jCheckBoxOverride = new javax.swing.JCheckBox();
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Select Data Source");
		getContentPane().setLayout(new java.awt.GridBagLayout());

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jButtonOK.setText("OK");
		jButtonOK.setMaximumSize(new java.awt.Dimension(50, 25));
		jButtonOK.setMinimumSize(new java.awt.Dimension(50, 25));
		jButtonOK.setPreferredSize(new java.awt.Dimension(50, 25));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		jPanel1.add(jButtonOK, gridBagConstraints);

		jButtonNew.setText("New...");
		jButtonNew.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonNewActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		jPanel1.add(jButtonNew, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(4, 0, 15, 0);
		jPanel1.add(jComboBox1, gridBagConstraints);

		jLabel1.setText("Select the data source where to dump the ABox:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(8, 1, 15, 1);
		jPanel1.add(jLabel1, gridBagConstraints);

		jCheckBoxOverride.setText("Override existing ABox drop");
		jCheckBoxOverride.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxOverrideActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(4, 0, 15, 0);
		jPanel1.add(jCheckBoxOverride, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(15, 15, 15, 15);
		getContentPane().add(jPanel1, gridBagConstraints);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(jLabel2, gridBagConstraints);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.weightx = 1.0;
		getContentPane().add(jLabel3, gridBagConstraints);

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void addListener() {
		jButtonOK.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton3ActionPerformed(evt);
			}
		});
	}

	private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
		this.setVisible(false);
		selectedSource = jComboBox1.getSelectedItem().toString();
	}

	public String getSelectedSource() {
		return selectedSource;
	}

	private void jButtonNewActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonNewActionPerformed

		dialog = new SetParametersDialog(new JFrame(), true, apic);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);

	}// GEN-LAST:event_jButtonNewActionPerformed

	private void jCheckBoxOverrideActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jCheckBoxOverrideActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_jCheckBoxOverrideActionPerformed

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton		jButtonNew;
	private javax.swing.JButton		jButtonOK;
	private javax.swing.JCheckBox	jCheckBoxOverride;
	private javax.swing.JComboBox	jComboBox1;
	private javax.swing.JLabel		jLabel1;
	private javax.swing.JLabel		jLabel2;
	private javax.swing.JLabel		jLabel3;
	private javax.swing.JPanel		jPanel1;

	// End of variables declaration//GEN-END:variables

	// @Override
	// public void alldatasourcesDeleted() {
	// if(isVisible()){
	// addExistingDataSourcesToCombo();
	// }
	//		
	// }

	// @Override
	// public void currentDatasourceChange(DataSource previousdatasource,
	// DataSource currentsource) {
	// if(isVisible()){
	// addExistingDataSourcesToCombo();
	// }
	//		
	// }

	// @Override
	// public void datasourceParametersUpdated() {
	// if(isVisible()){
	// addExistingDataSourcesToCombo();
	// }
	//		
	// }
	//
	// @Override
	// public void datasourceAdded(DataSource source) {
	// if(isVisible()){
	// addExistingDataSourcesToCombo();
	// }
	//		
	// }
	//
	// @Override
	// public void datasourceDeleted(DataSource source) {
	// if(isVisible()){
	// addExistingDataSourcesToCombo();
	// }
	//		
	// }
	//
	// @Override
	// public void datasourceUpdated(String oldname, DataSource currendata) {
	// if(isVisible()){
	// addExistingDataSourcesToCombo();
	// }
	//		
	// }

	public void dispose() {
		// apic.removeDatasourceControllerListener(this);
		super.dispose();
	}
}
