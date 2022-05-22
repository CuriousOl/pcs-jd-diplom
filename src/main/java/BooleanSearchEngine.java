import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearchEngine implements SearchEngine {
    Map<String, List<PageEntry>> results = new TreeMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {

        if (!pdfsDir.isDirectory()) return;

        var listFiles = pdfsDir.listFiles();
        if (listFiles == null) {
            System.out.println("Файлов нет");
            return;
        }

        for (File pdf : listFiles) {
            if (pdf.isDirectory()) continue;

            //для файлов:
            var doc = new PdfDocument(new PdfReader(pdf));
            for (int numberPage = 1; numberPage <= doc.getNumberOfPages(); ++numberPage) {

                PdfPage page = doc.getPage(numberPage);
                String text = PdfTextExtractor.getTextFromPage(page);
                String[] words = text.split("\\P{IsAlphabetic}+");

                //подсчитываем частоту повторения каждого слова в тексте текущего файла
                Map<String, Integer> freqs = wordsFrequency(words);

                //добавляем результаты поиска каждого слова в results
                for (String key : freqs.keySet()) {
                    if (results.containsKey(key)) {
                        List<PageEntry> list = new ArrayList<>(results.get(key));
//                        list = results.get(key);
                        list.add(new PageEntry(pdf.getName(), numberPage, freqs.get(key)));
                        results.replace(key, list);
                    } else {
                        results.put(key, List.of(new PageEntry(pdf.getName(), numberPage, freqs.get(key))));
                    }
                }
            }
        }

    }

    public Map<String, Integer> wordsFrequency(String[] words) {
        Map<String, Integer> freqs = new HashMap<>();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            freqs.put(word.toLowerCase(), freqs.getOrDefault(word.toLowerCase(), 0) + 1);
        }
        return freqs;
    }

    public List<PageEntry> search(String word) {

        if (!results.containsKey(word)) {
            return Collections.emptyList();
        }
        return results.get(word).stream()
                .sorted()
                .collect(Collectors.toList());
    }
}
