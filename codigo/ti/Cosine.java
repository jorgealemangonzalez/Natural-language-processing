package ti;

import java.util.*;
import java.lang.Math;

/**
 * Implements retrieval in a vector space with the cosine similarity function and a TFxIDF weight formulation.
 */
public class Cosine implements RetrievalModel
{
	public Cosine(){}

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
            
            /* // Método de Okami
            // extraer términos de la consulta
            ArrayList<String> queryTokens = docProcessor.processText(queryText);
            // calcular el vector consulta
            ArrayList<Tuple<Integer, String>> queryVector = computeVectorOkamiBM25(queryTokens, index);
            // calcular similitud de documentos
            ArrayList<Tuple<Integer, Double>> res = computeScoresOkamiBM25(queryVector, index, 2, 0.75);
            */
                
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
                
            similitudNormal(queryVector, index, normWtq, res);
            for(Map.Entry<Integer,Double> e : res.entrySet() ){
                results.add(new Tuple<Integer, Double>(e.getKey(),e.getValue()));
            }

            // Ordenar documentos por similitud y devolver
            Collections.sort(results, new Comparator<Tuple<Integer, Double>>(){
                @Override
                public int compare(Tuple<Integer, Double> o1, Tuple<Integer, Double> o2){
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
            HashMap <String,Integer> ftqAll = new HashMap<>();
            //ftq : apariciones palabra en query
            //nd : numero de documentos totales
            //ct : numero documentos donde aparece la palabra
            //tftq : indicador frecuancia aparicion del termino , suavizando en el numero de apariciones
            //idft : indicador que penaliza la aparicion de una palabra en una gran cantidad de documentos
            //wtq : peso de una palabra en la query
            
            Double idft, tftq;
            int id_term, ftq;
            for(int i = 0 ; i < terms.size() ; ++i){
                String term = terms.get(i);
                if(ftqAll.containsKey(term) == false )
                    ftqAll.put(term,1);
                else
                    ftqAll.replace(term, ftqAll.get(term)+1);
            }
            
            for(Map.Entry<String,Integer> e : ftqAll.entrySet() ){
                Tuple<Integer, Double> termInfo = index.vocabulary.get(e.getKey());
                id_term = termInfo.item1; idft = termInfo.item2;
                tftq = 1 + Math.log(e.getValue());
                
                vector.add(new Tuple<Integer, Double>(id_term,idft*tftq));
            }
            return vector;
	}
        
        // --------------------------- Métodos Okami ---------------------------
        
        // B y K son los pesos, estaría mejor dedicirlos y declararlos como constante.
        // Para eso hará falta realizar evaluaciones.
        protected ArrayList<Tuple<Integer, Double>> computeScoresOkamiBM25(ArrayList<Tuple<Integer, String>> queryVector, 
                Index index, double k, double b){
            
            ArrayList<Tuple<Integer, Double>> results = new ArrayList<>();
            // P1
            
            HashMap<Integer, Double> res  = new HashMap<Integer, Double>();
            
            double avgLength = 0.0;
            for(Integer length : index.documentsOkapi.values())
                avgLength += length;
            avgLength = avgLength/index.documentsOkapi.size();
            
            for(Tuple<Integer, String> qw : queryVector){//for each query word
                for(Tuple<Integer, Double> docsW : index.invertedIndex.get(qw.item1)) {
                    if(res.containsKey(docsW.item1) == false)
                        res.put(docsW.item1, 0.0);
                                
                                int ct =  index.invertedIndex.get(qw.item1).size();
                                
                                // Esta función da valores negativos si el término aparece en más de la mitad de los documentos
                                double idf = Math.log((index.documents.size() - ct + 0.5)/(ct + 0.5));
                                
                                // Si ocurre el caso anterior
                                //double idf = Math.log(index.documents.size()/ct);
                                
                                // docsW.item2 es Wtd y index.vocab es idft, así se obtiene tftd
                                double tftd = docsW.item2/index.vocabulary.get(qw.item2).item2;
                                
                                double termSup = ((k+1) * tftd)/(tftd + k * ((1 - b) + b * (index.documentsOkapi.get(docsW.item1)/avgLength)));
                                res.replace(docsW.item1, res.get(docsW.item1) + termSup * idf);
                                int pl;
                                if(res.get(docsW.item1) < 0)
                                    pl = 0;
                                    
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
        
        // Los valores de este vector son: -Integer: TermId. -String: Término.
        protected ArrayList<Tuple<Integer, String>> computeVectorOkamiBM25(ArrayList<String> terms, Index index){
            
            ArrayList<Tuple<Integer, String>> vector = new ArrayList<>();
            for(int i = 0 ; i < terms.size() ; ++i){
                Tuple<Integer, Double> termInfo = index.vocabulary.get(terms.get(i));
                int id_term = termInfo.item1; boolean isRepited = false;					
                
                for(int j = 0 ; j < terms.size() ; ++j){
                    if(terms.get(i).compareTo(terms.get(j)) == 0){
                        if(j < i){
                            isRepited = true;
                            break;
                        }
                    }
                }
                if(isRepited)continue;	//Si ya habiamos guardado la informacion de este término no lo guardamos en el vector de resultados
                vector.add(new Tuple(id_term, terms.get(i)));
            }
            return vector;
        }
}
