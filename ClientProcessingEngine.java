package csc435.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

class IndexResult {
    public double executionTime;
    public long totalBytesRead;

    public IndexResult(double executionTime, long totalBytesRead) {
        this.executionTime = executionTime;
        this.totalBytesRead = totalBytesRead;
    }
}

class DocPathFreqPair {
    public String documentPath;
    public long wordFrequency;
    public long documentNumber;

    public DocPathFreqPair(String documentPath, long wordFrequency) {
        this.documentPath = documentPath;
        this.wordFrequency = wordFrequency;
        this.documentNumber = documentNumber;
    }
}

class SearchResult {
    public double executionTime;
    public ArrayList<DocPathFreqPair> documentFrequencies;

    public SearchResult(double executionTime, ArrayList<DocPathFreqPair> documentFrequencies) {
        this.executionTime = executionTime;
        this.documentFrequencies = documentFrequencies;
    }
}

public class ClientProcessingEngine {
    // TO-DO keep track of the ZMQ context
    // TO-DO keep track of the request socket
    private ZContext context;
    private ZMQ.Socket requestSocket;
    private long clientId;
    private String serverIP;
    private String serverPort;

    public ClientProcessingEngine() { }

    public IndexResult indexFiles(String folderPath) {
        IndexResult result = new IndexResult(0.0, 0);
        long startTime = System.nanoTime();
        Path basePath = Paths.get(folderPath);

        List<Path> files;
        try {
            files = Files.walk(Paths.get(folderPath))
                    .filter(Files::isRegularFile)
                    .toList();
        } catch (IOException e) {
            System.out.println("Error: Cannot read files in " + folderPath);
            return result;
        }

        long totalBytesRead = 0;
        for (Path filePath : files) {
            try {
                byte[] fileBytes = Files.readAllBytes(filePath);
                totalBytesRead += fileBytes.length;
                String content = new String(fileBytes);
                HashMap<String, Long> wordFrequencies = extractWordFrequencies(content);
                String relativePath = basePath.relativize(filePath).toString();
                String indexedPath = "Client" + clientId + "_" + relativePath.replace("\\", "/");
                String indexMessage = "INDEX " + indexedPath + " " + wordFrequenciesToString(wordFrequencies);
                requestSocket.send(indexMessage);
                String response = requestSocket.recvStr();
                if (!response.equals("INDEX_REPLY")) {
                    System.out.println("Unexpected response from server: " + response);
                }
            } catch (IOException e) {
                System.out.println("Error reading file: " + filePath);
            }
        }

        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1e9;
        result = new IndexResult(executionTime, totalBytesRead);
        // TO-DO get the start time
        // TO-DO crawl the folder path and extrac all file paths
        // TO-DO for each file extract all words/terms and count their frequencies
        // TO-DO increment the total number of bytes read
        // TO-DO for each file prepare an INDEX REQUEST message and send to the server
        //       the document path, the client ID and the word frequencies
        // TO-DO receive for each INDEX REQUEST message an INDEX REPLY message
        // TO-DO get the stop time and calculate the execution time
        // TO-DO return the execution time and the total number of bytes read

        return result;
    }
    
    public SearchResult searchFiles(ArrayList<String> terms) {
        SearchResult result = new SearchResult(0.0, new ArrayList<DocPathFreqPair>());
        if (terms.isEmpty()) {
            System.out.println("Error: No search terms provided.");
            return result;
        }

        long startTime = System.nanoTime();
        requestSocket.send("SEARCH " + String.join(",", terms));
        String response = requestSocket.recvStr();

        if (response.startsWith("SEARCH_REPLY")) {
            String[] lines = response.split("\n");
            int totalResults = Integer.parseInt(lines[1]);

            ArrayList<DocPathFreqPair> results = new ArrayList<>();
            for (int i = 2; i < lines.length; i++) {
                String[] parts = lines[i].split(":");
                if (parts.length == 2) {
                    results.add(new DocPathFreqPair(parts[0], Long.parseLong(parts[1])));
                }
            }

            long endTime = System.nanoTime();
            double executionTime = (endTime - startTime) / 1e9;
            return new SearchResult(executionTime, results);
        } else {
            System.out.println("Unexpected response from server: " + response);
        }

        return result;
    }

    public long getInfo() {
        return clientId;
    }

    public void connect(String serverIP, String serverPort) {
        // TO-DO get the start time
        // TO-DO prepare a SEARCH REQUEST message that includes the search terms and send it to the server
        // TO-DO receive one or more SEARCH REPLY messages with the results of the search query
        // TO-DO get the stop time and calculate the execution time
        // TO-DO return the execution time and the top 10 documents and frequencies
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        context = new ZContext();
        requestSocket = context.createSocket(ZMQ.REQ);
        requestSocket.connect("tcp://" + serverIP + ":" + serverPort);

        requestSocket.send("REGISTER");
        String response = requestSocket.recvStr();
        if (response.startsWith("REGISTER_REPLY")) {
            clientId = Long.parseLong(response.split(" ")[1]);
        } else {
            System.out.println("Error: Unexpected response from server: " + response);
        }
    }

    public void disconnect() {
        requestSocket.send("QUIT");
        requestSocket.recvStr();
        requestSocket.close();
        context.close();
    }

    private HashMap<String, Long> extractWordFrequencies(String content) {
        HashMap<String, Long> frequencies = new HashMap<>();
        StringTokenizer tokenizer = new StringTokenizer(content, " \t\n\r\f.,;:!?'\"()[]{}");
        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken().toLowerCase();
            if (word.length() > 3) {
                frequencies.put(word, frequencies.getOrDefault(word, 0L) + 1);
            }
        }
        return frequencies;
    }

    private String wordFrequenciesToString(HashMap<String, Long> wordFrequencies) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Long> entry : wordFrequencies.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
    }
}
