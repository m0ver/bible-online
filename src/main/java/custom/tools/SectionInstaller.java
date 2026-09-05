package custom.tools;

import custom.objects.bible_section;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.DatabaseOperator;
import org.tinystruct.system.annotation.Action;

public class SectionInstaller extends AbstractApplication {

    @Override
    public void init() {
    }

    @Override
    public String version() {
        return "1.0";
    }

    @Action("install/sections")
    public String install() throws ApplicationException {
        try (DatabaseOperator operator = new DatabaseOperator()) {
            operator.disableSafeCheck();
            // Create table if not exists
            String createTableSQL = "CREATE TABLE IF NOT EXISTS [bible_section] (" +
                    "[id] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "[version] VARCHAR(20) DEFAULT 'ALL', " +
                    "[language] VARCHAR(20) NOT NULL, " +
                    "[book_id] INTEGER NOT NULL, " +
                    "[chapter_id] INTEGER NOT NULL, " +
                    "[part_id] INTEGER NOT NULL, " +
                    "[title] VARCHAR(255) NOT NULL, " +
                    "[parallel_ref] VARCHAR(255) DEFAULT '', " +
                    "[is_paragraph] INTEGER DEFAULT 1);";
            operator.execute(createTableSQL);

            String createIndexSQL = "CREATE INDEX IF NOT EXISTS [idx_section_chapter] " +
                    "ON [bible_section]([book_id], [chapter_id], [language]);";
            operator.execute(createIndexSQL);

            return "SUCCESS: bible_section table and index created. Run 'import/sections' to populate data from USFM files.";
        } catch (Exception e) {
            throw new ApplicationException("Installation failed: " + e.getMessage(), e);
        }
    }

    private void insertSection(String version, String language, int bookId, int chapterId, int partId, String title, String parallelRef, int isParagraph) throws ApplicationException {
        bible_section section = new bible_section();
        section.setVersion(version);
        section.setLanguage(language);
        section.setBookId(bookId);
        section.setChapterId(chapterId);
        section.setPartId(partId);
        section.setTitle(title);
        section.setParallelRef(parallelRef);
        section.setIsParagraph(isParagraph);
        section.append();
    }
}
