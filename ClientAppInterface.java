package csc435.app;

import java.util.ArrayList;
import java.util.Scanner;

public class ClientAppInterface {
    private ClientProcessingEngine engine;

    public ClientAppInterface(ClientProcessingEngine engine) {
        this.engine = engine;
    }

    public void readCommands() {
        // TO-DO implement the read commands method
        Scanner sc = new Scanner(System.in);
        String command;

        while (true) {
            System.out.print("> ");

            // read from command line
            command = sc.nextLine();

            // if the command is quit, terminate the program
            if (command.compareTo("quit") == 0) {

                engine.disconnect();
                break;
            }

            // if the command begins with connect, connect to the given server
            if (command.length() >= 7 && command.substring(0, 7).compareTo("connect") == 0) {
                // TO-DO parse command cand call connect on the processing engine

                String[] parts = command.split(" ");
                if (parts.length == 3) {
                    engine.connect(parts[1], parts[2]);
                    System.out.println("Connected successfully!");
                } else {
                    System.out.println("Usage: connect <serverIP> <port>");
                }
                continue;
            }

            // if the command begins with get_info, print the client ID
            if (command.length() >= 7 && command.substring(0, 8).compareTo("get_info") == 0) {
                // TO-DO parse command cand call getInfo on the processing engine
                // TO-DO print the client ID

                System.out.println("Client ID: " + engine.getInfo());
                continue;
            }
            
            // if the command begins with index, index the files from the specified directory
            if (command.length() >= 5 && command.substring(0, 5).compareTo("index") == 0) {
                // TO-DO parse command and call indexFolder on the processing engine
                // TO-DO print the execution time and the total number of bytes read
                String[] parts = command.split(" ");
                if (parts.length == 2) {
                    engine.indexFiles(parts[1]);
                } else {
                    System.out.println("Usage: index <folder path>");
                }
                continue;
            }

            // if the command begins with search, search for files that matches the query
            if (command.length() >= 6 && command.substring(0, 6).compareTo("search") == 0) {
                // TO-DO parse command and call search on the processing engine
                // TO-DO print the execution time and the top 10 search results
                String[] parts = command.split(" ");
                if (parts.length > 1) {
                    ArrayList<String> terms = new ArrayList<>();
                    for (int i = 1; i < parts.length; i++) {
                        if (!parts[i].equalsIgnoreCase("AND")) {
                            terms.add(parts[i]);
                        }
                    }
                    engine.searchFiles(terms);
                } else {
                    System.out.println("Usage: search <term1> [AND] <term2> ...");
                }
                continue;
            }

            System.out.println("unrecognized command!");
        }

        sc.close();
    }
}
