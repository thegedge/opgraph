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
package ca.gedge.opgraph.nodes.general;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.edits.node.NodeSettingsEdit;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.gedge.opgraph.nodes.general.script.InputFields;
import ca.gedge.opgraph.nodes.general.script.LoggingHelper;
import ca.gedge.opgraph.nodes.general.script.OutputFields;

/**
 * A node that runs a script. 
 */
@OpNodeInfo(
	name="Script",
	description="Executes a script.",
	category="General"
)
public class ScriptNode 
	extends OpNode
	implements NodeSettings
{
	private static final Logger LOGGER = Logger.getLogger(ScriptNode.class.getName());

	/** The script engine manager being used */
	private ScriptEngineManager manager;

	/** The script engine being used */
	private ScriptEngine engine;

	/** The scripting language of this node */
	private String language;

	/** The script source */
	private String script;

	/**
	 * Constructs a script node that uses Javascript as its language.
	 */
	public ScriptNode() {
		this(null);
	}

	/**
	 * Constructs a script node that uses a given scripting language.
	 * 
	 * @param language  the name of the language
	 */
	public ScriptNode(String language) {
		this.manager = new ScriptEngineManager();
		this.script = "";
		setScriptLanguage(language);
		putExtension(NodeSettings.class, this);
	}

	/**
	 * Gets the scripting language of this node.
	 * 
	 * @return the name of the scripting language currently used
	 */
	public String getScriptLanguage() {
		return language;
	}

	/**
	 * Sets the scripting language of this node.
	 * 
	 * @param language  the name of a supported language
	 */
	public void setScriptLanguage(String language) {
		language = (language == null ? "" : language);
		if(!language.equals(this.language)) {
			this.language = language;
			this.engine = manager.getEngineByName(language);

			// Only work with invocable script engines
			if(this.engine == null || !(this.engine instanceof Invocable)) {
				this.engine = null;
			} else {
				this.engine.put("Logging", new LoggingHelper());
			}

			reloadFields();
		}
	}

	/**
	 * Gets the script source used in this node.
	 * 
	 * @return the script source
	 */
	public String getScriptSource() {
		return script;
	}

	/**
	 * Sets the script source used in this node.
	 * 
	 * @param script  the script source
	 */
	public void setScriptSource(String script) {
		script = (script == null ? "" : script);
		if(!script.equals(this.script)) {
			this.script = script;
			reloadFields();
		}
	}

	/**
	 * Reload the input/output fields from the script. 
	 */
	private void reloadFields() {
		if(engine != null) {
			try {
				engine.eval(script);

				// XXX Perhaps have InputFields and OutputFields store temporary
				//     sets of fields and only remove the input/output fields that
				//     don't exist in them (isntead of all fields)
				//
				removeAllInputFields();
				removeAllOutputFields();

				final InputFields inputFields = new InputFields(this);
				final OutputFields outputFields = new OutputFields(this);
				try {
					((Invocable)engine).invokeFunction("init", inputFields, outputFields);
				} catch(NoSuchMethodException exc) {
					// XXX init() not necessary, but should we warn?
				}
			} catch(ScriptException exc) {
				LOGGER.warning("Script error: " + exc.getLocalizedMessage());
			}
		}
	}

	//
	// Overrides
	//

	@Override
	public void operate(OpContext context) throws ProcessingException {
		if(engine != null) {
			try {
				// Creating bindings from context
				for(String key : context.keySet())
					engine.put(key, context.get(key));

				// provide logger for script as 'logger'
				Logger logger = Logger.getLogger(Processor.class.getName());
				engine.put("logger", logger);

				// Execute run() method in script
				((Invocable)engine).invokeFunction("run");

				// Put output values in context
				for(OutputField field : getOutputFields())
					context.put(field, engine.get(field.getKey()));

				// Erase values
				for(String key : context.keySet())
					engine.put(key, null);
			} catch(ScriptException exc) {
				throw new ProcessingException("Could not execute script script", exc);
			} catch(NoSuchMethodException exc) {
				throw new ProcessingException("No run() method in script", exc);
			}
		}
	}

	//
	// NodeSettings
	//

	/**
	 * Constructs a math expression settings for the given node.
	 */
	public static class ScriptNodeSettings extends JPanel {
		/**
		 * Constructs a component for editing a {@link ScriptNode}'s settings.
		 * 
		 * @param node  the {@link ScriptNode}
		 */
		public ScriptNodeSettings(final ScriptNode node) {
			super(new GridBagLayout());

			// Script source components
			final JEditorPane sourceEditor = new JEditorPane() {
				@Override
				public boolean getScrollableTracksViewportWidth() {
					// Only track width if the preferred with is less than the viewport width
					if(getParent() != null)
						return (getUI().getPreferredSize(this).width <= getParent().getSize().width);
					return super.getScrollableTracksViewportWidth();
				}

				@Override
				public Dimension getPreferredSize() {
					// Add a little for the cursor
					final Dimension dim = super.getPreferredSize();
					//dim.width += 5;
					return dim;
				}
			};

			sourceEditor.setText(node.getScriptSource());
			sourceEditor.setCaretPosition(0);

			sourceEditor.addCaretListener(new CaretListener() {
				@Override
				public void caretUpdate(CaretEvent e) {
					try {
						final Rectangle rect = sourceEditor.modelToView(e.getMark());
						if(rect != null) {
							rect.width += 5;
							rect.height += 5;
							sourceEditor.scrollRectToVisible(rect);
						}
					} catch(BadLocationException exc) {}
				}
			});

			sourceEditor.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					// Post an undoable edit
					final GraphDocument document = GraphEditorModel.getActiveDocument();
					if(document != null) {
						final Properties settings = new Properties();
						settings.put(SCRIPT_KEY, sourceEditor.getText());
						document.getUndoSupport().postEdit(new NodeSettingsEdit(node, settings));
					} else {
						node.setScriptSource(sourceEditor.getText());
					}
				}

				@Override
				public void focusGained(FocusEvent e) {}
			});

			final JScrollPane sourcePane = new JScrollPane(sourceEditor);
			sourcePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			sourcePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

			// Script language components
			final Vector<ScriptEngineFactory> factories = new Vector<ScriptEngineFactory>();
			final Vector<String> languageChoices = new Vector<String>();

			factories.add(null);
			languageChoices.add("<no language>");
			for(ScriptEngineFactory factory : (new ScriptEngineManager()).getEngineFactories()) {
				factories.add(factory);
				languageChoices.add(factory.getLanguageName());
			}

			final JComboBox languageBox = new JComboBox(languageChoices);
			languageBox.setEditable(false);
			languageBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Post an undoable edit
					final ScriptEngineFactory factory = factories.get(languageBox.getSelectedIndex());
					final GraphDocument document = GraphEditorModel.getActiveDocument();
					if(document != null) {
						final Properties settings = new Properties();
						settings.put(LANGUAGE_KEY, factory == null ? "" : factory.getLanguageName());
						document.getUndoSupport().postEdit(new NodeSettingsEdit(node, settings));
					} else {
						node.setScriptLanguage(factory == null ? null : factory.getLanguageName());
					}

					// Update editor kit
					final int ss = sourceEditor.getSelectionStart();
					final int se = sourceEditor.getSelectionEnd();
					final String source = sourceEditor.getText();

					// TODO editor kit with syntax highlighting
					sourceEditor.setContentType("text/plain");					
					sourceEditor.setText(source);
					sourceEditor.select(ss, se);
				}
			});

			languageBox.setSelectedItem(node.getScriptLanguage());

			// Add components
			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 0;
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor = GridBagConstraints.EAST;
			add(new JLabel("Script Language: "), gbc);

			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.weightx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.EAST;
			add(languageBox, gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = 2;
			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.fill = GridBagConstraints.BOTH;
			add(sourcePane, gbc);

			// Put the cursor at the beginning of the document
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					sourceEditor.select(0, 0);
				}
			});
		}
	}

	private static final String LANGUAGE_KEY = "scriptLanguage";
	private static final String SCRIPT_KEY = "scriptSource";

	@Override
	public Component getComponent(GraphDocument document) {
		return new ScriptNodeSettings(this);
	}

	@Override
	public Properties getSettings() {
		final Properties props = new Properties();
		props.setProperty(LANGUAGE_KEY, getScriptLanguage());
		props.setProperty(SCRIPT_KEY, getScriptSource());
		return props;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey(LANGUAGE_KEY))
			setScriptLanguage(properties.getProperty(LANGUAGE_KEY));

		if(properties.containsKey(SCRIPT_KEY))
			setScriptSource(properties.getProperty(SCRIPT_KEY));
	}
}
