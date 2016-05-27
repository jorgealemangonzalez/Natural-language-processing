package ti;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
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
                
                String word ;
                BufferedReader br = null;
                br = new BufferedReader(new FileReader(pathToStopWords.getAbsolutePath()));
                while((word = br.readLine()) != null){
                    stopWords.add(word);
                    //System.out.println(word);
                }
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

            String title = doc.getElementsByTag("title").text();
            String text = body.text();
            
            //System.out.println(text);
             
            //Whitelist whitelist = Whitelist.basic();
            //String text2 = Jsoup.clean(doc.html(), whitelist);
            //System.out.println(text2);
               
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
                    String norm = this.normalize(s);
                    if(norm.length() > 1)
                        termsNormalized.add(norm);
                }
                for(String s : tokenized.item2){
                    String norm = this.normalize(s);
                    if(norm.length() > 1)
                        termsNormalized.add(norm);
                }
                //Stopwords y stemmer
                for(int i = 0 ; i < termsNormalized.size() ; ++i){
                    if(!isStopWord(termsNormalized.get(i))){
                        String stemmed = stem(termsNormalized.get(i));
                        terms.add(stemmed);
                    }
                }
                
                //add pairs
                /*
                Integer terSize =new Integer(terms.size()) ;
                for(int i = 0 ; i < terSize-1 ;i++){
                    terms.add( terms.get(i) + " " + terms.get(i+1) ) ;
                }
                */
                //System.out.println(terms);
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

            // Si es una página web https.
            if(text.contains("https")){
                text = text.replace("https://","");
                //System.out.println(text);
            }            
            // Si es una página web http.
            if(text.contains("http")){
                text = text.replace("http://","");
                //System.out.println(text);
            }
            // Si contiene www.
            if(text.contains("www.")){
                text = text.replace("www.","");
                text = text.replace("."," ");
                text = text.replace("/"," ");
                //System.out.println(text);
            }
            
            // Remplazamos / y \ por espacios y obtenemos las palabras.
            List<String> wordsSplited = Arrays.asList(text.replaceAll("[/\\\\]", " ").split("\\s+"));
            
            for(String word : wordsSplited){
                word = Normalizer.normalize(word, Normalizer.Form.NFD); // Normalizamos los carácteres y los pasamos todos a ASCII.
                word = word.replaceAll("[^\\p{ASCII}]", "");

                tokens.add(word);
                // Si tenemos palabras con guión las obtenemos de otra forma.
                List<String> wordsWithoutSymbols = Arrays.asList(word.split("-"));
                if(wordsWithoutSymbols.size() >  1  ){
                    //total 30000 palabras mas al añadir esto

                    tokens.add(word.replace("-", " "));      //Add the word as the same with spaces
                    tokens.add(word.replace("-", ""));      //Add the word as the same without -

                    for(String wordWithoutSymbols : wordsWithoutSymbols)
                        tokens.add(wordWithoutSymbols);   
                }
            }
            return tokens;
    }

	/**
	 * Normalize the given term.
	 *
	 * @param text the term to normalize.
	 * @return the normalized term.
	 */
	protected String normalize(String text)
	{
            String normalized = null;

            // Transformamos las palabras a minúsculas.
            if(text.matches(".*\\d+.*")) // Si es un número.
                normalized = text.toLowerCase().replaceAll("[^a-z0-9 .+=*:/@]", ""); // Reemplazamos todo lo que no sea.
            else
                normalized = text.toLowerCase().replaceAll("[^a-z +#@-]", "");
            
            return normalized;
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
            boolean isTopWord = stopWords.contains(term);
            return isTopWord;
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
            String stem = null;
            Stemmer stemer = new Stemmer();
            char[] w = new char[term.length()];
            term.getChars(0, term.length(), w, 0);
            for(int i = 0 ; i < term.length() ; ++i){
                stemer.add(w[i]);
            }
            
            stemer.stem();
            stem = stemer.toString();

            //System.out.println(stem);
            return stem;
	}
}
