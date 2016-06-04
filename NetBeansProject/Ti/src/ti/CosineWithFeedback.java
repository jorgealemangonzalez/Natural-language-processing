package ti;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements retrieval in a vector space with the cosine similarity function and a TFxIDF weight formulation,
 * plus pseudorelevance feedback.
 */
public class CosineWithFeedback extends Cosine
{
	protected int feedbackDepth;
	protected double feedbackAlpha;
	protected double feedbackBeta;

	/**
	 * Creates a new retriver with the specified pseudorelevance feedback parameters.
	 *
	 * @param feedbackDepth number of documents to consider relevant.
	 * @param feedbackAlpha relative weight of the original query terms
	 * @param feedbackBeta  relative weight of the expanded terms.
	 */
	public CosineWithFeedback(int feedbackDepth, double feedbackAlpha, double feedbackBeta)
	{
		super();
		this.feedbackDepth = feedbackDepth;
		this.feedbackAlpha = feedbackAlpha;
		this.feedbackBeta = feedbackBeta;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<Tuple<Integer, Double>> runQuery(String queryText, Index index, DocumentProcessor docProcessor)
	{
            // P4
            // calcular resultados iniciales
            // Extraer los términos de la consulta.
            ArrayList<String> queryTokens = docProcessor.processText(queryText); 
            // Calcular el vector inicial.
            ArrayList<Tuple<Integer, Double>> queryVector = super.computeVector(queryTokens, index);
            
            // calcular similitud de documentos
            ArrayList<Tuple<Integer, Double>> similDocs = super.computeScores(queryVector, index);
            
            //Obtener los primeros k documentos para q0
            ArrayList<Tuple<Integer, Double>> docs = new ArrayList<Tuple<Integer, Double>>(similDocs.subList(0, feedbackDepth));
            
            // actualizar vector consulta
            queryVector = computeFeedbackVector(queryVector, docs, index);
	    
            // volver a ejecutar consulta
            ArrayList<Tuple<Integer, Double>> docsWithFeedback = super.computeScores(queryVector, index);
            
            return new ArrayList<Tuple<Integer, Double>>(docsWithFeedback.subList(0, 
                    (500 > docsWithFeedback.size() ? docsWithFeedback.size() : 500))); // y devolver resultados
        }

	/**
	 * Computes the modified query vector for relevance feedback.
	 *
	 * @param queryVector the original query vector.
	 * @param results     the results with the original query.
	 * @param index       the index to search in.
	 * @return a list of {@code Tuple}s with the {@code termID} as first item and the weight as second one.
	 */
	protected ArrayList<Tuple<Integer, Double>> computeFeedbackVector(ArrayList<Tuple<Integer, Double>> queryVector,
	                                                                  ArrayList<Tuple<Integer, Double>> results,
	                                                                  Index index)
	{
            
            HashMap<Integer, Double> newVocab = new HashMap<>();
           
            for(Tuple<Integer, Double> queryTerm : queryVector){
                if(newVocab.containsKey(queryTerm.item1) == false)
                    newVocab.put(queryTerm.item1, 0.0);
                newVocab.put(queryTerm.item1, feedbackAlpha * queryTerm.item2); //primera parte de la ecuacion de rocchio 
            }
            
            for(Tuple<Integer, Double> doc : results){
                for(Tuple<Integer, Double> term : index.directIndex.get(doc.item1)){
                    if(newVocab.containsKey(term.item1) == false)
                        newVocab.put(term.item1, 0.0);
                    newVocab.put(term.item1, newVocab.get(term.item1) + (feedbackBeta * 1/feedbackDepth * term.item2));
                }
            }
            
            // P4
            // Creo que no es necesario.
            ArrayList<Tuple<Integer, Double>> weights = new ArrayList<>();
            for(Map.Entry<Integer, Double> e : newVocab.entrySet()){
                if(e.getValue() > 0)
                    weights.add(new Tuple<Integer, Double>(e.getKey(), e.getValue()));
            }
            
            return weights;
	}
}
