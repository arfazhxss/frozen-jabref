package org.jabref.gui.importer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.jabref.gui.DialogService;
import org.jabref.gui.EntryTypeView;
import org.jabref.gui.LibraryTab;
import org.jabref.gui.StateManager;
import org.jabref.gui.Telemetry;
import org.jabref.gui.actions.ActionHelper;
import org.jabref.gui.actions.SimpleCommand;
import org.jabref.gui.entryeditor.MultipleEntryFeatures;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.types.EntryType;
import org.jabref.preferences.PreferencesService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class NewEntryAction extends SimpleCommand {

    private static Logger LOGGER = LoggerFactory.getLogger(NewEntryAction.class);

    public static Supplier<LibraryTab> tabSupplier;

    /**
     * The type of the entry to create.
     */
    private Optional<EntryType> type;

    private DialogService dialogService;

    private PreferencesService preferences;

    public NewEntryAction(Supplier<LibraryTab> tabSupp, DialogService dialogService, PreferencesService preferences, StateManager stateManager) {
        tabSupplier = tabSupp;
        this.dialogService = dialogService;
        this.preferences = preferences;

        this.type = Optional.empty();

        this.executable.bind(ActionHelper.needsDatabase(stateManager));
        
    }

    public NewEntryAction(Supplier<LibraryTab> tabSupplier, EntryType type, DialogService dialogService, PreferencesService preferences, StateManager stateManager) {
        this(tabSupplier, dialogService, preferences, stateManager);
        this.type = Optional.of(type);
    }
    
    //Editted
    public NewEntryAction() {
    	//this.type = Optional.ofNullable(type);
    }
    
    
    @Override
    public void execute() {
        if (tabSupplier.get() == null) {
            LOGGER.error("Action 'New entry' must be disabled when no database is open.");
            return;
        }

        if (type == null) {
        	tabSupplier.get().insertEntry(new BibEntry(MultipleEntryFeatures.ce));
        }
        else if (type.isPresent()) {
        	
        	//error source
            tabSupplier.get().insertEntry(new BibEntry(type.get()));
            
            //throw new IllegalArgumentException(tabSupplier.toString());
            
        } else {
            EntryTypeView typeChoiceDialog = new EntryTypeView(tabSupplier.get(), dialogService, preferences);
            EntryType selectedType = dialogService.showCustomDialogAndWait(typeChoiceDialog).orElse(null);
            if (selectedType == null) {
                return;
            }

            trackNewEntry(selectedType);
            tabSupplier.get().insertEntry(new BibEntry(selectedType));
        }
    }

    private void trackNewEntry(EntryType type) {
        Map<String, String> properties = new HashMap<>();
        properties.put("EntryType", type.getName());

        Telemetry.getTelemetryClient().ifPresent(client -> client.trackEvent("NewEntry", properties, new HashMap<>()));
    }
}
