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

		// P1
		HashMap<Integer,Double> res  = new HashMap<Integer,Double>();
		double normWtq = 0.0;
		for(Tuple<Integer,Double> qw : queryVector){
			normWtq += Math.sqrt(qw.item2 * qw.item2); // Si no me equivoco esto no es correcto, la suma de raizes no es lo mismo que la raiz de una suma. 2² + 2² = 8, (2+2)² = 16
		}
		for(Tuple<Integer,Double> qw : queryVector){//for each query word
			for(Tuple<Integer, Double> docsW : index.invertedIndex.get(qw.item1)) {
				if(res.get(docsW.item1) == null){ // Sería mejor cambiarlo por containsKey que hace lo mismo y retorna un booleano.
					res.put(docsW.item1,0.0);
				}
				res.replace(docsW.item1,  res.get(docsW.item1) + ((docsW.item2) * (qw.item2))/((index.documents.get(docsW.item1).item2 )*( normWtq )) );
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
		// P1
                terms.clear();
                terms.add("Prueba");
                terms.add("Prueba");
                terms.add("Prueba");
                terms.add("Prueba");
                terms.add("Prueba2");
		for(int i = 0 ; i < terms.size() ; ++i){
			//Tuple<Integer, Double> termInfo = index.vocabulary.get(terms.get(i));
			//Double idft = termInfo.item2;
			//int id_term = termInfo.item1;
			int ftq = 0;
			boolean isRepited = false;					
			for(int j = 0 ; j < terms.size() ; ++j){
				if(terms.get(i) == terms.get(j)){
					ftq++;
					if(j < i){ // Antes era j > i
						isRepited = true;
						break;
					}
				}
			}
			if(isRepited)continue;			//Si ya habiamos guardado la informacion de este termino no lo guardamos en el vector de resultados
			Double tftq = 1 + Math.log(ftq);
			//vector.add(new Tuple<Integer, Double>(id_term,idft*tftq));
		}
		return vector;
	}
}

/*	protected ArrayList<Tuple<Integer, Double>> computeVector(ArrayList<String> terms, Index index)
	{
		ArrayList<Tuple<Integer, Double>> vector = new ArrayList<>();
                //ftq : apariciones palabra en query
                //nd : numero de documentos totales
                //ct : numero documentos donde aparece la palabra
                //tftq : indicador frecuancia aparicion del termino , suavizando en el numero de apariciones
                //idft : indicador que penaliza la aparicion de una palabra en una gran cantidad de documentos
                //wtq : peso de una palabra en la query
		// P1
		for(int i = 0 ; i < terms.size() ; ++i){
			Tuple<Integer, Double> termInfo = index.vocabulary.get(terms.get(i));
			Double idft = termInfo.item2;
			int id_term = termInfo.item1;
			int ftq = 0;
			boolean isRepited = false;					
			for(int j = 0 ; j < terms.size() ; ++j){
				if(terms.get(i) == terms.get(j)){
					ftq++;
					if(j > i){
						isRepited = true;
						break;
					}
				}
			}
			if(isRepited)continue;			//Si ya habiamos guardado la informacion de este termino no lo guardamos en el vector de resultados
			Double tftq = 1 + Math.log(ftq);
			vector.add(new Tuple<Integer, Double>(id_term,idft*tftq));
		}
		return vector;
	}*/
