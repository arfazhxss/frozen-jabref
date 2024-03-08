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

	private static String text;
	private static ArrayList<String> entries = new ArrayList<>();

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

		JFrame frame = new JFrame("Add Entry");
		frame.setSize(300, 150);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.add(panel);

		JTextField textField = new JTextField(15);
		panel.add(textField);

		JButton addButton = new JButton("Add Entry/Subfield");
		addButton.addActionListener(e -> {
			String text = textField.getText();

			// Storing inputed text
			
			this.text = text;
			entries.add(text);

			textField.setText("");
		});
		panel.add(addButton);

		frame.setVisible(true);
	}

	public static String getText() {
		return text;
	}

	public static ArrayList<String> getTextList() {
		return entries;
	}

	@Override
	public void execute() {
		
		
		ShowWindow();

	}

}
