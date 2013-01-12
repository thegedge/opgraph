/*
 * Copyright (C) 2012 Jason Gedge <http://www.gedge.ca>
 *
 * This file is part of the OpGraph project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.gedge.opgraph.app.components;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.extensions.NodeMetadata;
import ca.gedge.opgraph.validators.ClassValidator;

/**
 * A panel for displaying and editing default values for a node's input fields.
 * 
 * TODO undoable edits for defaults
 */
public class NodeDefaultsPanel extends JPanel {
	/** Logger */
	private final Logger LOGGER = Logger.getLogger(NodeDefaultsPanel.class.getName());

	/** The node currently being viewed */
	private OpNode node;

	/**
	 * Default constructor.
	 */
	public NodeDefaultsPanel() {
		super(new GridBagLayout());
		setNode(null);
	}

	/**
	 * Gets the node this info panel is currently viewing.
	 * 
	 * @return the node
	 */
	public OpNode getNode() {
		return node;
	}

	/**
	 * Sets the node this info panel is currently viewing.
	 * 
	 * @param node  the node to display
	 */
	public void setNode(OpNode node) {
		if(this.node != node || getComponentCount() == 0) {
			this.node = node;

			// Clear all current components and add in new ones
			removeAll();
			if(node == null) {
				final GridBagConstraints gbc = new GridBagConstraints();
				final JLabel label = new JLabel("No node selected", SwingConstants.CENTER);
				label.setFont(label.getFont().deriveFont(Font.ITALIC));
				add(label, gbc);
			} else {
				final GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridy = 0;

				final NodeMetadata meta = node.getExtension(NodeMetadata.class);
				if(meta != null) {
					gbc.insets.set(2, 5, 2, 2);
					gbc.gridwidth = 1;

					for(InputField field : node.getInputFields()) {
						final Object value = meta.getDefault(field);
						final JComponent editComp = getEditComponentForField(field, value);
						if(editComp != null) {
							final JLabel fieldNameLabel = new JLabel(field.getKey() + ":");
							fieldNameLabel.setFont(fieldNameLabel.getFont().deriveFont(Font.BOLD));
							fieldNameLabel.setToolTipText(field.getDescription());

							gbc.gridx = 0;
							gbc.anchor = GridBagConstraints.EAST;
							gbc.fill = GridBagConstraints.NONE;
							gbc.weightx = 0;
							add(fieldNameLabel, gbc);

							editComp.setToolTipText(field.getDescription());

							gbc.gridx = 1;
							gbc.anchor = GridBagConstraints.WEST;
							gbc.fill = GridBagConstraints.BOTH;
							gbc.weightx = 1;
							add(editComp, gbc);

							++gbc.gridy;
						}
					}

					if(getComponentCount() > 0) {
						gbc.weighty = 1;
						gbc.gridwidth = 2;
						add(new JComponent() {}, gbc);
					} else {
						final JLabel label = new JLabel("No editable input fields", SwingConstants.CENTER);
						label.setFont(label.getFont().deriveFont(Font.ITALIC));
						add(label, gbc);
					}
				} else {
					final JLabel label = new JLabel("Node does not support defaults", SwingConstants.CENTER);
					label.setFont(label.getFont().deriveFont(Font.ITALIC));
					add(label, gbc);
				}
			}

			revalidate();
			repaint();
		}
	}

	/**
	 * Gets an editing component for an input field.
	 *  
	 * @param field  the input field
	 * 
	 * @return the editing component for the specified input field, or
	 *         <code>null</code> if there is no editable component for
	 *         the given input field
	 *         
	 * TODO More user customization on editors for different types/validators.
	 *      Perhaps have an interface with a function similar to below.
	 */
	private JComponent getEditComponentForField(final InputField field, Object defaultValue) {
		// XXX allow enabled field here?
		if(field.getKey() == OpNode.ENABLED_FIELD.getKey())
			return null;

		// Editable component returned is currently based on a fixed set of
		// classes, and only if the validator of the field is a class-based
		// validator which accepts a single class
		JComponent ret = null;
		if(field.getValidator() != null && field.getValidator() instanceof ClassValidator) {
			final ClassValidator validator = (ClassValidator)field.getValidator();
			if(validator.getClasses().size() == 1) {
				Class<?> cls = validator.getClasses().get(0);

				// Check default value against this class
				if(defaultValue != null) {
					if(!cls.isAssignableFrom(defaultValue.getClass())) {
						LOGGER.warning("Default value for input field '" + field.getKey() +"' should be '" + cls.getName() + "' but got '" + defaultValue.getClass().getName() + "' instead");
						defaultValue = null;
					}
				}

				if(cls == String.class) {
					// JTextArea for java.lang.String
					String initialText = "";
					if(defaultValue != null)
						initialText = (String)defaultValue;

					final JTextArea stringEditable = new JTextArea(initialText);
					stringEditable.setBorder(BorderFactory.createEtchedBorder());
					stringEditable.setLineWrap(true);
					stringEditable.setTabSize(4);
					stringEditable.getDocument().addDocumentListener(new DocumentListener() {
						@Override
						public void removeUpdate(DocumentEvent e) {
							if(node != null) {
								final NodeMetadata meta = node.getExtension(NodeMetadata.class);
								if(meta != null)
									meta.setDefault(field, stringEditable.getText());
							}
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							if(node != null) {
								final NodeMetadata meta = node.getExtension(NodeMetadata.class);
								if(meta != null)
									meta.setDefault(field, stringEditable.getText());
							}
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							if(node != null) {
								final NodeMetadata meta = node.getExtension(NodeMetadata.class);
								if(meta != null)
									meta.setDefault(field, stringEditable.getText());
							}
						}
					});

					ret = stringEditable;
				} else if(cls == Boolean.class) {
					// JTextBox for java.lang.Boolean
					boolean initial = false;
					if(defaultValue != null)
						initial = (Boolean)defaultValue;

					final JCheckBox booleanEditable = new JCheckBox();
					booleanEditable.setSelected(initial);
					booleanEditable.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							if(node != null) {
								final NodeMetadata meta = node.getExtension(NodeMetadata.class);
								if(meta != null)
									meta.setDefault(field, e.getStateChange() == ItemEvent.SELECTED);
							}
						}
					});

					ret = booleanEditable;
				} else if(Number.class.isAssignableFrom(cls)) {
					Number initial = null;
					if(defaultValue != null)
						initial = (Number)defaultValue;

					final NumberFormat formatter = NumberFormat.getInstance();
					final JFormattedTextField numberEditable = new JFormattedTextField(formatter);
					numberEditable.setValue(initial);
					numberEditable.addPropertyChangeListener(new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent e) {
							if(node != null && e.getPropertyName().equals("value")) {
								final NodeMetadata meta = node.getExtension(NodeMetadata.class);
								if(meta != null)
									meta.setDefault(field, e.getNewValue());
							}
						}
					});

					ret = numberEditable;
				}
			}
		}

		return ret;
	}
}
