package ti;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.jsoup.*;
import org.jsoup.nodes.Document;

/**
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
                String text = doc.body().text();
                String title = doc.getElementsByTag("title").text();
                //System.out.println("Title : "+title);
                //System.out.println("Text:::::::::::"+text);
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
                //tokenize
                Tuple<ArrayList<String> , ArrayList<String> > tokenized  = new Tuple<>(this.tokenize(parsed.item1),this.tokenize(parsed.item2));
                //Normalize
                for(String s : tokenized.item1){
                    terms.add(this.normalize(s));
                }
                for(String s : tokenized.item2){
                    terms.add(this.normalize(s));
                }
                
                //Stopwords
                
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
		ArrayList<String> tokens = new ArrayList<>();
                List<String> wordsSplited = Arrays.asList(text.split("\\s+&[ ]"));  ///COMO TIENE QUE FUNCIONAR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                
                for(String word : wordsSplited){
                    
                    word = word.replaceAll(".","");
                    word = word.replaceAll("[)]","");
                    word = word.replaceAll("[(]","");
                    if(word.length()==1 )continue;
                    tokens.add(word);
                    List<String> wordsWithoutSymbols = Arrays.asList(word.split("-"));
                    if(wordsWithoutSymbols.size() >  1  ){
                        
                        tokens.add(word.replaceAll("-", " "));      //Add the word as the same with spaces
                        tokens.add(word.replaceAll("-", ""));      //Add the word as the same without -
                        
                        for(String wordWithoutSymbols : wordsWithoutSymbols){
                            tokens.add(wordWithoutSymbols);
                        }
                        
                    }
                    
                    
                }
                int tokSize = tokens.size();
                for(int i = 0 ; i < tokSize-1 ;i++){
                    tokens.add( tokens.get(i) + " " + tokens.get(i+1) ) ;
                }
		// P3
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

		// P3
                
                   
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
		boolean isTopWord = stopWords.contains(term);

		// P3
                
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
		String stem = null;

		// P3

		return stem;
	}
}
