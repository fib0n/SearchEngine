package hh.homework.search;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fib on 08/02/15.
 */
class Normalizer {
    private final LuceneMorphology russianMorphology;
    private final LuceneMorphology englishMorphology;

    public Normalizer() throws IOException {
        russianMorphology = new RussianLuceneMorphology();
        englishMorphology = new EnglishLuceneMorphology();
    }

    public List<String> execute(String[] words) {
        final List<String> normalForms = new ArrayList<>(words.length);
        for (final String word : words) {
            List<String> nfs = null;
            try {
                if (russianMorphology.checkString(word)) {
                    nfs = russianMorphology.getNormalForms(word);
                } else if (englishMorphology.checkString(word)) {
                    nfs = englishMorphology.getNormalForms(word);
                }
                if (nfs != null && !nfs.isEmpty()) {
                    normalForms.add(nfs.get(0));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return normalForms;
    }
}
