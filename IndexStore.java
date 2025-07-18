package csc435.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Data structure that stores a document number and the number of time a word/term appears in the document
class DocFreqPair {
    public long documentNumber;
    public long wordFrequency;

    public DocFreqPair(long documentNumber, long wordFrequency) {
        this.documentNumber = documentNumber;
        this.wordFrequency = wordFrequency;
    }
}

public class IndexStore {

    private Map<String, Long> documentMap;
    private Map<String, List<DocFreqPair>> termInvertedIndex;
    private long nextDocumentNumber;
    private final Lock documentMapLock = new ReentrantLock();
    private final Lock termInvertedIndexLock = new ReentrantLock();
    // TO-DO declare data structure that keeps track of the DocumentMap
    // TO-DO declare data structures that keeps track of the TermInvertedIndex
    // TO-DO declare two locks, one for the DocumentMap and one for the TermInvertedIndex

    public IndexStore() {

        documentMap = new HashMap<>();
        termInvertedIndex = new HashMap<>();
        nextDocumentNumber = 1;
        // TO-DO initialize the DocumentMap and TermInvertedIndex members
    }

    public long putDocument(String documentPath) {
        long documentNumber = 0;
        // TO-DO assign a unique number to the document path and return the number
        // IMPORTANT! you need to make sure that only one thread at a time can access this method
        documentMapLock.lock();
        try {
            if (!documentMap.containsKey(documentPath)) {
                documentNumber = nextDocumentNumber++;
                documentMap.put(documentPath, documentNumber);
            } else {
                documentNumber = documentMap.get(documentPath);
            }
            return documentNumber;
        } finally {
            documentMapLock.unlock();
        }
    }

    public String getDocument(long documentNumber) {
        String documentPath = "";
        // TO-DO retrieve the document path that has the given document number

        for (Map.Entry<String, Long> entry : documentMap.entrySet()) {
            if (entry.getValue() == documentNumber) {
                documentPath = entry.getKey();
                break;
            }
        }
        return documentPath;
    }

    public void updateIndex(long documentNumber, HashMap<String, Long> wordFrequencies) {
        // TO-DO update the TermInvertedIndex with the word frequencies of the specified document
        // IMPORTANT! you need to make sure that only one thread at a time can access this method
        termInvertedIndexLock.lock(); 
        try {
            for (Map.Entry<String, Long> entry : wordFrequencies.entrySet()) {
                String term = entry.getKey();
                long frequency = entry.getValue();

                termInvertedIndex.putIfAbsent(term, new ArrayList<>());

                List<DocFreqPair> docList = termInvertedIndex.get(term);
                boolean found = false;
                for (DocFreqPair pair : docList) {
                    if (pair.documentNumber == documentNumber) {
                        pair.wordFrequency += frequency;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    docList.add(new DocFreqPair(documentNumber, frequency));
                }
            }
        } finally {
            termInvertedIndexLock.unlock();
        }
    }

    public ArrayList<DocFreqPair> lookupIndex(String term) {
        ArrayList<DocFreqPair> results = new ArrayList<>();
        // TO-DO return the document and frequency pairs for the specified term

        if (termInvertedIndex.containsKey(term)) {
            results.addAll(termInvertedIndex.get(term));
        }
        return results;
    }
}