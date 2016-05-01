package ti;

import java.util.*;
import java.lang.Math;

/**
 * Implements retrieval in a vector space with the cosine similarity function and a TFxIDF weight formulation.
 */
public class Cosine implements RetrievalModel
{
	public Cosine()
	{
		// vacío
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<Tuple<Integer, Double>> runQuery(String queryText, Index index, DocumentProcessor docProcessor)
	{
		// P1
		// extraer términos de la consulta
                ArrayList<String> queryTokens = docProcessor.processText(queryText);
		// calcular el vector consulta
                ArrayList<Tuple<Integer, Double>> queryVector = computeVector(queryTokens, index);
		// calcular similitud de documentos
                ArrayList<Tuple<Integer, Double>> res = computeScores(queryVector, index);
		return res; // devolver resultados
	}

	/**
	 * Returns the list of documents in the specified index sorted by similarity with the specified query vector.
	 *
	 * @param queryVector the vector with query term weights.
	 * @param index       the index to search in.
	 * @return a list of {@link Tuple}s where the first item is the {@code docID} and the second one the similarity score.
	 */
	protected ArrayList<Tuple<Integer, Double>> computeScores(ArrayList<Tuple<Integer, Double>> queryVector, Index index)
	{
		ArrayList<Tuple<Integer, Double>> results = new ArrayList<>();
		HashMap<Integer,Double> res  = new HashMap<Integer,Double>();
                
		double normWtq = 0.0;
		for(Tuple<Integer,Double> qw : queryVector)
			normWtq += qw.item2 * qw.item2;
                normWtq = Math.sqrt(normWtq);
                
                JaccardScore(queryVector, index, normWtq, res);
                
		for(Map.Entry<Integer,Double> e : res.entrySet() ){
			results.add(new Tuple<Integer, Double>(e.getKey(),e.getValue()));
		}
		
		// Ordenar documentos por similitud y devolver
		Collections.sort(results, new Comparator<Tuple<Integer, Double>>()
		{
			@Override
			public int compare(Tuple<Integer, Double> o1, Tuple<Integer, Double> o2)
			{
				return o2.item2.compareTo(o1.item2);
			}
		});
		return results;
	}
        
        
        // Método que hace uso del cálculo de la similitud por defecto.
        void similitudNormal(ArrayList<Tuple<Integer, Double>> queryVector, Index index, double normWtq, HashMap<Integer,Double> res){
            for(Tuple<Integer,Double> qw : queryVector){//for each query word
                for(Tuple<Integer, Double> docsW : index.invertedIndex.get(qw.item1)) {
                    if(res.containsKey(docsW.item1) == false)
                        res.put(docsW.item1, 0.0);
                    res.replace(docsW.item1,  res.get(docsW.item1) + (((docsW.item2) * (qw.item2))/((index.documents.get(docsW.item1).item2)*(normWtq))));  
                }
            }          
        }
        
        // Método Dice
        // En teoría o en el enlace inferior.
        // https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient Mitad de la página. (Valor absoluto no es necesario en nuestro caso)
        void DiceScore(ArrayList<Tuple<Integer, Double>> queryVector, Index index, double normWtq, HashMap<Integer,Double> res){
            for(Tuple<Integer,Double> qw : queryVector){//for each query word
                for(Tuple<Integer, Double> docsW : index.invertedIndex.get(qw.item1)) {
                    if(res.containsKey(docsW.item1) == false)
                        res.put(docsW.item1, 0.0);
                    res.replace(docsW.item1,  res.get(docsW.item1) + (2 * (docsW.item2) * (qw.item2))
                    /(Math.pow(index.documents.get(docsW.item1).item2, 2) + Math.pow(normWtq, 2)));
                }
            }  
        }
        
        // Método de Jaccard
        // En teoría o en enlace inferior.
        // https://en.wikipedia.org/wiki/Jaccard_index  Al final de la página.
        void JaccardScore(ArrayList<Tuple<Integer, Double>> queryVector, Index index, double normWtq, HashMap<Integer,Double> res){
            for(Tuple<Integer,Double> qw : queryVector){//for each query word
                for(Tuple<Integer, Double> docsW : index.invertedIndex.get(qw.item1)) {
                    if(res.containsKey(docsW.item1) == false)
                        res.put(docsW.item1, 0.0);
                    res.replace(docsW.item1,  res.get(docsW.item1) + ((docsW.item2) * (qw.item2)));
                }
            }
            for(Integer key : res.keySet())
                res.replace(key, (res.get(key))/(Math.pow(index.documents.get(key).item2, 2) + Math.pow(normWtq, 2) - res.get(key)));
        }
        
        
	/**
	 * Compute the vector of weights for the specified list of terms.
	 *
	 * @param terms the list of terms.
	 * @param index the index
	 * @return a list of {@code Tuple}s with the {@code termID} as first item and the weight as second one.
	 */
	protected ArrayList<Tuple<Integer, Double>> computeVector(ArrayList<String> terms, Index index)
	{
		ArrayList<Tuple<Integer, Double>> vector = new ArrayList<>();
                //ftq : apariciones palabra en query
                //nd : numero de documentos totales
                //ct : numero documentos donde aparece la palabra
                //tftq : indicador frecuancia aparicion del termino , suavizando en el numero de apariciones
                //idft : indicador que penaliza la aparicion de una palabra en una gran cantidad de documentos
                //wtq : peso de una palabra en la query
                
                Double idft, tftq;
                int id_term, ftq;
        	for(int i = 0 ; i < terms.size() ; ++i){
                    Tuple<Integer, Double> termInfo = index.vocabulary.get(terms.get(i));
                    id_term = termInfo.item1; 
                    idft = termInfo.item2;
                    ftq = 0;
                    boolean isRepited = false;					
                    for(int j = 0 ; j < terms.size() ; ++j){
                        if(terms.get(i).compareTo(terms.get(j)) == 0){
                            ftq++;
                            if(j < i){
                                isRepited = true;
                                break;
                            }
                        }
                    }
                    if(isRepited)continue;			//Si ya habiamos guardado la informacion de este termino no lo guardamos en el vector de resultados
                    tftq = 1 + Math.log(ftq);
                    vector.add(new Tuple<Integer, Double>(id_term,idft*tftq));
		}
                
		return vector;
	}
        
        // Método Okami
        //B y K son los pesos, estaría mejor dedicirlos y declararlos como constante. Para eso hará falta realizar evaluaciones.
        protected ArrayList<Tuple<Integer, Double>> computeScoresOkamiBM25(ArrayList<Tuple<Integer, Tuple<Double, String>>> queryVector, 
                Index index, int k, int b){
            
            ArrayList<Tuple<Integer, Double>> results = new ArrayList<>();
            // P1
            
            HashMap<Integer, Double> res  = new HashMap<Integer, Double>();
            double normWtq = 0.0;
            for(Tuple<Integer,Tuple<Double, String>> qw : queryVector)
                normWtq += qw.item2.item1 * qw.item2.item1;
            normWtq = Math.sqrt(normWtq);
            
            for(Tuple<Integer, Tuple<Double, String>> qw : queryVector){//for each query word
                for(Tuple<Integer, Double> docsW : index.invertedIndex.get(qw.item1)) {
                    if(res.containsKey(docsW.item1) == false)
                        res.put(docsW.item1, 0.0);
                                
                                int ct =  index.invertedIndex.get(qw.item1).size();
                                double idf = Math.log((index.documents.size() - ct + 0.5)/(ct + 0.5));
                                
                                // docsW.item2 es Wtd y index.vocab es idft, así se obtiene tftd
                                double tftd = docsW.item2/index.vocabulary.get(qw.item2.item2).item2;
                                
                                
                                
                                //Falta obtener la longitud del documento y la media de todos ellos.
                                // Para eso se podría escribir al final del load del índice un bucle que recorra
                                // todo invertedIndex. Documentos pasaria a ser Tuple<String, Tuple<Double, Integer>>.
                                //donde int seria el numero de palabras y double la norma.
                                // Habria que usar los pesos y dividirlos entre idft del termino para encontrar tftd y a partir de ahi ftd
                                
                                //double termSup = (k+1) * tftd / (tftd + k * (1 - b + b * (docsW)));
                              
                        }
		}
		for(Map.Entry<Integer,Double> e : res.entrySet() ){
			results.add(new Tuple<Integer, Double>(e.getKey(),e.getValue()));
		}
		
		// Ordenar documentos por similitud y devolver
		Collections.sort(results, new Comparator<Tuple<Integer, Double>>()
		{
			@Override
			public int compare(Tuple<Integer, Double> o1, Tuple<Integer, Double> o2)
			{
				return o2.item2.compareTo(o1.item2);
			}
		});
		return results;
        }
        
        
        // Los valores de este vector son: -Integer: TermId. -Tupla: -Double: Peso termino query. -String: Término.
        protected  ArrayList<Tuple<Integer, Tuple<Double, String>>> computeVectorOkami(ArrayList<String> terms, Index index){
            ArrayList<Tuple<Integer, Tuple<Double, String>>> vector = new ArrayList<>();
            
            for(int i = 0 ; i < terms.size() ; ++i){
                Tuple<Integer, Double> termInfo = index.vocabulary.get(terms.get(i));
                Double idft = termInfo.item2;   int id_term = termInfo.item1;
                int ftq = 0;    boolean isRepited = false;					
                
                for(int j = 0 ; j < terms.size() ; ++j){
                    if(terms.get(i).compareTo(terms.get(j)) == 0){
                        ftq++;
                        if(j < i){
                            isRepited = true;
                            break;
                        }
                    }
                }
                if(isRepited)continue;	//Si ya habiamos guardado la informacion de este término no lo guardamos en el vector de resultados
                Double tftq = 1 + Math.log(ftq);
                vector.add(new Tuple<Integer, Tuple<Double, String>>(id_term, new Tuple(idft*tftq, terms.get(i))));
            }
            return vector;
        }
}
