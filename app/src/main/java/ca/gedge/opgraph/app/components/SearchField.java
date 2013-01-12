/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 * A search field with optional context button. The field displays a prompt
 * when the text field text is empty.
 */
public class SearchField extends JTextField {
	/** Search icon size */
	private static final int ICON_SIZE = 16;

	/** Popup menu used for context item */
	private JPopupMenu contextPopup;

	/**
	 * Text field state
	 */
	public static enum FieldState {
		/** Undefined state */
		UNDEFINED(Color.RED),

		/** Prompt (no input) */
		PROMPT(Color.GRAY),

		/** Regular input state */
		INPUT(SystemColor.textText);

		private Color color;

		FieldState(Color color) {
			this.color = color;
		}

		/**
		 * Gets the color used for this state.
		 * 
		 * @return the color
		 */
		public Color getColor() {
			return color;
		}
	};

	private final static String STATE_PROPERTY = "_search_field_state_";

	/**
	 * Current state
	 */
	private FieldState fieldState = FieldState.UNDEFINED;

	/**
	 * Search field prompt
	 */
	private String prompt;

	/**
	 * Search context button
	 */
	private SearchFieldButton ctxButton;

	private SearchFieldButton endButton;

	/**
	 * Constructs a search field with a default prompt.
	 */
	public SearchField() {
		this("Search");
	}

	/**
	 * Constructs a search field with a specified prompt.
	 * 
	 * @param prompt  the prompt
	 */
	public SearchField(String prompt) {
		init();
		this.prompt = prompt;
		setState(FieldState.PROMPT);
		addFocusListener(focusStateListener);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension retVal = super.getPreferredSize();
		retVal.height = Math.max(retVal.height, 25);
		return retVal;
	}

	private BufferedImage clearIcn = null;
	private BufferedImage createClearIcon() {
		if(clearIcn == null) {
			clearIcn = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = (Graphics2D)clearIcn.getGraphics();

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			Ellipse2D circle = new Ellipse2D.Float(2, 2, ICON_SIZE - 2, ICON_SIZE - 2);
			g2d.setColor(FieldState.PROMPT.getColor());
			g2d.fill(circle);

			Stroke s = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			g2d.setStroke(s);

			g2d.setColor(Color.WHITE);
			g2d.drawLine(6, 6, ICON_SIZE - 5, ICON_SIZE - 5);
			g2d.drawLine(ICON_SIZE - 5, 6, 6, ICON_SIZE - 5);
		}
		return clearIcn;
	}

	private BufferedImage searchIcn = null;
	private BufferedImage createSearchIcon() {
		if(searchIcn == null) {
		BufferedImage retVal = new BufferedImage(ICON_SIZE + 8, ICON_SIZE,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D)retVal.getGraphics();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Ellipse2D circle = new Ellipse2D.Float(2, 2, 
				10, 10);
		Line2D stem = new Line2D.Float(11, 11, ICON_SIZE - 2, ICON_SIZE - 2);

		Polygon tri = new Polygon();
		tri.addPoint(16, 8);
		tri.addPoint(24, 8);
		tri.addPoint(20, 12);

//		Line2D triA = new Line2D.Float(14.0f, 9.0f, 17.0f, 9.0f);
//		Line2D triB = new Line2D.Float(17.0f, 9.0f, 15.5f, 11.0f);
//		Line2D triC = new Line2D.Float(15.5f, 11.0f, 14.0f, 9.0f);

		Stroke s = new BasicStroke(2.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		g2d.setStroke(s);
		g2d.setColor(FieldState.PROMPT.getColor());

		g2d.draw(circle);
		g2d.draw(stem);

		g2d.fillPolygon(tri);

//		s = new BasicStroke(0.5f);
//		g2d.setStroke(s);
//		
//		g2d.draw(triA);
//		g2d.draw(triB);
//		g2d.draw(triC);
		searchIcn = retVal;
		}
		return searchIcn;
	}

	private void init() {
		// load search icon
		searchIcn =  createSearchIcon();

		final int borderInset = 10;
		ctxButton = new SearchFieldButton(SwingConstants.LEFT, createSearchIcon());
		ctxButton.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(contextPopup != null) {
					final int x = ctxButton.getX();
					final int y = ctxButton.getY() + ctxButton.getHeight();
					contextPopup.show(SearchField.this, x, y);
				}
			}
		});
		ctxButton.setCursor(Cursor.getDefaultCursor());

		super.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				ctxButton.setBounds(0, 0, searchIcn.getWidth() + borderInset, getHeight());
				endButton.setBounds(getWidth() - (ICON_SIZE + borderInset), 0, ICON_SIZE + borderInset, getHeight());
			}
		});
		add(ctxButton);

		endButton = new SearchFieldButton(SwingConstants.RIGHT, null);
		endButton.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setText("");
			}
		});
		endButton.setCursor(Cursor.getDefaultCursor());
		add(endButton);

		// setup an empty border allowing for the
		// extra space needed for drawing
		final int leftSpace = searchIcn.getWidth() + borderInset;
		final int rightSpace = ICON_SIZE + borderInset;
		final int topSpace = 0;
		final int btmSpace = 0;

		final Border emptyBorder = BorderFactory.createEmptyBorder(topSpace, leftSpace, btmSpace, rightSpace);
		final Border matteBorder = BorderFactory.createMatteBorder(1, 0, 1, 0, FieldState.PROMPT.getColor());

		setBorder(BorderFactory.createCompoundBorder(emptyBorder, matteBorder));
		setBackground(Color.WHITE);
		setOpaque(false);
	}

	/**
	 * Sets the popup menu used for the context button.
	 * 
	 * @param contextPopup  the popup menu, or <code>null</code> if no popup
	 *                      should be used
	 */
	public void setContextPopup(JPopupMenu contextPopup) {
		this.contextPopup = contextPopup;
	}

	/**
	 * Gets the popup menu used for the context button.
	 * 
	 * @return the popup menu, or <code>null</code> if one is not set
	 */
	public JPopupMenu getContextPopup() {
		return contextPopup;
	}

	@Override
	public String getText() {
		String retVal = super.getText();
		if(this.fieldState == FieldState.PROMPT) {
			retVal = "";
		}
		return retVal;
	}

	@Override
	public void setText(String s) {
		if(s == null) s = "";

		super.setText(s);
		if(hasFocus() && s.length() > 0) {
			setState(FieldState.INPUT);
		} else if(s.length() == 0) {
			setState(FieldState.PROMPT);
		}
	}

	/**
	 * Gets the prompt text for the search field.
	 * 
	 * @return the prompt text
	 */
	public String getPrompt() {
		return prompt;
	}

	/**
	 * Sets the prompt text for the search field.
	 * 
	 * @param prompt  the prompt text
	 */
	public void setPrompt(String prompt) {
		this.prompt = prompt;
		if(getState() == FieldState.PROMPT)
			super.setText(prompt);
	}

	/**
	 * Set state of field
	 * 
	 * @param state  the new state for this field
	 */
	public void setState(FieldState state) {
		if(this.fieldState == state) return;
		FieldState oldState = this.fieldState;
		this.fieldState = state;

		if(this.fieldState == FieldState.PROMPT) {
			if(oldState == FieldState.INPUT && super.getText().length() > 0)
				throw new IllegalStateException("Cannot set state to PROMPT when field has input.");
			super.setForeground(this.fieldState.getColor());
			super.setText(prompt);

			endButton.setIcn(null);
			endButton.setEnabled(false);
		} else if(this.fieldState == FieldState.INPUT) {
			super.setForeground(this.fieldState.getColor());
			super.setText("");

			endButton.setIcn(createClearIcon());
			endButton.setEnabled(true);
		}

		super.firePropertyChange(STATE_PROPERTY, oldState, this.fieldState);
	}

	/**
	 * Gets the current state.
	 * 
	 * @return the state
	 */
	public FieldState getState() {
		return this.fieldState;
	}

	/**
	 * State change on focus
	 * 
	 */
	private static FocusListener focusStateListener = new FocusListener() {
		@Override
		public void focusGained(FocusEvent arg0) {
			SearchField sf = (SearchField)arg0.getSource();
			if(sf.fieldState == FieldState.PROMPT) {
				sf.setState(FieldState.INPUT);
			}
		}

		@Override
		public void focusLost(FocusEvent arg0) {
			SearchField sf = (SearchField)arg0.getSource();
			if(sf.getText().length()==0) {
				sf.setState(FieldState.PROMPT);
			}
		}
	};

	/**
	 * Custom shaped button for the search field
	 */
	private class SearchFieldButton extends JButton {

		private int side = SwingConstants.LEFT;

		private Image icn = null;

		public SearchFieldButton(int side, Image icn) {
			this.side = side;
			this.icn = icn;
			super.setOpaque(false);
		}

		public void setIcn(Image icn) {
			this.icn = icn;
		}

		@Override
		protected void paintComponent(Graphics g) {
			// setup graphics context
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// create button shape
			int w = super.getWidth();
			int h = super.getHeight();

			Area btnShape = new Area();
			if(side == SwingConstants.LEFT) {
//				Shape circle = new Ellipse2D.Float(1, 0, h-1.0f, h-1.0f);
				Shape roundRect = new RoundRectangle2D.Float(1.0f, 0.0f, w*2, h-1, h, h);
//				Shape square = new Rectangle2D.Float(h/2.0f+1.0f, 0.0f, w-(h/2.0f)+1, h-1.0f);
				btnShape.add(new Area(roundRect));
//				btnShape.add(new Area(square));
			} else if(side == SwingConstants.RIGHT) {
				Shape roundRect = new RoundRectangle2D.Float(-w, 0.0f, w*2-1, h-1, h, h);
//				Shape square = new Rectangle2D.Float(0.0f, 0.0f, w/2, h-1.0f);
				btnShape.add(new Area(roundRect));
//				btnShape.add(new Area(square));
			}

//			GradientPaint gp = new GradientPaint(new Point(0,0), new Color(215, 215, 215), 
//					new Point(0, h), new Color(200, 200, 200));
//			g2d.setColor(gp);
//			g2d.setPaint(gp);
			g2d.setColor(SearchField.this.getBackground());
			g2d.fill(btnShape);

			// there is sometimes a single pixel artifact left
			// over from the shape intersection.  fix this
			if(side == SwingConstants.LEFT) {
				g2d.fillRect(h/2, 1, 2, h-1);
			} else if(side == SwingConstants.RIGHT) {
				g2d.fillRect(getWidth()-(h/2)-1, 1, 2, h-1);
			}

			g2d.setColor(FieldState.PROMPT.getColor());
			g2d.draw(btnShape);

			if(icn != null ) {
				int btnY = h/2 - icn.getHeight(this)/2;
				int btnX = w/2 - icn.getWidth(this)/2;
				g2d.drawImage(icn, btnX, btnY, null);
			}

//			Rectangle2D rectToRemove = new Rectangle2D.Float(0, h/2, w, h/2);
//			Area areaToRemove = new Area(rectToRemove);
//			Area topArea = (Area)btnShape.clone();
//			topArea.subtract(areaToRemove);
//			 gp = new GradientPaint(new Point(0,0), new Color(255, 255, 255, 75), 
//					new Point(0, h/2), new Color(255, 255, 255, 25));
//			g2d.setPaint(gp);
//			g2d.fill(topArea);
		}

	}
}
