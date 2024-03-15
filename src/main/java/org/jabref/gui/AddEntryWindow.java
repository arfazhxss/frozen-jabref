package org.jabref.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jabref.gui.actions.SimpleCommand;
import org.jabref.gui.bibtexextractor.ExtractBibtexDialog;
import org.jabref.gui.importer.GrobidOptInDialogHelper;

/**
import org.jabref.gui.DialogService;
import org.jabref.gui.StateManager;
import org.jabref.preferences.PreferencesService;

import com.airhacks.afterburner.injection.Injector;
**/
import static org.jabref.gui.actions.ActionHelper.needsDatabase;

import java.util.ArrayList;

import javax.swing.JButton;

import org.jabref.model.entry.types.BiblatexEntryTypeDefinitions;
import org.jabref.model.entry.types.BibtexEntryTypeDefinitions;

public class AddEntryWindow extends SimpleCommand {

	/*
	 * private DialogService dialogueService; private PreferencesService
	 * preferencesService; private StateManager stateManager;
	 */


	private static ArrayList<String> entries = new ArrayList<>();
	private static ArrayList<String> fields = new ArrayList<>();

	public AddEntryWindow()// DialogService dialogueService,PreferencesService preferencesService,
							// StateManager stateManager
	{
		/**
		 * this.dialogueService = dialogueService; this.preferencesService =
		 * preferencesService; this.stateManager = stateManager;
		 **/
	}

	public void ShowWindow() {

		// Creating a customized window for adding new entries.

		JFrame frame = new JFrame("Add Entry an Fields");
		frame.setSize(300, 150);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
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
			// Storing inputed text
			
			fields.add(text);
	
			
			textField_2.setText("");
		});
		panel.add(addButton_2);
		
		frame.setVisible(true);
	}

	public static ArrayList<String> getTextList() {
		return entries;
	}
	
	public static ArrayList<String> getFieldsList() {
		
		return fields;
	}
	
	public static void ClearFieldsList() {
		fields.clear();
	}

	@Override
	public void execute() {
		
		
		ShowWindow();

	}

}
