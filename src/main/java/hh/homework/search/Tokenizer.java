package hh.homework.search;

/**
 * Created by fib on 08/02/15.
 */
class Tokenizer {
    public String[] execute(final String text){
        if (text == null){
            return new String[0];
        }
        return text.split("\\s+");
    }
}
