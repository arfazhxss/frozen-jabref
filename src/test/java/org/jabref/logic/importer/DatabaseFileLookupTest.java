package org.jabref.logic.importer;

import java.util.Collection;

import org.jabref.logic.importer.fileformat.BibtexImporter;
import org.jabref.logic.util.io.DatabaseFileLookup;
import org.jabref.model.database.BibDatabase;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.util.DummyFileUpdateMonitor;
import org.jabref.preferences.FilePreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

class DatabaseFileLookupTest {

    private BibDatabase database;
    private Collection<BibEntry> entries;

    private BibEntry entry1;
    private BibEntry entry2;

    @BeforeEach
    void setUp() throws Exception {
        ParserResult result = new BibtexImporter(mock(ImportFormatPreferences.class, Answers.RETURNS_DEEP_STUBS), new DummyFileUpdateMonitor())
                                                                                                                                               .importDatabase(ImportDataTest.UNLINKED_FILES_TEST_BIB);
        database = result.getDatabase();
        entries = database.getEntries();

        entry1 = database.getEntryByCitationKey("entry1").get();
        entry2 = database.getEntryByCitationKey("entry2").get();
    }

    /**
     * Tests the prerequisites of this test-class itself.
     */
    @Test
    void prerequisitesFulfilled() {
        assertEquals(2, database.getEntryCount());
        assertEquals(2, entries.size());
        assertNotNull(entry1);
        assertNotNull(entry2);
    }

    @Test
    void testFileLookupForPresentAndAbsentFiles(@TempDir Path tempDir) throws IOException {
        // Create a temporary file directory
        Path xTxtFile = tempDir.resolve("x.txt");
        Files.write(xTxtFile, Collections.singleton("Sample content"));

        // Create a BibDatabaseContext with a BibDatabase containing two entries
        BibDatabase bibDatabase = new BibDatabase();
        BibEntry entry1 = new BibEntry();
        entry1.setField(StandardField.FILE, xTxtFile.toAbsolutePath().toString());
        BibEntry entry2 = new BibEntry();
        entry2.setField(StandardField.FILE, tempDir.resolve("y.txt").toAbsolutePath().toString());
        bibDatabase.insertEntry(entry1);
        bibDatabase.insertEntry(entry2);

        BibDatabaseContext databaseContext = new BibDatabaseContext(bibDatabase);

        // Set the temporary directory as the default file directory in the preferences
        FilePreferences filePreferences = new FilePreferences("", tempDir.toString(), false, "", "", false, false, null, Collections.emptySet(), false, null);

        // Create DatabaseFileLookup instance
        DatabaseFileLookup fileLookup = new DatabaseFileLookup(databaseContext, filePreferences);

        // Perform file lookup
        assertTrue(fileLookup.lookupDatabase(xTxtFile)); // x.txt should be found
        assertFalse(fileLookup.lookupDatabase(tempDir.resolve("y.txt"))); // y.txt should not be found
    }
}
/*
    @Test
    void testFileLookupForPresentAndAbsentFiles(@TempDir Path tempDir) throws IOException {
        // Create a temporary file directory
        Path xTxtFile = tempDir.resolve("x.txt");
        Files.write(xTxtFile, Collections.singleton("Sample content"));

        // Create a BibDatabaseContext with a BibDatabase containing two entries
        BibDatabase bibDatabase = new BibDatabase();
        BibEntry entry1 = new BibEntry();
        entry1.setField(StandardField.FILE, xTxtFile.toAbsolutePath().toString());
        BibEntry entry2 = new BibEntry();
        entry2.setField(StandardField.FILE, tempDir.resolve("y.txt").toAbsolutePath().toString());
        bibDatabase.insertEntry(entry1);
        bibDatabase.insertEntry(entry2);

        BibDatabaseContext databaseContext = new BibDatabaseContext(bibDatabase);

        // Set the temporary directory as the default file directory in the preferences
        FilePreferences filePreferences = new FilePreferences("", tempDir.toString(), false, "", "", false, false, null, Collections.emptySet(), false, null);

        // Create DatabaseFileLookup instance
        DatabaseFileLookup fileLookup = new DatabaseFileLookup(databaseContext, filePreferences);

        // Perform file lookup
        assertTrue(fileLookup.lookupDatabase(xTxtFile)); // x.txt should be found
        assertFalse(fileLookup.lookupDatabase(tempDir.resolve("y.txt"))); // y.txt should not be found
    }
 */