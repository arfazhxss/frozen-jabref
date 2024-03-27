package org.jabref.gui.entryeditor;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;

import javafx.application.Platform;

import org.jabref.gui.bibtexextractor.ExtractBibtexAction;
import org.jabref.gui.bibtexextractor.ExtractBibtexDialog;
import org.jabref.gui.importer.GrobidOptInDialogHelper;
import org.jabref.gui.importer.NewEntryAction;

import static org.jabref.gui.actions.ActionHelper.needsDatabase;

import java.util.ArrayList;
import java.util.function.Supplier;

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


public class MultipleEntryFeatures extends SimpleCommand {

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

	
	public MultipleEntryFeatures() {

	}

	public void ShowWindow() {

		KeyBindingRepository kbr = new KeyBindingRepository();
		// Creating a customized window for adding new entries.

		JFrame frame = new JFrame("Add Entry with Features");
		frame.setSize(300, 150);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel(new GridLayout(0, 1, 2, 2));
		frame.add(panel);

		JTextField textField_1 = new JTextField(15);
		panel.add(textField_1);

		JButton addButton_1 = new JButton("Add Entry From Plain Text");
		addButton_1.addActionListener(e -> {

			// Storing inputted text

			text_1 = textField_1.getText();

			if (MainToolBar.Frame() != null && MainToolBar.dser() != null && MainToolBar.pser() != null
					&& MainToolBar.stmn() != null) {

				frame.dispose();

				new Thread(new Runnable() {
					@Override
					public void run() {
						
						ExtractBibtexAction nea = new ExtractBibtexAction(MainToolBar.dser(), MainToolBar.pser(), MainToolBar.stmn());

						ce = new CustomEntry(text_1);
						
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								nea.execute();
							}
						});
					}
				}).start();
			}

			textField_1.setText("");
		});
		panel.add(addButton_1);

		JTextField textField_2 = new JTextField(15);
		panel.add(textField_2);

		JButton addButton_2 = new JButton("Search Entry By ID");
		addButton_2.addActionListener(e -> {
			String text = textField_2.getText();
			text_1 = text;
			
			frame.dispose();
			
			new Thread(new Runnable() {
				@Override
				public void run() {
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
			}).start();

			textField_2.setText("");

		});
		panel.add(addButton_2);
		
		JLabel blank = new JLabel("++++".repeat(100));
		panel.add(blank);
		
		JButton addButton_3 = new JButton("Predefined Availabe Entry Types");
		addButton_3.addActionListener(e -> {

			// TODO add functionality of new entry

			if (MainToolBar.Frame() != null && MainToolBar.dser() != null && MainToolBar.pser() != null
					&& MainToolBar.stmn() != null) {

				frame.dispose();

				new Thread(new Runnable() {
					@Override
					public void run() {
						NewEntryAction nea = new NewEntryAction(MainToolBar.Frame()::getCurrentLibraryTab,
								MainToolBar.dser(), MainToolBar.pser(), MainToolBar.stmn());

						ce = new CustomEntry(text_1);

						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								nea.execute();
							}
						});
					}
				}).start();
			}

		});
		
		panel.add(addButton_3);

		JButton addButton_4 = new JButton("Add New Article Entry");
		addButton_4.addActionListener(e -> {

			// TODO add functionality of new entry

			new Thread(new Runnable() {
				@Override
				public void run() {
					NewEntryAction nea = new NewEntryAction(MainToolBar.Frame()::getCurrentLibraryTab,
							StandardEntryType.Article, MainToolBar.dser(), MainToolBar.pser(), MainToolBar.stmn());
					;

					ce = new CustomEntry(text_1);
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							nea.execute();
						}
					});
				}
			}).start();

		});
		panel.add(addButton_4);

		frame.setVisible(true);
	}

	public static String entry_from_plain_text() {
		String t = text_1;
		return t;
	}
	
	public static void set_text_null() {
		text_1 = "";
	}
	
	@Override
	public void execute() {

		ShowWindow();

	}
}