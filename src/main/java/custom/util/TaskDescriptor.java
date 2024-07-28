/*******************************************************************************
 * Copyright  (c) 2013 Mover Zhou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package custom.util;

import custom.util.model.Book;
import custom.util.model.Chapter;
import custom.util.model.Section;
import org.tinystruct.ApplicationException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TaskDescriptor {
    private String value;
    private List<Book> books;

    public TaskDescriptor() {
        this.books = new ArrayList<Book>();
    }

    public static void main(String[] args) throws ApplicationException {
//		String s="1^1";
//		String s="1^1-3";
//		String s="1^1,3";
//		String s="1^1:1";
//		String s="1^1:1-10";
//		String s="1^1:1-3:1";

        String s = "19^58;41^8;12^18-19";

        TaskDescriptor task = new TaskDescriptor();
        System.out.println(task.parse(s));
    }

    public StringBuffer parse(String value) throws ApplicationException {
        this.value = value;

        StringBuffer buffer = new StringBuffer();
        if (this.value.indexOf(";") != -1) {
            String[] books = this.value.split(";");
            for (int i = 0; i < books.length; i++) {
                this.books.add(parseBook(books[i]));
            }

            Iterator<Book> iterator = this.books.iterator();
            while (iterator.hasNext()) {
                if (buffer.length() == 0) {
                    buffer.append(iterator.next().toSQL());
                } else {
                    buffer.append(" or ").append(iterator.next().toSQL());
                }
            }
        } else if (this.value.indexOf("^") != -1) {
            buffer.append(parseBook(value).toSQL());
        }


        return buffer;
    }

    public Book parseBook(String value) throws ApplicationException {
        if (value.indexOf("^") == -1) {
            throw new ApplicationException("Invalid format value:" + value);
        }

        String[] bookInfo = value.split("\\^", 2);

        String bookId = bookInfo[0];
        Book book = new Book(Integer.parseInt(bookId));

        List<Chapter> chapters = this.parseChapter(book, bookInfo[1]);
        for (Chapter chapter : chapters) {
            book.newChapter(chapter);
        }
        return book;
    }

    private List<Chapter> parseChapter(Book book, String value) {
        // TODO Auto-generated method stub
        List<Chapter> chapters = new ArrayList<Chapter>();

        if (value.indexOf(',') != -1) {
            String[] chapter_id = value.split(",");

            for (String s : chapter_id) {
                Chapter chapter = new Chapter(Integer.parseInt(s));
                chapters.add(chapter);
            }
        } else if (value.indexOf('-') != -1) {
            String[] chapterInfo = value.split("-");

            if (chapterInfo[0].indexOf(':') == -1 && chapterInfo[1].indexOf(':') == -1) {
                book.fromNode(new Chapter(Integer.parseInt(chapterInfo[0])));
                book.toNode(new Chapter(Integer.parseInt(chapterInfo[1])));
            } else if (chapterInfo[0].indexOf(':') != -1 && chapterInfo[1].indexOf(':') == -1) {
                String[] info = chapterInfo[0].split(":");
                Chapter chapter = new Chapter(Integer.parseInt(info[0]));

                chapter.fromNode(new Section(Integer.parseInt(info[1])));
                chapter.toNode(new Section(Integer.parseInt(chapterInfo[1])));

                book.newChapter(chapter);
            } else if (chapterInfo[0].indexOf(':') == -1 && chapterInfo[1].indexOf(':') != -1) {
                book.fromNode(new Chapter(Integer.parseInt(chapterInfo[0])));

                String[] info = chapterInfo[1].split(":");

                Section section = new Section(Integer.parseInt(info[1]));
                Chapter chapter = new Chapter(Integer.parseInt(info[0]));
                chapter.newSection(section);

                book.toNode(chapter);
            } else if (chapterInfo[0].indexOf(':') != -1 && chapterInfo[1].indexOf(':') != -1) {
                String[] info = chapterInfo[0].split(":");
                Chapter chapter1 = new Chapter(Integer.parseInt(info[0]));
                Section section1 = new Section(Integer.parseInt(info[1]));
                chapter1.newSection(section1);

                String[] info1 = chapterInfo[1].split(":");
                Chapter chapter2 = new Chapter(Integer.parseInt(info1[0]));
                Section section2 = new Section(Integer.parseInt(info1[1]));
                chapter2.newSection(section2);

                book.fromNode(chapter1);
                book.toNode(chapter2);
            }

        } else if (value.indexOf(':') != -1) {
            String[] chapterInfo = value.split(":");

            Chapter chapter = new Chapter(Integer.parseInt(chapterInfo[0]));
            Section section = new Section(Integer.parseInt(chapterInfo[1]));

            chapter.newSection(section);

            chapters.add(chapter);
        } else {
            Chapter chapter = new Chapter(Integer.parseInt(value));

            chapters.add(chapter);
        }

        return chapters;
    }

    enum Model {
        NONE,
        CHAPTER_CHAPTER,
        CHAPTER_CHAPTER_SECTION,
        CHAPTER_SECTION_SECTION,
        CHAPTER_SECTION_CHAPTER_SECTION,
    }
}
