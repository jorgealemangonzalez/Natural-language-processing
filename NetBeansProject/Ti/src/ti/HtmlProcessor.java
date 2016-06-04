package ti;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/*
 * A processor to extract terms from HTML documents.
 */
public class HtmlProcessor implements DocumentProcessor
{

    // P3
    HashSet<String> stopWords;
    /**
     * Creates a new HTML processor.
     *
     * @param pathToStopWords the path to the file with stopwords, or {@code null} if stopwords are not filtered.
     * @throws IOException if an error occurs while reading stopwords.
     */
    public HtmlProcessor(File pathToStopWords) throws IOException
    {
        // P3
        // cargar stopwords
        stopWords = new HashSet<String>();
        if(pathToStopWords == null)return;

        String word;
        BufferedReader br = new BufferedReader(new FileReader(pathToStopWords.getAbsolutePath()));
        while((word = br.readLine()) != null)
            stopWords.add(word);
    }

    /**
     * {@inheritDoc}
     */
    public Tuple<String, String> parse(String html)
    {
        // P3
        // parsear documento
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        if(body == null)return null;

        String title = doc.title();
        String text = body.text();

        return new Tuple<String,String>(title,text); // devolver título y body por separado
    }

    /**
     * Process the given text (tokenize, normalize, filter stopwords and stemize) and return the list of terms to index.
     *
     * @param text the text to process.
     * @return the list of index terms.
     */
    public ArrayList<String> processText(String text)
    {
        ArrayList<String> terms = new ArrayList<>();
        // P3
        // tokenizar, normalizar, stopword, stem, etc.
        //parse
        Tuple<String, String> parsed = this.parse(text);
        if(parsed == null)return null;

        //tokenize
        Tuple<ArrayList<String> , ArrayList<String> > tokenized  = new Tuple<>(this.tokenize(parsed.item1),this.tokenize(parsed.item2));

        //Normalize
        ArrayList<String> termsNormalized = new ArrayList<>();
        for(String s : tokenized.item1){
            Tuple<String, String> normTerms = this.normalize(s);
            if(normTerms.item1.length() > 1)
                termsNormalized.add(normTerms.item1);
            if(normTerms.item2.length() > 1)
                termsNormalized.add(normTerms.item2);
        }
        for(String s : tokenized.item2){
            Tuple<String, String> normTerms = this.normalize(s);
            if(normTerms.item1.length() > 1)
                termsNormalized.add(normTerms.item1);
            if(normTerms.item2.length() > 1)
                termsNormalized.add(normTerms.item2);
        }
        //Stopwords y stemmer
        for(int i = 0 ; i < termsNormalized.size() ; ++i){
            if(isStopWord(termsNormalized.get(i)) == false){
                String stemmed = stem(termsNormalized.get(i));
                terms.add(stemmed);
            }
        }
        
        
        // Estaría bien dejarlo activado, pasamos a tener un DNG de 71'5 %, AP 46 %, RR: 66 %
        // En comparación a DNG 68 %, AP: 41 % y RR: 59 %
        //add pairs
        /*
        int terSize = terms.size();
        for(int i = 0 ; i < terSize-1 ;i++){
            terms.add( terms.get(i) + " " + terms.get(i+1) ) ;
        }*/
        
        
        return terms;
    }

    /**
     * Tokenize the given text.
     *
     * @param text the text to tokenize.
     * @return the list of tokens.
     */
    protected ArrayList<String> tokenize(String text)
    {
        // P3
        ArrayList<String> tokens = new ArrayList<>();
        
        // Eliminamos puntos, comillas, símbolos varios y carácteres no ascii.
        List<String> wordsSplited = Arrays.asList(text.replaceAll("[^A-Za-z0-9'’:/*+=-@.]", " ").split("\\s+"));
        for(String word : wordsSplited){
            
            // Ejemplo parseo email.
            //word = "nom.estudiant.upf@hotmail.com";
            // Ejemplo parseo web.
            //word = "http://youtube.com";
            //word = "https://www.youtube.com.de.fr.org";
            // Ejemplo parseo puntos.
            //word = "www.upf.edu.org.j.p.k-d.es";
            // Ejemplo hyphen
            //word = "prueba-hyphen";
            
            // Mirar de parsear cosas como web2.0 a web2.0 web y 2.0.
            
            // Primero añadimos la palabra completa. Si no es valida será normalizada después.
            tokens.add(word);
            
            // Ahora obtenemos otras posibles palabras a partir de la dada.
            // Parsea URLS
            if(parseWebsites(tokens, word));
            else if(parseEmails(tokens, word));
            else{
                if(!parseWordsWithPoints(tokens, word)); // Si no contenia no habremos realizado ninguna comprovación de guiones.
                    parseHyphenWords(tokens, word);
            } 
        }
        
        return tokens;
    }
    
    // Parsea cosas tipo http:// o https://
    // Dado https://www.youtube.com.de.fr.org ->
    // Con add combinaciones y el primer substring obtenemos:
    // www.youtube.com.de.fr.org, youtube.com.de.fr.org, com.de.fr.org, de.fr.org,
    // fr.org, org, 
    // Posteriormente con el bucle for:
    // www, youtube, com, de, fr, org
    protected boolean parseWebsites(ArrayList<String> tokens, String word){
        
        if(word.contains("http://")){
            word = word.substring(7, word.length());
            tokens.add(word);
            
            addCombinaciones(tokens, word);
            
            List<String> splitedWebpage = Arrays.asList(word.replaceAll("[./:]", " ").split("\\s+"));
            for(String webWord : splitedWebpage){           
                tokens.add(webWord);
                parseHyphenWords(tokens, webWord);
            }
            return true;
        }
        else if(word.contains("https://")){
            word = word.substring(8, word.length());
            tokens.add(word);
            
            addCombinaciones(tokens, word);
            
            List<String> splitedWebpage = Arrays.asList(word.replaceAll("[./:]", " ").split("\\s+"));
            for(String webWord : splitedWebpage){           
                tokens.add(webWord);
                parseHyphenWords(tokens, webWord);
            }
            return true;
        }
        return false;
    }
    
    // Parsea emails.
    // nom.estudiant.upf@hotmail.com -> 
    // En el primer bucle for obtenemos: 
    // nom.estudiant.upf@hotmail.com, nom.estudiant.upf, hotmail.com,
    // estudiant.upf, upf, com.
    // En el segundo:
    // nom, estudiant, upf, hotmail, com
    // Aqui sacamos los puntos o de otra forma si tuvieran números al normalizar perderíamos datos.
    protected boolean parseEmails(ArrayList<String> tokens, String word){
        
        if(word.contains("@")){
            List<String> splitedWebpage = Arrays.asList(word.replace("@", " ").split("\\s+"));
            for(String webWord : splitedWebpage){
                tokens.add(webWord);
                addCombinaciones(tokens, webWord);
                parseHyphenWords(tokens,webWord);
            }
            
            splitedWebpage = Arrays.asList(word.replace("@", " ").replace(".", " ").split("\\s+"));
            for(String webWord : splitedWebpage){
                tokens.add(webWord);
                parseHyphenWords(tokens,webWord);
            }
            
            return true;
        }
        
        return false;
    }
    
    // Dado www.upf.edu.org -> www.upf.edu.org, upf.edu.org, edu.org, org
    protected void addCombinaciones(ArrayList<String> tokens, String word){
        
        String aux = word;
        int pointPos = aux.indexOf(".");
        while(pointPos != -1){
            aux = aux.substring(aux.indexOf(".")+1, aux.length());
            tokens.add(aux);
            pointPos = aux.indexOf(".");
        }
    }
    
    // Parsea palabras con puntos. www.upf.edu.com -> www, upf, edu, com, también devuelve los resultado de hyphens si hay.
    protected boolean  parseWordsWithPoints(ArrayList<String> tokens, String word){
        
        if(word.contains(".")){ // Parsea palabras con punto tipo www.web.com o hotmail.com cogemos www web y com o hotmail y com
            List<String> splitedWebpage = Arrays.asList(word.replace(".", " ").split("\\s+"));
            for(String webWord : splitedWebpage){
                tokens.add(webWord);
                parseHyphenWords(tokens, webWord);
            }
            return true;
        }

        return false;
    }
    
    // Parsea palabras con guion. prueba1-prueba2 -> prueba1-prueba2, prueba1 prueba2, prueba1prueba2, prueba1, prueba2
    protected void parseHyphenWords(ArrayList<String> tokens, String word){
        
        // Si tenemos palabras con guión las obtenemos de otra forma.
        List<String> wordsWithoutSymbols = Arrays.asList(word.split("-"));
        if(wordsWithoutSymbols.size() >  1  ){
            //total 30000 palabras mas al añadir esto
            
            // Creo que este no es necesario ya teniendo en cuenta que se normaliza. Comprovar antes de eliminar.
            tokens.add(word.replace("-", " "));      //Add the word as the same with spaces
            
            
            tokens.add(word.replace("-", ""));      //Add the word as the same without -

            for(String wordWithoutSymbols : wordsWithoutSymbols)
                tokens.add(wordWithoutSymbols);   
        }
    }

    /**
     * Normalize the given term.
     *
     * @param text the term to normalize.
     * @return the normalized term.
     */
    protected Tuple<String,String> normalize(String text)
    {

        String normalized = "";

        if(!text.matches("[^a-zA-Z0-9]")){ // Si contiene números o letras. Filtra cosas tipo: #--~//// o --
            // Transformamos las palabras a minúsculas.
            if(text.matches(".*\\d+.*")){ // Si contiene un número dentro.
                if(normalized.endsWith(".")) // Le quitamos el último punto si lo tiene.
                    normalized = normalized.substring(0, normalized.length()-1);
                }
            else
                normalized = text.replaceAll("[^A-za-z-@]", "");
            }
        //System.out.println(normalized + " " + normalized.toLowerCase());

        // Añadimos su versión en mayúsucula. Así podemos dar algo más de importancia a York ciudad que york jamon por e.j.
        return new Tuple<String, String>(normalized, normalized.toLowerCase());
    }

    /**
     * Checks whether the given term is a stopword.
     *
     * @param term the term to check.
     * @return {@code true} if the term is a stopword and {@code false} otherwise.
     */
    protected boolean isStopWord(String term)
    {
        // P3
        return stopWords.contains(term);
    }

    /**
     * Stem the given term.
     *
     * @param term the term to stem.
     * @return the stem of the term.
     */
    protected String stem(String term)
    {
        // P3	
        Stemmer stemer = new Stemmer();
        char[] w = new char[term.length()];
        term.getChars(0, term.length(), w, 0);
        for(int i = 0 ; i < term.length() ; ++i){
            stemer.add(w[i]);
        }

        stemer.stem();
        String stem = stemer.toString();

        return stem;
    }
}
