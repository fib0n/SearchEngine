package hh.homework.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by fib on 08/02/15.
 */
class Tokenizer {
    public List<String> execute(final String text){
        if (text == null){
            return new ArrayList<>();
        }
        return Arrays.asList(text.split("\\s+"))
                .stream()
                .filter(t -> t.trim().length() > 1)
                .collect(Collectors.toList());
    }
}
