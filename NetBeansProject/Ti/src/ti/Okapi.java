package ti;

import java.util.*;
import java.lang.Math;

/**
 * Implements retrieval in a vector space with the cosine similarity function and a TFxIDF weight formulation.
 */
public class Okapi implements RetrievalModel
{
	public Okapi(){}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<Tuple<Integer, Double>> runQuery(String queryText, Index index, DocumentProcessor docProcessor)
	{
            // extraer términos de la consulta
            ArrayList<String> queryTokens = docProcessor.processText(queryText);
            // calcular el vector consulta
            ArrayList<Tuple<Integer, String>> queryVector = computeVectorOkapiBM25(queryTokens, index);
            // calcular similitud de documentos
            ArrayList<Tuple<Integer, Double>> res = computeScoresOkapiBM25(queryVector, index, 2, 0.75);
                
            return res; // devolver resultados
	}
        
        // --------------------------- Métodos Okapi ---------------------------
        // B y K son los pesos, estaría mejor dedicirlos y declararlos como constante.
        // Para eso hará falta realizar evaluaciones.
        protected ArrayList<Tuple<Integer, Double>> computeScoresOkapiBM25(ArrayList<Tuple<Integer, String>> queryVector, 
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
        protected ArrayList<Tuple<Integer, String>> computeVectorOkapiBM25(ArrayList<String> terms, Index index){
            
            ArrayList<Tuple<Integer, String>> vector = new ArrayList<>();
            HashMap <String,Integer> uniqueTerms = new HashMap<>();
            
            for(int i = 0 ; i < terms.size() ; ++i){
                String term = terms.get(i);
                if(uniqueTerms.containsKey(term) == false )
                    uniqueTerms.put(term, 0);
            }
            
            int id_term;
            for(Map.Entry<String,Integer> e : uniqueTerms.entrySet()){
                Tuple<Integer, Double> termInfo = index.vocabulary.get(e.getKey());
                if(termInfo == null)    continue;
                id_term = termInfo.item1; boolean isRepited = false;					
                
                if(isRepited)   continue;   //Si ya habiamos guardado la informacion de este término no lo guardamos en el vector de resultados
                vector.add(new Tuple(id_term, e.getKey()));
            }
            return vector;
        }
}

