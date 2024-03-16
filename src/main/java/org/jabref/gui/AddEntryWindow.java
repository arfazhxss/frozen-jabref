package org.jabref.gui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;

import org.jabref.gui.actions.SimpleCommand;
import org.jabref.gui.bibtexextractor.ExtractBibtexDialog;
import org.jabref.gui.importer.GrobidOptInDialogHelper;

import static org.jabref.gui.actions.ActionHelper.needsDatabase;

import java.util.ArrayList;

import javax.swing.JButton;

import org.jabref.model.entry.types.BiblatexEntryTypeDefinitions;
import org.jabref.model.entry.types.BibtexEntryTypeDefinitions;

public class AddEntryWindow extends SimpleCommand {

	private static ArrayList<String> entries = new ArrayList<>();
	private static ArrayList<String> fields = new ArrayList<>();

	public AddEntryWindow() {

	}

	public void ShowWindow() {

		// Creating a customized window for adding new entries.

		JFrame frame = new JFrame("Add Entry and Fields");
		frame.setSize(300, 150);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel(new GridLayout(0, 2, 0, 0));
		frame.add(panel);

		JTextField textField_1 = new JTextField(15);
		panel.add(textField_1);

		JButton addButton_1 = new JButton("Add Entry");
		addButton_1.addActionListener(e -> {
			String text = textField_1.getText();

			// Storing inputted text

			entries.add(text);
			ClearFieldsList();

			textField_1.setText("");
		});
		panel.add(addButton_1);

		JTextField textField_2 = new JTextField(15);
		panel.add(textField_2);

		JButton addButton_2 = new JButton("Add Field");
		addButton_2.addActionListener(e -> {
			String text = textField_2.getText();

			// Storing inputted text

			fields.add(text);

			textField_2.setText("");

		});
		panel.add(addButton_2);

		// Separator for quick custom fields' buttons

		JLabel custom_entries_table = new JLabel("Quik Custom Fields");
		panel.add(custom_entries_table);

		JLabel separator = new JLabel("");
		panel.add(separator);

		// Buttons for a quick field addition

		JButton addButton_3 = new JButton("Author");
		addButton_3.addActionListener(e -> {

			fields.add("Author");

		});
		panel.add(addButton_3);

		JButton addButton_4 = new JButton("Title");
		addButton_4.addActionListener(e -> {

			fields.add("Title");

		});

		panel.add(addButton_4);

		JButton addButton_5 = new JButton("ID");
		addButton_5.addActionListener(e -> {

			fields.add("ID");

		});
		panel.add(addButton_5);

		JButton addButton_6 = new JButton("Date");
		addButton_6.addActionListener(e -> {

			fields.add("Date");

		});
		panel.add(addButton_6);

		JButton addButton_7 = new JButton("Website");
		addButton_7.addActionListener(e -> {

			fields.add("Website");

		});

		panel.add(addButton_7);

		frame.setVisible(true);
	}

	public static ArrayList<String> getTextList() {

		return new ArrayList<String>(entries);
	}

	public static ArrayList<String> getFieldsList() {

		return new ArrayList<String>(fields);
	}

	public static void ClearFieldsList() {
		fields.clear();
	}

	@Override
	public void execute() {

		ShowWindow();

	}

}
