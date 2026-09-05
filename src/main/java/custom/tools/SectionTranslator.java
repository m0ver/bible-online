package custom.tools;

import custom.ai.GeminiApplication;
import custom.objects.bible_section;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.DatabaseOperator;
import org.tinystruct.data.component.Builder;
import org.tinystruct.data.component.Builders;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.system.annotation.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Translates all zh_CN section headings in the bible_section table into English (en_US)
 * using the Google Gemini API, and saves the results back to the database.
 *
 * <p>Requires {@code gemini.api.key} to be set in {@code application.properties}.
 *
 * <p>Run via command line:
 * <pre>
 *   bin/dispatcher.cmd translate/sections
 * </pre>
 *
 * <p>To avoid re-translating existing data, run with the --skip-existing flag (default: true).
 * Translations are done in batches of 50 to minimise API calls.
 */
public class SectionTranslator extends GeminiApplication {

    private static final Logger logger = Logger.getLogger(SectionTranslator.class.getName());

    private static final String SOURCE_LANG = "zh_CN";
    private static final String TARGET_LANG = "en_US";
    private static final String VERSION     = "ALL";
    private static final int    BATCH_SIZE  = 50;

    @Override
    public void init() {
        super.init();
    }

    @Override
    public String version() {
        return "1.0";
    }

    /**
     * Main translation action.
     *
     * <ol>
     *   <li>Reads all existing zh_CN bible_section rows.</li>
     *   <li>Checks which (book_id, chapter_id, part_id) tuples already have an en_US row.</li>
     *   <li>Translates missing titles in batches via Gemini.</li>
     *   <li>Inserts translated rows into bible_section.</li>
     * </ol>
     */
    @Action("translate/sections")
    public String translateSections() throws ApplicationException {
        logger.info("Loading zh_CN sections...");
        List<SectionRow> sources = loadSections(SOURCE_LANG);
        logger.info("Found " + sources.size() + " zh_CN sections.");

        logger.info("Loading existing en_US sections...");
        List<String> existingKeys = loadExistingKeys(TARGET_LANG);
        logger.info("Found " + existingKeys.size() + " existing en_US sections (will skip).");

        // Filter out already-translated rows
        List<SectionRow> toTranslate = new ArrayList<>();
        for (SectionRow row : sources) {
            if (!existingKeys.contains(row.key())) {
                toTranslate.add(row);
            }
        }
        logger.info("Need to translate " + toTranslate.size() + " sections.");

        if (toTranslate.isEmpty()) {
            return "All sections already translated. en_US count: " + existingKeys.size();
        }

        int inserted = 0;
        int batches = (int) Math.ceil((double) toTranslate.size() / BATCH_SIZE);

        for (int i = 0; i < batches; i++) {
            int from = i * BATCH_SIZE;
            int to   = Math.min(from + BATCH_SIZE, toTranslate.size());
            List<SectionRow> batch = toTranslate.subList(from, to);

            logger.info("Translating batch " + (i + 1) + "/" + batches
                    + " (" + batch.size() + " items)...");

            List<String> titles = new ArrayList<>();
            for (SectionRow r : batch) titles.add(r.title);

            List<String> translated = translateBatch(titles);

            if (translated.size() != batch.size()) {
                logger.warning("Batch " + (i + 1) + ": expected " + batch.size()
                        + " translations, got " + translated.size() + ". Skipping batch.");
                continue;
            }

            for (int j = 0; j < batch.size(); j++) {
                SectionRow src = batch.get(j);
                String enTitle = translated.get(j);
                insertSection(src.bookId, src.chapterId, src.partId, enTitle, src.parallelRef);
                inserted++;
            }

            // Polite delay to avoid hitting rate limits
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }

        return "SUCCESS: Translated and inserted " + inserted + " en_US sections into bible_section.";
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Calls Gemini to translate a batch of Chinese titles into English. */
    private List<String> translateBatch(List<String> titles) throws ApplicationException {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a Bible scholar translator. Translate each of the following Chinese Bible section headings into natural English. ");
        prompt.append("Return ONLY a JSON array of translated strings in the same order, no explanation or markdown. Example: [\"Title 1\",\"Title 2\"]\n\n");
        prompt.append("Chinese headings:\n");
        for (int i = 0; i < titles.size(); i++) {
            prompt.append((i + 1)).append(". ").append(titles.get(i)).append("\n");
        }

        String model = this.getConfiguration().get("gemini.model");
        if (model == null || model.isEmpty()) model = "gemini-2.5-flash-lite";

        String geminiBaseUrl = "https://generativelanguage.googleapis.com/v1beta/models";
        String url = geminiBaseUrl + "/" + model + ":generateContent";

        String payload = "{"
                + "\"contents\":[{\"parts\":[{\"text\":" + jsonString(prompt.toString()) + "}]}],"
                + "\"generationConfig\":{\"temperature\":0.1,\"maxOutputTokens\":2048}"
                + "}";

        List<String> result = new ArrayList<>();
        try {
            Builder response = callGemini(url, "POST", payload);

            // Navigate: candidates[0].content.parts[0].text
            // In tinystruct: JSON arrays are stored as Builders (ArrayList<Builder>)
            Builders candidates = (Builders) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                logger.warning("No candidates in Gemini response.");
                return result;
            }

            Builder first = candidates.get(0);
            Builder content = (Builder) first.get("content");
            Builders parts = (Builders) content.get("parts");
            String text = parts.get(0).get("text").toString().trim();

            // Strip markdown code fences if present (```json ... ```)
            text = text.replaceAll("(?s)^```[a-z]*\\s*", "").replaceAll("```\\s*$", "").trim();

            // Parse the JSON array of translated strings using Builders
            Builders arr = new Builders();
            arr.parse(text);

            for (Builder item : arr) {
                // Each item is a Builder wrapping a single string value
                result.add(item.isSingleValue() ? item.getValue().toString() : item.toString());
            }

        } catch (Exception e) {
            throw new ApplicationException("Gemini translation failed: " + e.getMessage(), e);
        }

        return result;
    }

    private List<SectionRow> loadSections(String language) throws ApplicationException {
        List<SectionRow> list = new ArrayList<>();
        bible_section sectionQuery = new bible_section();
        try {
            Table table = sectionQuery.setRequestFields("*")
                    .findWith("WHERE [language]=? ORDER BY book_id, chapter_id, part_id", 
                              new Object[]{language});
            if (table != null) {
                for (Row row : table) {
                    SectionRow r = new SectionRow();
                    r.bookId      = row.getFieldInfo("book_id").intValue();
                    r.chapterId   = row.getFieldInfo("chapter_id").intValue();
                    r.partId      = row.getFieldInfo("part_id").intValue();
                    r.title       = row.getFieldInfo("title").stringValue();
                    r.parallelRef = row.getFieldInfo("parallel_ref") != null
                            ? row.getFieldInfo("parallel_ref").stringValue() : "";
                    list.add(r);
                }
            }
        } catch (Exception e) {
            throw new ApplicationException("Failed to load sections: " + e.getMessage(), e);
        }
        return list;
    }

    private List<String> loadExistingKeys(String language) throws ApplicationException {
        List<String> keys = new ArrayList<>();
        bible_section sectionQuery = new bible_section();
        try {
            Table table = sectionQuery.setRequestFields("book_id, chapter_id, part_id")
                    .findWith("WHERE [language]=?", new Object[]{language});
            if (table != null) {
                for (Row row : table) {
                    int b = row.getFieldInfo("book_id").intValue();
                    int c = row.getFieldInfo("chapter_id").intValue();
                    int p = row.getFieldInfo("part_id").intValue();
                    keys.add(b + ":" + c + ":" + p);
                }
            }
        } catch (Exception e) {
            throw new ApplicationException("Failed to load existing keys: " + e.getMessage(), e);
        }
        return keys;
    }

    private void insertSection(int bookId, int chapterId, int partId,
                               String title, String parallelRef) throws ApplicationException {
        bible_section section = new bible_section();
        section.setVersion(VERSION);
        section.setLanguage(TARGET_LANG);
        section.setBookId(bookId);
        section.setChapterId(chapterId);
        section.setPartId(partId);
        section.setTitle(title);
        section.setParallelRef(parallelRef);
        section.setIsParagraph(1);
        section.append();
    }

    /** Escapes a string for inclusion in a JSON string value. */
    private static String jsonString(String s) {
        return "\"" + s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }

    // -------------------------------------------------------------------------
    // Helper record
    // -------------------------------------------------------------------------

    private static class SectionRow {
        int    bookId;
        int    chapterId;
        int    partId;
        String title;
        String parallelRef;

        String key() { return bookId + ":" + chapterId + ":" + partId; }
    }
}
