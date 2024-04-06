package org.jabref.gui.entryeditor;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;

import javafx.application.Platform;

import org.jabref.gui.bibtexextractor.ExtractBibtexAction;
import org.jabref.gui.bibtexextractor.ExtractBibtexDialog;
import org.jabref.gui.entryeditor.MultipleEntryFeatures.CustomEntry;

import static org.jabref.gui.actions.ActionHelper.needsDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;

import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.types.BiblatexEntryTypeDefinitions;
import org.jabref.model.entry.types.BibtexEntryTypeDefinitions;
import org.jabref.model.entry.types.EntryType;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.gui.Globals;
import org.jabref.gui.JabRefFrame;
import org.jabref.gui.LibraryTab;
import org.jabref.gui.MainMenu;
import org.jabref.gui.MainToolBar;

import org.jabref.gui.actions.*;
import org.jabref.gui.keyboard.KeyBindingRepository;

import org.jabref.gui.actions.JabRefAction;

import de.saxsys.mvvmfx.utils.commands.Command;
import org.jabref.gui.importer.*;

import org.controlsfx.control.PopOver;
import org.jabref.gui.linkedfile.*;
import org.jabref.gui.maintable.*;
import org.jabref.gui.maintable.*;

public class AdvancedEntryLookUp extends SimpleCommand {

	public class CustomEntry implements EntryType {

		String s = "";

		CustomEntry(String s) {
			this.s = s;
		}

		@Override
		public String getName() {
			return s;
		}

		@Override
		public String getDisplayName() {
			return s.toLowerCase();
		}

	}

	public static CustomEntry ce;

	private static String text_1 = "";

	public static List<String> fields = new ArrayList<>();
	public static List<String> values = new ArrayList<>();

	public AdvancedEntryLookUp() {

	}

	public void ShowWindow() {

		KeyBindingRepository kbr = new KeyBindingRepository();
		// Creating a customized window for adding new entries.

		JFrame frame = new JFrame("Add Entry with Features");
		frame.setSize(300, 150);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel(new GridLayout(0, 1, 0, 0));
		frame.add(panel);

		JTextField textField_1 = new JTextField(15);
		textField_1.setHorizontalAlignment(JTextField.LEFT);
		panel.add(textField_1);

		JButton addButton_1 = new JButton("Smart Entry Search");
		addButton_1.addActionListener(e -> {

			// Storing inputted text

			text_1 = textField_1.getText();

			if (!text_1.contains("@") && (text_1.toLowerCase().contains("doi") || text_1.toLowerCase().contains("http")
					|| text_1.toLowerCase().contains("www.")) && !text_1.toLowerCase().contains("pdf")
					&& !text_1.toLowerCase().contains("txt")) {

				frame.dispose();

				new Thread(new Runnable() {
					@Override
					public void run() {

						if (MainToolBar.Frame() != null && MainToolBar.dser() != null && MainToolBar.pser() != null
								&& MainToolBar.stmn() != null) {

							GenerateEntryFromIdDialog genfrid = new GenerateEntryFromIdDialog(
									MainToolBar.Frame().getCurrentLibraryTab(), MainToolBar.dser(), MainToolBar.pser(),
									MainToolBar.tskexec(), MainToolBar.stmn());

							genfrid.setEntryFromIdPopOver(new PopOver(genfrid.getDialogPane()));

							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									genfrid.generateEntry();
								}
							});
						}
					}
				}).start();

			} else if (text_1.toLowerCase().contains("pdf") || text_1.toLowerCase().contains("txt")) {

				frame.dispose();

				new Thread(new Runnable() {
					@Override
					public void run() {

						AttachFileFromURLAction attachFile = new AttachFileFromURLAction(MainTable.getDser(),
								MainTable.getStm(), MainTable.getTaskExec(), MainTable.getPser());

						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								attachFile.execute();
							}
						});
					}

				}).start();

			} else {
				if (!(text_1.toCharArray()[0] == '@')) {
					frame.dispose();

					new Thread(new Runnable() {
						@Override
						public void run() {

							if (MainToolBar.Frame() != null && MainToolBar.dser() != null && MainToolBar.pser() != null
									&& MainToolBar.stmn() != null) {

								ExtractBibtexAction nea = new ExtractBibtexAction(MainToolBar.dser(),
										MainToolBar.pser(), MainToolBar.stmn());

								ce = new CustomEntry(text_1);

								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										nea.execute();
									}
								});
							}
						}

					}).start();

				} else {
					
					clear_fields();
					clear_values();
					
					String entry_name = text_1.substring(0, text_1.indexOf("{")).replace("@", "");

					final String name = entry_name.substring(0, 1).toUpperCase()
							+ entry_name.substring(1).toLowerCase();

					extract_fields_values(text_1);

					new Thread(new Runnable() {
						@Override
						public void run() {
							NewEntryAction nea = new NewEntryAction(MainToolBar.Frame()::getCurrentLibraryTab,
									new CustomEntry(name), MainToolBar.dser(), MainToolBar.pser(), MainToolBar.stmn());

							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									nea.execute();
								}
							});
						}
					}).start();
					
					
				}
			}

			textField_1.setText("");

		});
		panel.add(addButton_1);

		frame.setVisible(true);
	}

	public static void extract_fields_values(String input) {

		Pattern pattern = Pattern.compile("\\s*([a-zA-Z]+)\\s*=\\s*\\{(.*?)\\},?\\s*");

		Matcher matcher = pattern.matcher(input);

		while (matcher.find()) {
			fields.add(matcher.group(1));
			values.add(matcher.group(2));
		}

	}

	public static String entry_from_plain_text() {
		String t = text_1;
		return t;
	}

	public static void set_text_null() {
		text_1 = "";
	}

	public static List<String> getFieldsList() {
		return new ArrayList<String>(fields);
	}

	public static List<String> getValuesList() {
		return new ArrayList<String>(values);
	}

	public static void clear_fields() {
		fields.clear();
	}

	public static void clear_values() {
		values.clear();
	}

	@Override
	public void execute() {

		ShowWindow();

	}

}