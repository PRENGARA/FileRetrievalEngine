package csc435.app;

import java.util.ArrayList;
import java.util.List;

import org.zeromq.ZContext;

public class ServerProcessingEngine {
    private IndexStore store;
    private ZContext context;
    private Thread proxyThread;
    private List<Thread> workerThreads;
    // TO-DO keep track of the ZMQ context
    // TO-DO keep track of the ZMQ Proxy object
    // TO-DO keep track of the ZMQ Proxy thread and worker threads

    public ServerProcessingEngine(IndexStore store) {
        this.store = store;
        this.workerThreads = new ArrayList<>();
    }

    public void initialize(int serverPort, int numWorkerThreads) {
        this.context = new ZContext();
        proxyThread = new Thread(new ZMQProxyWorker(context, serverPort));
        proxyThread.start();
        for (int i = 0; i < numWorkerThreads; i++) {
            Thread workerThread = new Thread(new ServerWorker(store, context));
            workerThreads.add(workerThread);
            workerThread.start();
        }
        // TO-DO initialize the ZMQ context
        // TO-DO create a ZMQ Proxy object
        // TO-DO create and start the ZMQ Proxy thread
        // TO-DO create Server Worker objects
        // TO-DO create and start the worker threads
    }

    public void shutdown() {
        context.close();
        try {
            proxyThread.join();
            for (Thread t : workerThreads) t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // TO-DO destroy the ZMQ context
        // TO-DO join the ZMQ Proxy and worker threads
    }
}
