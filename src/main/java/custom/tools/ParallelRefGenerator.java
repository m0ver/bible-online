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

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParallelRefGenerator extends GeminiApplication {

    private static final Logger logger = Logger.getLogger(ParallelRefGenerator.class.getName());
    private static final int BATCH_SIZE = 30;

    private static final Map<String, String> EN_TO_ZH = new LinkedHashMap<>();

    static {
        // Old Testament
        EN_TO_ZH.put("Gen", "创"); EN_TO_ZH.put("Ex", "出"); EN_TO_ZH.put("Lev", "利");
        EN_TO_ZH.put("Num", "民"); EN_TO_ZH.put("Deut", "申"); EN_TO_ZH.put("Josh", "书");
        EN_TO_ZH.put("Judg", "士"); EN_TO_ZH.put("Ruth", "得"); EN_TO_ZH.put("1 Sam", "撒上");
        EN_TO_ZH.put("2 Sam", "撒下"); EN_TO_ZH.put("1 Ki", "王上"); EN_TO_ZH.put("2 Ki", "王下");
        EN_TO_ZH.put("1 Chr", "代上"); EN_TO_ZH.put("2 Chr", "代下"); EN_TO_ZH.put("Ezra", "拉");
        EN_TO_ZH.put("Neh", "尼"); EN_TO_ZH.put("Est", "斯"); EN_TO_ZH.put("Job", "伯");
        EN_TO_ZH.put("Ps", "诗"); EN_TO_ZH.put("Prov", "箴"); EN_TO_ZH.put("Eccl", "传");
        EN_TO_ZH.put("Song", "歌"); EN_TO_ZH.put("Isa", "赛"); EN_TO_ZH.put("Jer", "耶");
        EN_TO_ZH.put("Lam", "哀"); EN_TO_ZH.put("Ezek", "结"); EN_TO_ZH.put("Dan", "但");
        EN_TO_ZH.put("Hos", "何"); EN_TO_ZH.put("Joel", "珥"); EN_TO_ZH.put("Amos", "摩");
        EN_TO_ZH.put("Obad", "俄"); EN_TO_ZH.put("Jonah", "拿"); EN_TO_ZH.put("Mic", "弥");
        EN_TO_ZH.put("Nah", "鸿"); EN_TO_ZH.put("Hab", "哈"); EN_TO_ZH.put("Zeph", "番");
        EN_TO_ZH.put("Hag", "该"); EN_TO_ZH.put("Zech", "亚"); EN_TO_ZH.put("Mal", "玛");

        // New Testament
        EN_TO_ZH.put("Mt", "太"); EN_TO_ZH.put("Mk", "可"); EN_TO_ZH.put("Lk", "路");
        EN_TO_ZH.put("Jn", "约"); EN_TO_ZH.put("Acts", "徒"); EN_TO_ZH.put("Rom", "罗");
        EN_TO_ZH.put("1 Cor", "林前"); EN_TO_ZH.put("2 Cor", "林后"); EN_TO_ZH.put("Gal", "加");
        EN_TO_ZH.put("Eph", "弗"); EN_TO_ZH.put("Phil", "腓"); EN_TO_ZH.put("Col", "西");
        EN_TO_ZH.put("1 Thess", "帖前"); EN_TO_ZH.put("2 Thess", "帖后"); EN_TO_ZH.put("1 Tim", "提前");
        EN_TO_ZH.put("2 Tim", "提后"); EN_TO_ZH.put("Titus", "多"); EN_TO_ZH.put("Phlm", "门");
        EN_TO_ZH.put("Heb", "来"); EN_TO_ZH.put("Jas", "雅"); EN_TO_ZH.put("1 Pet", "彼前");
        EN_TO_ZH.put("2 Pet", "彼后"); EN_TO_ZH.put("1 Jn", "约一"); EN_TO_ZH.put("2 Jn", "约二");
        EN_TO_ZH.put("3 Jn", "约三"); EN_TO_ZH.put("Jude", "犹"); EN_TO_ZH.put("Rev", "启");
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public String version() {
        return "1.0";
    }

    @Action("generate/parallel-refs")
    public String generateAll() throws ApplicationException {
        return generateForBook(-1);
    }

    @Action("generate/parallel-refs")
    public String generateForBook(int targetBookId) throws ApplicationException {
        logger.info("Loading en_US sections...");
        List<SectionRow> sources = loadSections("en_US", targetBookId);
        logger.info("Found " + sources.size() + " en_US sections to process.");

        if (sources.isEmpty()) {
            return "No sections found.";
        }

        int updated = 0;
        int batches = (int) Math.ceil((double) sources.size() / BATCH_SIZE);

        for (int i = 0; i < batches; i++) {
            int from = i * BATCH_SIZE;
            int to = Math.min(from + BATCH_SIZE, sources.size());
            List<SectionRow> batch = sources.subList(from, to);

            logger.info("Generating batch " + (i + 1) + "/" + batches + " (" + batch.size() + " items)...");

            List<String> enRefs;
            try {
                enRefs = generateBatch(batch);
            } catch (Exception e) {
                logger.warning("Batch " + (i + 1) + " failed: " + e.getMessage() + ". Skipping batch.");
                continue;
            }

            if (enRefs.size() != batch.size()) {
                logger.warning("Batch " + (i + 1) + ": expected " + batch.size()
                        + " refs, got " + enRefs.size() + ". Skipping batch.");
                continue;
            }

            for (int j = 0; j < batch.size(); j++) {
                SectionRow src = batch.get(j);
                String enRef = enRefs.get(j).trim();
                
                // Skip updating if empty to save DB calls, unless we really want to overwrite
                if (enRef.isEmpty() || enRef.equalsIgnoreCase("none")) {
                    continue; 
                }
                
                // Sanitize enRef to make sure it doesn't contain weird JSON artifacts
                if (enRef.equalsIgnoreCase("null") || enRef.equals("[]") || enRef.equals("\"\"")) {
                    continue;
                }

                String zhRef = translateToChinese(enRef);

                updateSection(src.bookId, src.chapterId, src.partId, "en_US", enRef);
                updateSection(src.bookId, src.chapterId, src.partId, "en_GB", enRef);
                updateSection(src.bookId, src.chapterId, src.partId, "zh_CN", zhRef);
                updateSection(src.bookId, src.chapterId, src.partId, "zh_TW", zhRef);
                updated++;
            }

            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }

        return "SUCCESS: Updated parallel references for " + updated + " sections across 4 languages.";
    }

    private String translateToChinese(String enRef) {
        String zhRef = enRef;
        for (Map.Entry<String, String> entry : EN_TO_ZH.entrySet()) {
            // Replace the book name abbreviations.
            // Using a simple regex to match whole words/abbreviations safely.
            String regex = "(?<![A-Za-z])" + Pattern.quote(entry.getKey()) + "(?![A-Za-z])";
            zhRef = zhRef.replaceAll(regex, entry.getValue());
        }
        return zhRef;
    }

    private List<String> generateBatch(List<SectionRow> batch) throws ApplicationException {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a Bible cross-reference scholar. For each section heading below, provide the parallel ");
        prompt.append("passage references in OTHER books of the Bible that describe the exact same event or teaching (e.g. synoptic gospels). ");
        prompt.append("If there are no direct parallel passages, return an empty string.\n\n");
        prompt.append("Format: Use standard abbreviations (Gen, Ex, Lev, Num, Deut, Josh, Judg, Ruth, 1 Sam, 2 Sam, ");
        prompt.append("1 Ki, 2 Ki, 1 Chr, 2 Chr, Ezra, Neh, Est, Job, Ps, Prov, Eccl, Song, Isa, Jer, Lam, Ezek, ");
        prompt.append("Dan, Hos, Joel, Amos, Obad, Jonah, Mic, Nah, Hab, Zeph, Hag, Zech, Mal, Mt, Mk, Lk, Jn, ");
        prompt.append("Acts, Rom, 1 Cor, 2 Cor, Gal, Eph, Phil, Col, 1 Thess, 2 Thess, 1 Tim, 2 Tim, Titus, Phlm, ");
        prompt.append("Heb, Jas, 1 Pet, 2 Pet, 1 Jn, 2 Jn, 3 Jn, Jude, Rev).\n\n");
        prompt.append("Multiple references should be separated by \"; \".\n\n");
        prompt.append("Return ONLY a JSON array of strings in the same order. Example: [\"Mk 1:1-8; Lk 3:1-18\",\"\",\"Lk 4:1-13\"]\n\n");
        prompt.append("Sections:\n");
        for (int i = 0; i < batch.size(); i++) {
            SectionRow r = batch.get(i);
            prompt.append((i + 1)).append(". Book ").append(r.bookId).append(", Chapter ").append(r.chapterId)
                  .append(":").append(r.partId).append(" — ").append(r.title).append("\n");
        }

        String model = this.getConfiguration().get("gemini.model");
        if (model == null || model.isEmpty()) model = "gemini-3.1-flash-lite";

        String geminiBaseUrl = "https://generativelanguage.googleapis.com/v1beta/models";
        String url = geminiBaseUrl + "/" + model + ":generateContent";

        String payload = "{"
                + "\"contents\":[{\"parts\":[{\"text\":" + jsonString(prompt.toString()) + "}]}],"
                + "\"generationConfig\":{\"temperature\":0.1,\"maxOutputTokens\":8192}"
                + "}";

        List<String> result = new ArrayList<>();
        try {
            Builder response = callGemini(url, "POST", payload);
            Builders candidates = (Builders) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                logger.warning("No candidates in Gemini response.");
                return result;
            }

            Builder first = candidates.get(0);
            Builder content = (Builder) first.get("content");
            Builders parts = (Builders) content.get("parts");
            String text = parts.get(0).get("text").toString().trim();

            text = text.replaceAll("(?s)^```[a-z]*\\s*", "").replaceAll("```\\s*$", "").trim();

            Builders arr = new Builders();
            arr.parse(text);

            for (Builder item : arr) {
                result.add(item.isSingleValue() ? item.getValue().toString() : item.toString());
            }

        } catch (Exception e) {
            throw new ApplicationException("Gemini generation failed: " + e.getMessage(), e);
        }

        return result;
    }

    private List<SectionRow> loadSections(String language, int bookId) throws ApplicationException {
        List<SectionRow> list = new ArrayList<>();
        bible_section sectionQuery = new bible_section();
        try {
            String where = "WHERE [language]=? AND parallel_ref=''";
            Object[] args;
            if (bookId > 0) {
                where += " AND book_id=?";
                args = new Object[]{language, bookId};
            } else {
                args = new Object[]{language};
            }
            where += " ORDER BY book_id, chapter_id, part_id";

            Table table = sectionQuery.setRequestFields("*").findWith(where, args);
            if (table != null) {
                for (Row row : table) {
                    SectionRow r = new SectionRow();
                    r.bookId = row.getFieldInfo("book_id").intValue();
                    r.chapterId = row.getFieldInfo("chapter_id").intValue();
                    r.partId = row.getFieldInfo("part_id").intValue();
                    r.title = row.getFieldInfo("title").stringValue();
                    list.add(r);
                }
            }
        } catch (Exception e) {
            throw new ApplicationException("Failed to load sections: " + e.getMessage(), e);
        }
        return list;
    }

    private void updateSection(int bookId, int chapterId, int partId, String language, String parallelRef) throws ApplicationException {
        bible_section sectionQuery = new bible_section();
        try {
            Table table = sectionQuery.setRequestFields("*")
                    .findWith("WHERE book_id=? AND chapter_id=? AND part_id=? AND language=?",
                            new Object[]{bookId, chapterId, partId, language});
            if (table != null && table.size() > 0) {
                bible_section sec = new bible_section();
                sec.setData(table.get(0));
                sec.setParallelRef(parallelRef);
                sec.update();
            }
        } catch (Exception e) {
            throw new ApplicationException("Failed to update section: " + e.getMessage(), e);
        }
    }

    private static String jsonString(String s) {
        return "\"" + s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }

    private static class SectionRow {
        int bookId;
        int chapterId;
        int partId;
        String title;
    }
}
