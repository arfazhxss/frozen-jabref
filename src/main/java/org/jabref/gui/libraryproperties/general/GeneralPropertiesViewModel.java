package org.jabref.model.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jabref.architecture.AllowedToUseLogic;
import org.jabref.gui.LibraryTab;
import org.jabref.logic.crawler.Crawler;
import org.jabref.logic.crawler.StudyRepository;
import org.jabref.logic.shared.DatabaseLocation;
import org.jabref.logic.shared.DatabaseSynchronizer;
import org.jabref.logic.util.CoarseChangeFilter;
import org.jabref.logic.util.OS;
import org.jabref.logic.util.io.BackupFileUtil;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.metadata.MetaData;
import org.jabref.model.study.Study;
import org.jabref.preferences.FilePreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents everything related to a BIB file.
 *
 * <p> The entries are stored in BibDatabase, the other data in MetaData
 * and the options relevant for this file in Defaults.
 * </p>
 * <p>
 *     To get an instance for a .bib file, use {@link org.jabref.logic.importer.fileformat.BibtexParser}.
 * </p>
 */
@AllowedToUseLogic("because it needs access to shared database features")
public class BibDatabaseContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryTab.class);

    private final BibDatabase database;
    private MetaData metaData;

    /**
     * The path where this database was last saved to.
     */
    private Path path;

    private DatabaseSynchronizer dbmsSynchronizer;
    private CoarseChangeFilter dbmsListener;
    private DatabaseLocation location;

    public BibDatabaseContext() {
        this(new BibDatabase());
    }

    public BibDatabaseContext(BibDatabase database) {
        this(database, new MetaData());
    }

    public BibDatabaseContext(BibDatabase database, MetaData metaData) {
        this.database = Objects.requireNonNull(database);
        this.metaData = Objects.requireNonNull(metaData);
        this.location = DatabaseLocation.LOCAL;
    }

    public BibDatabaseContext(BibDatabase database, MetaData metaData, Path path) {
        this(database, metaData, path, DatabaseLocation.LOCAL);
    }

    public BibDatabaseContext(BibDatabase database, MetaData metaData, Path path, DatabaseLocation location) {
        this(database, metaData);
        Objects.requireNonNull(location);
        this.path = path;

        if (location == DatabaseLocation.LOCAL) {
            convertToLocalDatabase();
        }
    }

    public BibDatabaseMode getMode() {
        return metaData.getMode().orElse(BibDatabaseMode.BIBLATEX);
    }

    public void setMode(BibDatabaseMode bibDatabaseMode) {
        metaData.setMode(bibDatabaseMode);
    }

    public void setDatabasePath(Path file) {
        this.path = file;
    }

    /**
     * Get the path where this database was last saved to or loaded from, if any.
     *
     * @return Optional of the relevant Path, or Optional.empty() if none is defined.
     */
    public Optional<Path> getDatabasePath() {
        return Optional.ofNullable(path);
    }

    public void clearDatabasePath() {
        this.path = null;
    }

    public BibDatabase getDatabase() {
        return database;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = Objects.requireNonNull(metaData);
    }

    public boolean isBiblatexMode() {
        return getMode() == BibDatabaseMode.BIBLATEX;
    }

    /**
     * Returns whether this .bib file belongs to a {@link Study}
     */
    public boolean isStudy() {
        return this.getDatabasePath()
                .map(path -> path.getFileName().toString().equals(Crawler.FILENAME_STUDY_RESULT_BIB) &&
                        Files.exists(path.resolveSibling(StudyRepository.STUDY_DEFINITION_FILE_NAME)))
                .orElse(false);
    }

    /**
     * Look up the directories set up for this database.
     * There can be up to four directories definitions for these files:
     * <ol>
     * <li>next to the .bib file.</li>
     * <li>the preferences can specify a default one.</li>
     * <li>the database's metadata can specify a general directory.</li>
     * <li>the database's metadata can specify a user-specific directory.</li>
     * </ol>
     * <p>
     * The settings are prioritized in the following order, and the first defined setting is used:
     * <ol>
     *     <li>user-specific metadata directory</li>
     *     <li>general metadata directory</li>
     *     <li>BIB file directory (if configured in the preferences AND none of the two above directories are configured)</li>
     *     <li>preferences directory (if .bib file directory should not be used according to the preferences)</li>
     * </ol>
     *
     * @param preferences The fileDirectory preferences
     */
    public List<Path> getFileDirectories(FilePreferences preferences) {
        List<Path> fileDirs = new ArrayList<>();

        // 1. Metadata user-specific directory
        metaData.getUserFileDirectory(preferences.getUserAndHost())
                .ifPresent(userFileDirectory -> fileDirs.add(getFileDirectoryPath(userFileDirectory)));

        // 2. Metadata general directory
        metaData.getDefaultFileDirectory()
                .ifPresent(metaDataDirectory -> fileDirs.add(getFileDirectoryPath(metaDataDirectory)));

        // 3. BIB file directory or main file directory
        // fileDirs.isEmpty in the case, 1) no user-specific file directory and 2) no general file directory is set
        // (in the metadata of the bib file)
        if (fileDirs.isEmpty() && preferences.shouldStoreFilesRelativeToBibFile()) {
            getDatabasePath().ifPresent(dbPath -> {
                Path parentPath = dbPath.getParent();
                if (parentPath == null) {
                    parentPath = Path.of(System.getProperty("user.dir"));
                }
                Objects.requireNonNull(parentPath, "BibTeX database parent path is null");
                fileDirs.add(parentPath);
            });
        } else {
            // Main file directory
            preferences.getMainFileDirectory().ifPresent(fileDirs::add);
        }

        return fileDirs.stream().map(Path::toAbsolutePath).collect(Collectors.toList());
    }

    /**
     * Returns the first existing file directory from  {@link #getFileDirectories(FilePreferences)}
     *
     * @return the path - or an empty optional, if none of the directories exists
     */
    public Optional<Path> getFirstExistingFileDir(FilePreferences preferences) {
        return getFileDirectories(preferences).stream()
                .filter(Files::exists)
                .findFirst();
    }

    public Optional<Path> getUserFileDirectory(FilePreferences preferences) {
        return metaData.getUserFileDirectory(preferences.getUserAndHost()).map(this::getFileDirectoryPath);
    }


    // -------------------------------------------------------------------------------------------------------------- A3.3
    private Path getFileDirectoryPath(String directoryName) {
        Path directory = Path.of(directoryName);
        // If this directory is relative, we try to interpret it as relative to
        // the file path of this BIB file:
        Optional<Path> databaseFile = getDatabasePath();
        if (!directory.isAbsolute() && databaseFile.isPresent()) {
            return databaseFile.get().getParent().resolve(directory).normalize();
        }
        return directory;
    }
//    private Path getFileDirectoryPath(String directoryName) {
//        Path directory = Path.of(System.getProperty("user.home") + "/Downloads/SENG 371 Spring 2024 Assignment 03");
//        // If this directory is relative, we try to interpret it as relative to
//        // the file path of this BIB file:
//        Optional<Path> databaseFile = getDatabasePath();
//        if (!directory.isAbsolute() && databaseFile.isPresent()) {
//            return databaseFile.get().getParent().resolve(directory).normalize();
//        }
//        return directory;
//    }
    // --------------------------------------------------------------------------------------------------------------

    public DatabaseSynchronizer getDBMSSynchronizer() {
        return this.dbmsSynchronizer;
    }

    public void clearDBMSSynchronizer() {
        this.dbmsSynchronizer = null;
    }

    public DatabaseLocation getLocation() {
        return this.location;
    }

    public void convertToSharedDatabase(DatabaseSynchronizer dmbsSynchronizer) {
        this.dbmsSynchronizer = dmbsSynchronizer;

        this.dbmsListener = new CoarseChangeFilter(this);
        dbmsListener.registerListener(dbmsSynchronizer);

        this.location = DatabaseLocation.SHARED;
    }

    public void convertToLocalDatabase() {
        if (dbmsListener != null && (location == DatabaseLocation.SHARED)) {
            dbmsListener.unregisterListener(dbmsSynchronizer);
            dbmsListener.shutdown();
        }

        this.location = DatabaseLocation.LOCAL;
    }

    public List<BibEntry> getEntries() {
        return database.getEntries();
    }

    /**
     * @return The path to store the lucene index files. One directory for each library.
     */
    public Path getFulltextIndexPath() {
        Path appData = OS.getNativeDesktop().getFulltextIndexBaseDirectory();
        Path indexPath;

        if (getDatabasePath().isPresent()) {
            Path databaseFileName = getDatabasePath().get().getFileName();
            String fileName = BackupFileUtil.getUniqueFilePrefix(databaseFileName) + "--" + databaseFileName;
            indexPath = appData.resolve(fileName);
            LOGGER.debug("Index path for {} is {}", getDatabasePath().get(), indexPath);
            return indexPath;
        }

        indexPath = appData.resolve("unsaved");
        LOGGER.debug("Using index for unsaved database: {}", indexPath);
        return indexPath;
    }

    @Override
    public String toString() {
        return "BibDatabaseContext{" +
                "metaData=" + metaData +
                ", mode=" + getMode() +
                ", databasePath=" + getDatabasePath() +
                ", biblatexMode=" + isBiblatexMode() +
                ", fulltextIndexPath=" + getFulltextIndexPath() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BibDatabaseContext that)) {
            return false;
        }
        return Objects.equals(database, that.database) && Objects.equals(metaData, that.metaData) && Objects.equals(path, that.path) && location == that.location;
    }

    @Override
    public int hashCode() {
        return Objects.hash(database, metaData, path, location);
    }
}
package org.jabref.gui.libraryproperties.general;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

import org.jabref.gui.DialogService;
import org.jabref.gui.libraryproperties.PropertiesTabViewModel;
import org.jabref.gui.util.DirectoryDialogConfiguration;
import org.jabref.logic.l10n.Encodings;
import org.jabref.logic.shared.DatabaseLocation;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.database.BibDatabaseMode;
import org.jabref.model.metadata.MetaData;
import org.jabref.preferences.PreferencesService;

public class GeneralPropertiesViewModel implements PropertiesTabViewModel {

    private final BooleanProperty encodingDisableProperty = new SimpleBooleanProperty();
    private final ListProperty<Charset> encodingsProperty = new SimpleListProperty<>(FXCollections.observableArrayList(Encodings.getCharsets()));
    private final ObjectProperty<Charset> selectedEncodingProperty = new SimpleObjectProperty<>(Encodings.getCharsets().get(0));
    private final ListProperty<BibDatabaseMode> databaseModesProperty = new SimpleListProperty<>(FXCollections.observableArrayList(BibDatabaseMode.values()));
    private final SimpleObjectProperty<BibDatabaseMode> selectedDatabaseModeProperty = new SimpleObjectProperty<>(BibDatabaseMode.BIBLATEX);
    private final StringProperty generalFileDirectoryProperty = new SimpleStringProperty("");
    private final StringProperty userSpecificFileDirectoryProperty = new SimpleStringProperty("");
    private final StringProperty laTexFileDirectoryProperty = new SimpleStringProperty("");

    private final DialogService dialogService;
    private final PreferencesService preferencesService;

    private final BibDatabaseContext databaseContext;
    private final MetaData metaData;
    private final DirectoryDialogConfiguration directoryDialogConfiguration;

    GeneralPropertiesViewModel(BibDatabaseContext databaseContext, DialogService dialogService, PreferencesService preferencesService) {
        this.dialogService = dialogService;
        this.preferencesService = preferencesService;
        this.databaseContext = databaseContext;
        this.metaData = databaseContext.getMetaData();

        this.directoryDialogConfiguration = new DirectoryDialogConfiguration.Builder()
                .withInitialDirectory(preferencesService.getFilePreferences().getWorkingDirectory()).build();
    }

    @Override
    public void setValues() {
        boolean isShared = databaseContext.getLocation() == DatabaseLocation.SHARED;
        encodingDisableProperty.setValue(isShared); // the encoding of shared database is always UTF-8

        selectedEncodingProperty.setValue(metaData.getEncoding().orElse(StandardCharsets.UTF_8));
        selectedDatabaseModeProperty.setValue(metaData.getMode().orElse(BibDatabaseMode.BIBLATEX));
        generalFileDirectoryProperty.setValue(metaData.getDefaultFileDirectory().orElse("").trim());
        userSpecificFileDirectoryProperty.setValue(metaData.getUserFileDirectory(preferencesService.getFilePreferences().getUserAndHost()).orElse("").trim());
        laTexFileDirectoryProperty.setValue(metaData.getLatexFileDirectory(preferencesService.getFilePreferences().getUserAndHost()).map(Path::toString).orElse(""));
    }

    @Override
    public void storeSettings() {
        MetaData newMetaData = databaseContext.getMetaData();

        newMetaData.setEncoding(selectedEncodingProperty.getValue());
        newMetaData.setMode(selectedDatabaseModeProperty.getValue());

        String generalFileDirectory = generalFileDirectoryProperty.getValue().trim();
        if (generalFileDirectory.isEmpty()) {
            newMetaData.clearDefaultFileDirectory();
        } else {
            newMetaData.setDefaultFileDirectory(generalFileDirectory);
        }

        String userSpecificFileDirectory = userSpecificFileDirectoryProperty.getValue();
        if (userSpecificFileDirectory.isEmpty()) {
            newMetaData.clearUserFileDirectory(preferencesService.getFilePreferences().getUserAndHost());
        } else {
            newMetaData.setUserFileDirectory(preferencesService.getFilePreferences().getUserAndHost(), userSpecificFileDirectory);
        }

        String latexFileDirectory = laTexFileDirectoryProperty.getValue();
        if (latexFileDirectory.isEmpty()) {
            newMetaData.clearLatexFileDirectory(preferencesService.getFilePreferences().getUserAndHost());
        } else {
            newMetaData.setLatexFileDirectory(preferencesService.getFilePreferences().getUserAndHost(), Path.of(latexFileDirectory));
        }

        databaseContext.setMetaData(newMetaData);
    }

    public void browseGeneralDir() {
        dialogService.showDirectorySelectionDialog(directoryDialogConfiguration)
                     .ifPresent(dir -> generalFileDirectoryProperty.setValue(dir.toAbsolutePath().toString()));
    }

    public void browseUserDir() {
        dialogService.showDirectorySelectionDialog(directoryDialogConfiguration)
                     .ifPresent(dir -> userSpecificFileDirectoryProperty.setValue(dir.toAbsolutePath().toString()));
    }

    public void browseLatexDir() {
        dialogService.showDirectorySelectionDialog(directoryDialogConfiguration)
                     .ifPresent(dir -> laTexFileDirectoryProperty.setValue(dir.toAbsolutePath().toString()));
    }

    public BooleanProperty encodingDisableProperty() {
        return encodingDisableProperty;
    }

    public ListProperty<Charset> encodingsProperty() {
        return this.encodingsProperty;
    }

    public ObjectProperty<Charset> selectedEncodingProperty() {
        return selectedEncodingProperty;
    }

    public ListProperty<BibDatabaseMode> databaseModesProperty() {
        return databaseModesProperty;
    }

    public SimpleObjectProperty<BibDatabaseMode> selectedDatabaseModeProperty() {
        return selectedDatabaseModeProperty;
    }

    public StringProperty generalFileDirectoryPropertyProperty() {
        return this.generalFileDirectoryProperty;
    }

    public StringProperty userSpecificFileDirectoryProperty() {
        return this.userSpecificFileDirectoryProperty;
    }

    public StringProperty laTexFileDirectoryProperty() {
        return this.laTexFileDirectoryProperty;
    }
}
