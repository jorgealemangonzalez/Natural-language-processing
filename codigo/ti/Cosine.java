package ti;

import java.util.*;

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
                //ftd : apariciones palabra en documento
                //nd : numero de documentos totales
                //ct : numero documentos donde aparece la palabra
                //tftd : indicador frecuancia aparicion del termino , suavizando en el numero de apariciones
                //idft : indicador que penaliza la aparicion de una palabra en una gran cantidad de documentos
                //wtd : peso de una palabra en la query
		// P1

		return vector;
	}
}
