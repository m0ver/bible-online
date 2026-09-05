package custom.tools;

import custom.objects.bible_section;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.DatabaseOperator;
import org.tinystruct.system.annotation.Action;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.*;

/**
 * Imports Bible section headings from USFM files into the bible_section table.
 *
 * <p>Parses all *.usfm files found in the configured directory, extracting:
 * <ul>
 *   <li>{@code \s1} or {@code \s} — section heading title</li>
 *   <li>{@code \r} — parallel reference (e.g., cross-references to other gospels)</li>
 *   <li>{@code \c} — chapter number</li>
 *   <li>{@code \v} — verse number (used as part_id)</li>
 * </ul>
 *
 * <p>Run via command line:
 * <pre>
 *   bin/dispatcher.cmd import/sections
 * </pre>
 */
public class UsfmSectionImporter extends AbstractApplication {

    private static final Logger logger = Logger.getLogger(UsfmSectionImporter.class.getName());

    /** The language tag stored in bible_section for these CUV (Chinese Union Version) files. */
    private static final String LANGUAGE = "zh_CN";

    /** The version tag — 'ALL' means it applies to all translation versions of this language. */
    private static final String VERSION = "ALL";

    /**
     * Maps USFM book abbreviations to the numeric book_id used in the database.
     * IDs follow the standard 1-based OT+NT ordering (Gen=1 ... Rev=66).
     */
    private static final Map<String, Integer> BOOK_IDS = new LinkedHashMap<>();

    static {
        // Old Testament (1-39)
        BOOK_IDS.put("GEN", 1);  BOOK_IDS.put("EXO", 2);  BOOK_IDS.put("LEV", 3);
        BOOK_IDS.put("NUM", 4);  BOOK_IDS.put("DEU", 5);  BOOK_IDS.put("JOS", 6);
        BOOK_IDS.put("JDG", 7);  BOOK_IDS.put("RUT", 8);  BOOK_IDS.put("1SA", 9);
        BOOK_IDS.put("2SA", 10); BOOK_IDS.put("1KI", 11); BOOK_IDS.put("2KI", 12);
        BOOK_IDS.put("1CH", 13); BOOK_IDS.put("2CH", 14); BOOK_IDS.put("EZR", 15);
        BOOK_IDS.put("NEH", 16); BOOK_IDS.put("EST", 17); BOOK_IDS.put("JOB", 18);
        BOOK_IDS.put("PSA", 19); BOOK_IDS.put("PRO", 20); BOOK_IDS.put("ECC", 21);
        BOOK_IDS.put("SNG", 22); BOOK_IDS.put("ISA", 23); BOOK_IDS.put("JER", 24);
        BOOK_IDS.put("LAM", 25); BOOK_IDS.put("EZK", 26); BOOK_IDS.put("DAN", 27);
        BOOK_IDS.put("HOS", 28); BOOK_IDS.put("JOL", 29); BOOK_IDS.put("AMO", 30);
        BOOK_IDS.put("OBA", 31); BOOK_IDS.put("JON", 32); BOOK_IDS.put("MIC", 33);
        BOOK_IDS.put("NAM", 34); BOOK_IDS.put("HAB", 35); BOOK_IDS.put("ZEP", 36);
        BOOK_IDS.put("HAG", 37); BOOK_IDS.put("ZEC", 38); BOOK_IDS.put("MAL", 39);
        // New Testament (40-66)
        BOOK_IDS.put("MAT", 40); BOOK_IDS.put("MRK", 41); BOOK_IDS.put("LUK", 42);
        BOOK_IDS.put("JHN", 43); BOOK_IDS.put("ACT", 44); BOOK_IDS.put("ROM", 45);
        BOOK_IDS.put("1CO", 46); BOOK_IDS.put("2CO", 47); BOOK_IDS.put("GAL", 48);
        BOOK_IDS.put("EPH", 49); BOOK_IDS.put("PHP", 50); BOOK_IDS.put("COL", 51);
        BOOK_IDS.put("1TH", 52); BOOK_IDS.put("2TH", 53); BOOK_IDS.put("1TI", 54);
        BOOK_IDS.put("2TI", 55); BOOK_IDS.put("TIT", 56); BOOK_IDS.put("PHM", 57);
        BOOK_IDS.put("HEB", 58); BOOK_IDS.put("JAS", 59); BOOK_IDS.put("1PE", 60);
        BOOK_IDS.put("2PE", 61); BOOK_IDS.put("1JN", 62); BOOK_IDS.put("2JN", 63);
        BOOK_IDS.put("3JN", 64); BOOK_IDS.put("JUD", 65); BOOK_IDS.put("REV", 66);
    }

    @Override
    public void init() {
    }

    @Override
    public String version() {
        return "1.0";
    }

    /**
     * Main action: clear existing zh_CN sections, then re-import from all USFM files.
     *
     * @return a summary string describing how many records were inserted.
     * @throws ApplicationException on any I/O or database error.
     */
    @Action("import/sections")
    public String importSections() throws ApplicationException {
        // Resolve the USFM directory relative to the working directory
        String usfmDir = "data/cuv/USX_1";
        Path dir = Paths.get(usfmDir);
        if (!Files.isDirectory(dir)) {
            throw new ApplicationException("USFM directory not found: " + dir.toAbsolutePath());
        }

        int totalInserted = 0;

        // Clear existing zh_CN data before re-importing
        try (DatabaseOperator operator = new DatabaseOperator()) {
            operator.disableSafeCheck();
            logger.info("Clearing existing zh_CN sections...");
            operator.execute("DELETE FROM [bible_section] WHERE [language]='" + LANGUAGE + "';");
        } catch (Exception e) {
            throw new ApplicationException("Failed to clear existing data: " + e.getMessage(), e);
        }

        // Process each .usfm file
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.usfm")) {
            List<Path> files = new ArrayList<>();
            stream.forEach(files::add);
            files.sort(Comparator.naturalOrder());

            for (Path file : files) {
                String abbrev = extractBookAbbrev(file.getFileName().toString());
                if (abbrev == null || !BOOK_IDS.containsKey(abbrev)) {
                    logger.warning("Skipping unrecognized file: " + file.getFileName());
                    continue;
                }
                int bookId = BOOK_IDS.get(abbrev);
                int count = parseAndInsert(file, bookId);
                logger.info(abbrev + " (" + bookId + "): inserted " + count + " sections");
                totalInserted += count;
            }
        } catch (IOException e) {
            throw new ApplicationException("Failed to read USFM directory: " + e.getMessage(), e);
        }

        return "SUCCESS: Imported " + totalInserted + " sections from USFM files into bible_section (language=" + LANGUAGE + ").";
    }

    /**
     * Extracts the book abbreviation from a filename like MAT.usfm -> MAT.
     */
    private String extractBookAbbrev(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return null;
        return filename.substring(0, dot).toUpperCase(Locale.ROOT);
    }

    /**
     * Parses a single USFM file and inserts section records.
     *
     * @param file   path to the .usfm file
     * @param bookId numeric database book ID
     * @return number of records inserted
     */
    private int parseAndInsert(Path file, int bookId) throws ApplicationException {
        int currentChapter = 0;
        int currentVerse = 1;
        String pendingTitle = null;
        String pendingRef = null;
        int insertCount = 0;

        // Regex patterns for the markers we care about
        Pattern chapterPat = Pattern.compile("^\\\\c\\s+(\\d+)");
        Pattern versePat   = Pattern.compile("\\\\v\\s+(\\d+)");
        Pattern s1Pat      = Pattern.compile("^\\\\s1?\\s+(.+)");
        Pattern rPat       = Pattern.compile("^\\\\r\\s+(.+)");

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // New chapter: reset state
                Matcher cMatcher = chapterPat.matcher(line);
                if (cMatcher.find()) {
                    pendingTitle = null;
                    pendingRef = null;
                    currentChapter = Integer.parseInt(cMatcher.group(1));
                    currentVerse = 1;
                    continue;
                }

                // Section heading (\s or \s1)
                Matcher sMatcher = s1Pat.matcher(line);
                if (sMatcher.find()) {
                    pendingTitle = cleanUsfm(sMatcher.group(1));
                    pendingRef = "";
                    continue;
                }

                // Parallel reference (\r) -- always follows \s1
                Matcher rMatcher = rPat.matcher(line);
                if (rMatcher.find() && pendingTitle != null) {
                    pendingRef = cleanUsfm(rMatcher.group(1));
                    continue;
                }

                // When we encounter a verse marker and there's a pending section, commit it
                if (pendingTitle != null) {
                    Matcher vMatcher = versePat.matcher(line);
                    if (vMatcher.find()) {
                        currentVerse = Integer.parseInt(vMatcher.group(1));
                        if (currentChapter > 0) {
                            insertSection(bookId, currentChapter, currentVerse, pendingTitle, pendingRef != null ? pendingRef : "");
                            insertCount++;
                        }
                        pendingTitle = null;
                        pendingRef = null;
                    }
                }
            }
        } catch (IOException e) {
            throw new ApplicationException("Failed to read file " + file + ": " + e.getMessage(), e);
        }

        return insertCount;
    }

    /**
     * Strips inline USFM markers (e.g. \pn, \pn*, \+pn, footnote tags) from text,
     * leaving only human-readable content.
     */
    private String cleanUsfm(String raw) {
        if (raw == null) return "";
        // Remove character markers like \pn, \pn*, \+pn, \+pn*, \wj, \wj*, \qt, \qt*
        String cleaned = raw.replaceAll("\\\\\\+?[a-z0-9]+\\*?", "");
        // Remove footnote blocks \f + \fr ... \f*
        cleaned = cleaned.replaceAll("\\\\f.*?\\\\f\\*", "");
        // Collapse whitespace
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    private void insertSection(int bookId, int chapterId, int partId,
                                String title, String parallelRef) throws ApplicationException {
        bible_section section = new bible_section();
        section.setVersion(VERSION);
        section.setLanguage(LANGUAGE);
        section.setBookId(bookId);
        section.setChapterId(chapterId);
        section.setPartId(partId);
        section.setTitle(title);
        section.setParallelRef(parallelRef);
        section.setIsParagraph(1);
        section.append();
    }
}
