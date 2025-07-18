package csc435.app;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ZMQProxyWorker implements Runnable {
    private ZContext context;
    private int serverPort;

    public ZMQProxyWorker(ZContext context, int serverPort) {
        this.context = context;
        this.serverPort = serverPort;
    }
    
    @Override
    public void run() {
        try (ZMQ.Socket router = context.createSocket(SocketType.ROUTER);
             ZMQ.Socket dealer = context.createSocket(SocketType.DEALER)) {
            router.bind("tcp://*:" + serverPort);
            dealer.bind("inproc://workers");
            ZMQ.proxy(router, dealer, null);
        } catch (Exception e) {
            System.err.println("Error starting ZeroMQ Proxy: " + e.getMessage());
        }
        // TO-DO create and bind router and dealer sockets
        // TO-DO create and start the ZMQ Proxy that will forward messages between the router and dealer sockets
        // TO-DO close the router and dealer sockets
        // TO-DO close the context
    }
}
