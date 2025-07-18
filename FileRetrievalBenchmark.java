package csc435.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class BenchmarkWorker implements Runnable {
    // TO-DO keep track of a ClientProcessingEngine object
    private ClientProcessingEngine engine;
    private String serverIP;
    private String serverPort;
    private String datasetPath;

    public BenchmarkWorker(String serverIP, String serverPort, String datasetPath) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.datasetPath = datasetPath;
        this.engine = new ClientProcessingEngine();
    }

    @Override
    public void run() {
        // TO-DO create a ClientProcessinEngine object
        // TO-DO connect the ClientProcessingEngine to the server
        // TO-DO index the dataset
        engine.connect(serverIP, serverPort);
        engine.indexFiles(datasetPath);
    }

    public void search() {
        // TO-DO perform search on the ClientProcessingEngine object
        // TO-DO print the results and performance
        ArrayList<String> query1 = new ArrayList<>();
        query1.add("the");
        System.out.println("Searching: " + String.join(" ", query1));
        SearchResult result1 = engine.searchFiles(query1);
        System.out.println("Search completed in " + result1.executionTime + " seconds");
        System.out.println("Search results (top 10 out of " + result1.documentFrequencies.size() + "):");
        for (DocPathFreqPair pair : result1.documentFrequencies) {
            System.out.println("* " + pair.documentPath + ":" + pair.wordFrequency);
        }

        ArrayList<String> query2 = new ArrayList<>();
        query2.add("child-like");
        System.out.println("Searching: " + String.join(" ", query2));
        SearchResult result2 = engine.searchFiles(query2);
        System.out.println("Search completed in " + result2.executionTime + " seconds");
        System.out.println("Search results (top 10 out of " + result2.documentFrequencies.size() + "):");
        for (DocPathFreqPair pair : result2.documentFrequencies) {
            System.out.println("* " + pair.documentPath + ":" + pair.wordFrequency);
        }

        ArrayList<String> query3 = new ArrayList<>();
        query3.add("distortion");
        query3.add("adaptation");
        System.out.println("Searching: " + String.join(" AND ", query3));
        SearchResult result3 = engine.searchFiles(query3);
        System.out.println("Search completed in " + result3.executionTime + " seconds");
        System.out.println("Search results (top 10 out of " + result3.documentFrequencies.size() + "):");
        for (DocPathFreqPair pair : result3.documentFrequencies) {
            System.out.println("* " + pair.documentPath + ":" + pair.wordFrequency);
        }
    }

    public void disconnect() {
        // TO-DO disconnect the ClientProcessingEngine object from the server
        engine.disconnect();
    }
}

public class FileRetrievalBenchmark {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java FileRetrievalBenchmark <serverIP> <serverPort> <numberOfClients> <datasetPath1> <datasetPath2> ... <datasetPathN>");
            return;
        }
        // TO-DO extract the arguments from args
        // TO-DO measure the execution start time
        // TO-DO create Benchmark Worker objects equal to the number of clients
        // TO-DO create and start benchmark worker threads equal to the number of clients
        // TO-DO join the benchmark worker threads
        // TO-DO measure the execution stop time and print the performance
        // TO-DO run search queries on the first client
        // TO-DO disconnect all clients

        String serverIP = args[0];
        String serverPort = args[1];
        int numberOfClients = Integer.parseInt(args[2]);
        List<String> clientsDatasetPath = new ArrayList<>();

        for (int i = 3; i < args.length; i++) {
            clientsDatasetPath.add(args[i]);
        }

        if (clientsDatasetPath.size() < numberOfClients) {
            System.out.println("Error: Insufficient dataset paths provided for " + numberOfClients + " clients.");
            return;
        }

        long startTime = System.nanoTime();

        List<Thread> threads = new ArrayList<>();
        List<BenchmarkWorker> workers = new ArrayList<>();

        for (int i = 0; i < numberOfClients; i++) {
            BenchmarkWorker worker = new BenchmarkWorker(serverIP, serverPort, clientsDatasetPath.get(i));
            workers.add(worker);
            Thread thread = new Thread(worker);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1e9;
        System.out.println("Completed indexing all datasets in " + executionTime + " seconds");

        if (!workers.isEmpty()) {
            workers.get(0).search();
        }

        for (BenchmarkWorker worker : workers) {
            worker.disconnect();
        }

        Scanner sc = new Scanner(System.in);
        System.out.print("> ");
        String command = sc.nextLine();
        if (command.equalsIgnoreCase("quit")) {
            System.exit(0);
        }
        sc.close();
    }
}
