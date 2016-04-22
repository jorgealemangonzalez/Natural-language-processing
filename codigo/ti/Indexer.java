package ti;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

/**
 * This class contains the logic to run the indexing process of the search engine.
 */
public class Indexer
{
    protected File pathToIndex;
    protected File pathToCollection;
    protected DocumentProcessor docProcessor;

	/**
     * Creates a new indexer with the given paths and document processor.
     * @param pathToIndex path to the index directory.
     * @param pathToCollection path to the original documents directory.
     * @param docProcessor document processor to extract terms.
     */
    public Indexer(File pathToIndex, File pathToCollection, DocumentProcessor docProcessor)
    {
        this.pathToIndex = pathToIndex;
        this.pathToCollection = pathToCollection;
        this.docProcessor = docProcessor;
    }

	/**
     * Run the indexing process in two passes and save the index to disk.
     * @throws IOException if an error occurs while indexing.
     */
    public void run() throws IOException
    {
        Index ind = new Index(this.pathToIndex.getPath());
        this.firstPass(ind);
        this.secondPass(ind);

        // Save index
        System.err.print("Saving index...");
        ind.save();
        System.err.println("done.");
        System.err.println("Index statistics:");
        ind.printStatistics();
    }
    /**
     * Runs the first pass of the indexer.
     * It builds the inverted index by iterating all original document files and calling {@link #processDocument}.
     * @param ind the index.
     * @throws IOException if an error occurs while processing a document.
     */
    protected void firstPass(Index ind) throws IOException
    {
        DecimalFormat df = new DecimalFormat("#.##");
        long startTime = System.currentTimeMillis();
        int totalDocuments = 0;
        long totalBytesDocuments = 0;

        System.err.println("Running first pass...");
        for (File subDir : this.pathToCollection.listFiles()) {
            if (!subDir.getName().startsWith(".")) {
                for (File docFile : subDir.listFiles()) {
                    if (docFile.getPath().endsWith(".html")) {
                        try {
                            System.err.print("  Indexing file " + docFile.getName() + "...");
                            this.processDocument(docFile, ind);
                            System.err.print("done.");
                        } catch (IOException ex) {
                            System.err.println("exception!");
                            System.err.print(ex.getMessage());
                        } finally {
                            System.err.println();
                        }
                        totalDocuments++;
                        totalBytesDocuments += docFile.length();
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        double totalTime = (endTime - startTime) / 1000d;
        double totalMegabytes = totalBytesDocuments / 1024d / 1024d;
        System.err.println("...done:");
        System.err.println("  - Documents: " + totalDocuments + " (" + df.format(totalMegabytes) + " MB).");
        System.err.println("  - Time: " + df.format(totalTime) + " seconds.");
        System.err.println("  - Throughput: " + df.format(totalMegabytes / totalTime) + " MB/s.");
    }
    /**
     * Runs the second pass of the indexer.
     * Here it traverses the inverted index to compute and store IDF, update weights in the postings,
     * build the direct index, and compute document norms.
     * @param ind the index.
     */
    protected void secondPass(Index ind)
    {
        DecimalFormat df = new DecimalFormat("#.##");
        long startTime = System.currentTimeMillis();

        System.err.println("Running second pass...");
        System.err.print("  Updating term weights and direct index...");

		// P2
		// recorrer el índice para calcular IDF y actualizar pesos

		// P4
		// actualizar directIndex
        // Traverse all terms to compute IDF, direct postings, and norm summations

        System.err.println("done.");

        System.err.print("  Updating document norms...");
        
		// P2
		// actualizar normas de documentos

        long endTime = System.currentTimeMillis();
        double totalTime = (endTime - startTime) / 1000d;
        System.err.println("done.");
        System.err.println("...done");
        System.err.println("  - Time: " + df.format(totalTime) + " seconds.");
    }
	/**
     * Process the original document in the specified path and add it to the given index.
     * <p>
     * After extracting the document terms, it populates the vocabulary and document structures,
     * and adds the corresponding postings to the inverted index.
     * @param docFile the path to the original document file.
     * @param ind the index to add the document to.
     * @throws IOException if an error occurrs while processing this document.
     */
    protected void processDocument(File docFile, Index ind) throws IOException
    {
		// P2
        // leer documento desde disco
		// procesarlo para obtener los términos
		// calcular pesos
		// actualizar estructuras del índice: vocabulary, documents e invertedIndex
    }
}
