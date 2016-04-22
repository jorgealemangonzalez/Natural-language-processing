package ti;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class contains the logic to run the retrieval process of the search engine in interactive mode.
 */
public class Interactive
{
	protected RetrievalModel model;
	protected Index index;
	protected DocumentProcessor docProcessor;

	/**
	 * Creates a new interactive retriever using the given model.
	 *
	 * @param model        the retrieval model to run queries.
	 * @param index        the index.
	 * @param docProcessor the processor to extract query terms.
	 */
	public Interactive(RetrievalModel model, Index index, DocumentProcessor docProcessor)
	{
		this.model = model;
		this.index = index;
		this.docProcessor = docProcessor;
	}

	/**
	 * Runs the interactive retrieval process. It asks the user for a query, and then it prints the results to
	 * {@link System#out} showing the document title and a snippet, highlighting important terms for the query.
	 *
	 * @throws Exception in an error occurs during the process.
	 */
	public void run() throws Exception
	{
		// Run prompt loop
		Scanner scan = new Scanner(System.in);
		String input;
		do {
			System.out.println();
			System.out.print("Query (empty to exit): ");
			scan.reset();
			input = scan.nextLine();

			ArrayList<Tuple<Integer, Double>> results = this.model.runQuery(input, this.index, this.docProcessor);

			// P5
			// paginar resultados
			this.printResults(results, 0, 10);
		} while (!input.isEmpty());
	}

	/**
	 * Print a page of results for a query, showing for each document its title and snippet, with highlighted terms.
	 *
	 * @param results the results for the query. A list of {@link Tuple}s where the first item is the {@code docID} and
	 *                the second item is the similarity score.
	 * @param from    index of the first result to print.
	 * @param count   how many results to print from the {@code from} index.
	 */
	protected void printResults(ArrayList<Tuple<Integer, Double>> results, int from, int count)
	{
		// P5
	}
}