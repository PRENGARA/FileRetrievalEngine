package csc435.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ServerWorker implements Runnable {
    private IndexStore store;
    private ZContext context;
    private ZMQ.Socket worker;
    private static long clientCounter = 1;
    private static final Object lock = new Object();

    public ServerWorker(IndexStore store, ZContext context) {
        this.store = store;
        this.context = context;
        this.worker = context.createSocket(SocketType.REP);
        worker.connect("inproc://workers"); 
    }
    
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            String request = worker.recvStr(); 
            if (request == null) {
                continue;
            }

            String[] parts = request.split(" ", 2);
            String command = parts[0];

            if (command.equals("REGISTER")) {
                handleRegisterRequest();
            } else if (command.equals("INDEX") && parts.length > 1) {
                handleIndexRequest(parts[1]);
            } else if (command.equals("SEARCH") && parts.length > 1) {
                handleSearchRequest(parts[1]);
            } else if (command.equals("QUIT")) {
                worker.send("BYE");
                break;
            }
        }
        worker.close();
        // TO-DO create a reply socket and connect it to the dealer
        // TO-DO receive a message from the client
        // TO-DO if the message is a REGISTER REQUEST, then
        //       generate a new client ID and return a REGISTER REPLY message containing the client ID
        // TO-DO if the message is an INDEX REQUEST, then
        //       extract the document path, client ID and word frequencies from the message(s)
        //       get the document number associated with the document path (call putDocument)
        //       update the index store with the word frequencies and the document number
        //       return an acknowledgement INDEX REPLY message
        // TO-DO if the message is a SEARCH REQUEST, then
        //       extract the terms from the message
        //       for each term get the pairs of documents and frequencies from the index store
        //       combine the returned documents and frequencies from all of the specified terms
        //       sort the document and frequency pairs and keep only the top 10
        //       for each document number get from the index store the document path
        //       return a SEARCH REPLY message containing the top 10 results
        // TO-DO if the message is a QUIT message, then finish running
        // TO-DO close the reply socket

    }

    private void handleRegisterRequest() {
        long clientId;
        synchronized (lock) {
            clientId = clientCounter++;
        }
        worker.send("REGISTER_REPLY " + clientId);
    }

    private void handleIndexRequest(String requestData) {
        String[] parts = requestData.split(" ", 2);
        if (parts.length < 2) {
            worker.send("INDEX_ERROR");
            return;
        }

        String documentPath = parts[0];
        long documentNumber = store.putDocument(documentPath);
        HashMap<String, Long> wordFrequencies = parseWordFrequencies(parts[1]);

        store.updateIndex(documentNumber, wordFrequencies);
        worker.send("INDEX_REPLY");
    }

    private void handleSearchRequest(String requestData) {
        List<String> terms = Arrays.asList(requestData.split(","));
        List<DocPathFreqPair> searchResults = performSearch(terms);

        StringBuilder response = new StringBuilder("SEARCH_REPLY\n" + searchResults.size() + "\n");
        for (int i = 0; i < Math.min(10, searchResults.size()); i++) {
            response.append(searchResults.get(i).documentPath)
                    .append(":")
                    .append(searchResults.get(i).wordFrequency)
                    .append("\n");
        }

        worker.send(response.toString());
    }

    private List<DocPathFreqPair> performSearch(List<String> terms) {
        Map<Long, Long> docFrequencyCount = new HashMap<>();
        Set<Long> commonDocs = null;

        for (String term : terms) {
            List<DocFreqPair> termResults = store.lookupIndex(term);
            Set<Long> termDocs = new HashSet<>();

            for (DocFreqPair pair : termResults) {
                termDocs.add(pair.documentNumber);
                docFrequencyCount.put(pair.documentNumber,
                        docFrequencyCount.getOrDefault(pair.documentNumber, 0L) + pair.wordFrequency);
            }

            if (commonDocs == null) {
                commonDocs = termDocs;
            } else {
                commonDocs.retainAll(termDocs); 
            }
        }

        List<DocPathFreqPair> sortedResults = new ArrayList<>();
        if (commonDocs != null) {
            for (Long docNum : commonDocs) {
                if (docFrequencyCount.containsKey(docNum)) {
                    String docPath = store.getDocument(docNum);
                    long totalFrequency = docFrequencyCount.get(docNum);
                    sortedResults.add(new DocPathFreqPair(docPath, totalFrequency));
                }
            }
        }

        sortedResults.sort((a, b) -> Long.compare(b.wordFrequency, a.wordFrequency));

        return sortedResults;
    }

    private HashMap<String, Long> parseWordFrequencies(String data) {
        HashMap<String, Long> frequencies = new HashMap<>();
        StringTokenizer tokenizer = new StringTokenizer(data, ",");
        while (tokenizer.hasMoreTokens()) {
            String entry = tokenizer.nextToken();
            int separator = entry.indexOf(":");
            if (separator != -1) {
                String key = entry.substring(0, separator);
                long value = Long.parseLong(entry.substring(separator + 1));
                frequencies.put(key, value);
            }
        }
        return frequencies;
    }
}
