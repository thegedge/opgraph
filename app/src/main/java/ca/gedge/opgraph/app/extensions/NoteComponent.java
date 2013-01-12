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
package ca.gedge.opgraph.app.extensions;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.components.DoubleClickableTextField;
import ca.gedge.opgraph.app.components.ResizeGrip;
import ca.gedge.opgraph.app.edits.notes.SetNoteTextEdit;

/**
 * A component for displaying a {@link Note}.
 */
public class NoteComponent extends JPanel {
	/** The default note color */
	private static final Color DEFAULT_COLOR = new Color(255, 255, 150);

	/** The note this component displaying */
	private WeakReference<Note> noteRef;

	/** Background color */
	private Color background;

	/** Border color */
	private Color border;

	/** Title text field */
	private JTextField titleField;

	/** Double-click support for the title text field */
	private DoubleClickableTextField titleDoubleClickSupport;

	/** Body text field */
	private JTextArea bodyField;

	/** Double-click support for the body text field */
	private DoubleClickableTextField bodyDoubleClickSupport;

	/** Resize grip */
	private ResizeGrip resizeGrip;

	/**
	 * Constructs a component to display a given {@link Note}.
	 * 
	 * @param note  the note
	 */
	public NoteComponent(Note note) {
		this.noteRef = new WeakReference<Note>(note);

		// Components
		this.titleField = new JTextField(note.getTitle());
		this.titleField.setBorder(new EmptyBorder(2, 5, 2, 5));
		this.titleDoubleClickSupport = new DoubleClickableTextField(this.titleField);
		this.titleDoubleClickSupport.addPropertyChangeListener(DoubleClickableTextField.TEXT_PROPERTY, textListener);

		this.bodyField = new JTextArea(note.getBody());
		this.bodyField.setLineWrap(true);
		this.bodyField.setBorder(new EmptyBorder(2, 5, 2, 5));
		this.bodyDoubleClickSupport = new DoubleClickableTextField(this.bodyField);
		this.bodyDoubleClickSupport.addPropertyChangeListener(DoubleClickableTextField.TEXT_PROPERTY, textListener);

		this.resizeGrip = new ResizeGrip(this);
		this.resizeGrip.setBackground(new Color(0, 0, 0, 100));

		add(this.resizeGrip);
		add(this.titleField);
		add(this.bodyField);

		// Layout
		final SpringLayout layout = new SpringLayout();

		layout.putConstraint(SpringLayout.NORTH, this.titleField, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, this.titleField, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, this.titleField, 0, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.EAST, this.bodyField, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, this.bodyField, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, this.bodyField, 0, SpringLayout.SOUTH, this);

		layout.putConstraint(SpringLayout.EAST, this.resizeGrip, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, this.resizeGrip, 0, SpringLayout.SOUTH, this);

		layout.putConstraint(SpringLayout.NORTH, this.bodyField, 1, SpringLayout.SOUTH, this.titleField);

		setLayout(layout);

		// Compute initial preferred size
		final Dimension titlePref = this.titleField.getPreferredSize();
		final Dimension bodyPref = this.bodyField.getPreferredSize();
		final Dimension resizePref = this.resizeGrip.getPreferredSize();

		final int prefw = Math.max(titlePref.width, bodyPref.width) + resizePref.width;
		final int prefh = titlePref.height + 1 + Math.max(bodyPref.height, resizePref.width);
		setPreferredSize(new Dimension(prefw, prefh));
		setMinimumSize(new Dimension(prefw, prefh));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

		setBackground(DEFAULT_COLOR);
		setOpaque(false);
	}

	/**
	 * Gets the note this component is displaying.
	 * 
	 * @return the note
	 */
	public Note getNote() {
		return noteRef.get();
	}

	/**
	 * Gets the resize grip for this component.
	 * 
	 * @return the resize grip
	 */
	public ResizeGrip getResizeGrip() {
		return resizeGrip;
	}

	//
	// Overrides
	//

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		background = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 200);
		border = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 255);
		setBorder(new MatteBorder(1, 1, 1, 1, border));
	}

	@Override
	protected void paintComponent(Graphics gfx) {
		super.paintComponent(gfx);

		final Graphics2D g = (Graphics2D)gfx;
		g.setColor(background);
		g.fill(bodyField.getBounds());

		final Rectangle titleBounds = titleField.getBounds();
		++titleBounds.height;
		g.setColor(border);
		g.fill(titleBounds);
	}

	//
	// DoubleClickableTextField property listener
	//

	private final PropertyChangeListener textListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			final Note note = noteRef.get();

			String title = note.getTitle();
			String body = note.getBody();

			if(e.getSource() == bodyDoubleClickSupport) {
				body = (String)e.getNewValue();
			} else {
				title = (String)e.getNewValue();
			}

			final SetNoteTextEdit edit = new SetNoteTextEdit(note, title, body);
			GraphEditorModel.getActiveDocument().getUndoSupport().postEdit(edit);
		}
	};
}
