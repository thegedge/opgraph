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
/**
 * 
 */
package ca.gedge.opgraph.nodes.math;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpinnerModel;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.edits.node.NodeSettingsEdit;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.gedge.opgraph.nodes.math.parser.MathExpressionEval;
import ca.gedge.opgraph.nodes.math.parser.MathExpressionLexer;
import ca.gedge.opgraph.nodes.math.parser.MathExpressionParser;

/**
 * A node that computes a value from a mathematical expression.
 */
@OpNodeInfo(
	name="Math Expression",
	description="Computes the value of a mathematical expression.",
	category="Math"
)
public class MathExpressionNode
	extends OpNode
	implements NodeSettings
{
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(MathExpressionNode.class.getName());
	
	/** Output field for the expression result */
	public final OutputField RESULT_OUTPUT_FIELD = new OutputField("result", "expression result", true, Number.class); 

	/** The math expression */
	private String expression;

	/** The expression parser that parsed the expression when it was set */
	private MathExpressionParser expressionParser;

	/** The parsed expression tree */
	private Object expressionTree;
	
	/** The number of decimal places that are significant in the expression result */
	private int significantDigits;
	
	/** The default number of decimal places that are significant the expression result */
	private static final int DEFAULT_SIGNIFICANT_DIGITS = -1;

	/**
	 * Constructs a math expression node with no expression.
	 */
	public MathExpressionNode() {
		this(null);
	}

	/**
	 * Constructs a math expression node with a given expression.
	 * 
	 * @param expression  the math expression
	 */
	public MathExpressionNode(String expression) {
		setExpression(expression);
		setSignificantDigits(DEFAULT_SIGNIFICANT_DIGITS);

		putField(RESULT_OUTPUT_FIELD);
		putExtension(NodeSettings.class, this);
	}

	/**
	 * Gets the math expression to evaluate.
	 * 
	 * @return the math expression
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Sets the math expression to evaluate.
	 * 
	 * @param expression  the math expression
	 */
	public void setExpression(String expression) {
		this.expression = (expression == null ? "" : expression);

		final ANTLRStringStream stream = new ANTLRStringStream(this.expression);
		final MathExpressionLexer lexer = new MathExpressionLexer(stream);
		final CommonTokenStream tokens = new CommonTokenStream(lexer);

		expressionParser = new MathExpressionParser(tokens);

		try {
			expressionTree = expressionParser.prog().getTree();
			if(expressionTree != null)
				LOGGER.info(((org.antlr.runtime.tree.CommonTree)expressionTree).toStringTree());

			// Remove any input fields that correspond to non-existant variables
			final ArrayList<InputField> inputFieldsCopy = new ArrayList<InputField>(getInputFields());
			for(InputField field : inputFieldsCopy) {
				if(!expressionParser.getVariables().contains(field.getKey()))
					removeField(field);
			}

			// Insert new input fields
			for(String variable : expressionParser.getVariables()) {
				if(getInputFieldWithKey(variable) == null)
					putField(new InputField(variable, "expression variable", false, true, Number.class));
			}
		} catch(RecognitionException exc) {
			expressionParser = null;
		}
	}

	/**
	 * Gets the number of decimal places that are significant in the expression
	 * result. If negative, all decimal places are significant. If zero, the
	 * result will always be an integer.
	 * 
	 * @return the number of significant digits
	 */
	public int getSignificantDigits() {
		return significantDigits;
	}

	/**
	 * Sets the number of decimal places that are significant in the expression
	 * result. If negative, all decimal places are significant. If set to zero,
	 * the result will always be an integer.
	 * 
	 * @param significantDigits  the number of significant digits.
	 */
	public void setSignificantDigits(int significantDigits) {
		this.significantDigits = significantDigits;
	}

	/**
	 * Rounds a double to a specified number of significant digits past the
	 * decimal place. Given a value <code>x</code>, the computed value
	 * <code>x'</code> will satisfy:
	 * <blockquote>
	 *   <code>Math.abs(x - x') < Math.pow(1, -significantDigits)</code>
	 * </blockquote>
	 * If the number of significant digits is negative then all decimal places
	 * are significant, and the result is returned as-is.
	 * 
	 * @param val  the value
	 * @param significantDigits  the number of significant digits
	 * 
	 * @return The value rounded to the given number of significant digits.
	 *         If the rounded value is an integer, an integral value is
	 *         returned, otherwise a decimal value is returned. 
	 */
	private static Number roundToSignificantDigits(double val, int significantDigits) {
		// If negative significant digits, return value as-is
		if(significantDigits < 0)
			return val;

		// If zero significant digits, just return the value rounded
		if(significantDigits == 0)
			return Math.round(val);

		// Take advantage of the rounding facilities of BigDecimal
		final BigDecimal bigValue = new BigDecimal(val);
		final BigDecimal scaledBigValue = bigValue.setScale(significantDigits, BigDecimal.ROUND_HALF_UP);

		// Try to get an integer out of it
		Number retVal = scaledBigValue;
		try {
			retVal = scaledBigValue.toBigIntegerExact();
		} catch(ArithmeticException exc) { }

		return retVal;
	}

	//
	// OpNode
	//

	@Override
	public void operate(OpContext context) throws ProcessingException {
		if(expressionParser == null || expressionTree == null)
			throw new NullPointerException("Math expression could not be parsed");

		//
		final CommonTreeNodeStream stream = new CommonTreeNodeStream(expressionTree);
		final MathExpressionEval expressionEval = new MathExpressionEval(stream);

		// Add variable bindings
		for(String variable : expressionParser.getVariables())
			expressionEval.putValue(variable, (Number)context.get(variable));

		// Evaluate, and round to the number of significant decimal places
		try {
			expressionEval.prog();
			
			final Number result = roundToSignificantDigits(expressionEval.getResult(), significantDigits);
			context.put(RESULT_OUTPUT_FIELD, result);
		} catch(RecognitionException exc) {
			throw new ProcessingException("Could not evaluate math expression", exc);
		}
	}

	//
	// NodeSettings
	//

	private static final String EXPRESSION_KEY = "expression";
	
	private static final String SIGNIFICANT_DIGITS_KEY = "significantDigits";

	/**
	 * A formatter that checks whether or not a given math expression is valid. 
	 */
	public static class MathExpressionFormatter extends AbstractFormatter {
		@Override
		public Object stringToValue(String text) throws ParseException {
			final ANTLRStringStream stream = new ANTLRStringStream(text);
			final MathExpressionLexer lexer = new MathExpressionLexer(stream);
			final CommonTokenStream tokens = new CommonTokenStream(lexer);
			final MathExpressionParser expressionParser = new MathExpressionParser(tokens);

			try {
				expressionParser.prog();
			} catch(RecognitionException exc) {
				setEditValid(false);
				invalidEdit();
				throw new ParseException(expressionParser.getErrorHeader(exc), 0);
			}

			if(expressionParser.getNumberOfSyntaxErrors() == 0) {
				setEditValid(true);
			} else {
				setEditValid(false);
				throw new ParseException("Could not parser expression: " + text, 0);
			}

			return text;
		}

		@Override
		public String valueToString(Object value) throws ParseException {
			return (value == null ? "" : value.toString());
		}
	}

	/**
	 * Constructs a math expression settings for the given node.
	 */
	public static class MathExpressionSettings extends JPanel {
		/**
		 * Constructs this component for a given math expression node .
		 * 
		 * @param node  the math expression node
		 */
		public MathExpressionSettings(final MathExpressionNode node) {
			super(new GridBagLayout());

			// A text field for the mathematical expression
			final JLabel expressionLabel = new JLabel("Expression: ");
			expressionLabel.setToolTipText("The mathematical expression (e.g., x+y)");
			
			final JFormattedTextField expressionText = new JFormattedTextField(new MathExpressionFormatter());
			expressionText.setValue(node.getExpression());
			expressionText.addPropertyChangeListener("value", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent e) {
					final GraphDocument document = GraphEditorModel.getActiveDocument();
					if(document != null) {
						final Properties settings = new Properties();
						settings.put(EXPRESSION_KEY, e.getNewValue().toString());
						document.getUndoSupport().postEdit(new NodeSettingsEdit(node, settings));
					}
				}
			});
			
			// An integer spinner for the number of significant digits in the result
			final JLabel significantDigitsLabel = new JLabel("Significant digits: ");
			significantDigitsLabel.setToolTipText("The number of significant decimal places to maintain in the result. If zero, the result will always be an integer. If negative, all decimal places are significant.");
			
			final SpinnerModel spinnerModel = new SpinnerNumberModel(node.getSignificantDigits(), -1, 100, 1);
			final JSpinner significantDigitsSpinner = new JSpinner(spinnerModel);
			significantDigitsSpinner.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					final GraphDocument document = GraphEditorModel.getActiveDocument();
					if(document != null) {
						final Properties settings = new Properties();
						settings.put(EXPRESSION_KEY, spinnerModel.getValue());
						document.getUndoSupport().postEdit(new NodeSettingsEdit(node, settings));
					}
				}
			});

			// Add components
			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 0;
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor = GridBagConstraints.EAST;
			add(expressionLabel, gbc);

			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.weightx = 1;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			add(expressionText, gbc);
			
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.weightx = 0;
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor = GridBagConstraints.EAST;
			add(significantDigitsLabel, gbc);

			gbc.gridx = 1;
			gbc.gridy = 1;
			gbc.weightx = 1;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			add(significantDigitsSpinner, gbc);
		}
	}

	//
	// NodeSettings
	//

	@Override
	public Component getComponent(GraphDocument document) {
		return new MathExpressionSettings(this);
	}

	@Override
	public Properties getSettings() {
		final Properties props = new Properties();
		props.setProperty(EXPRESSION_KEY, getExpression());
		props.setProperty(SIGNIFICANT_DIGITS_KEY, "" + getSignificantDigits());
		return props;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey(EXPRESSION_KEY))
			setExpression(properties.getProperty(EXPRESSION_KEY));
		
		if(properties.containsKey(SIGNIFICANT_DIGITS_KEY))
			setSignificantDigits(Integer.parseInt(properties.getProperty(SIGNIFICANT_DIGITS_KEY)));
	}
}
