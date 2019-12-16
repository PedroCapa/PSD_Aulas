import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import java.util.Random;

public class Server
{
    public static void main(String[] args) throws Exception
    {
        try (ZContext context = new ZContext()) {
            // Socket to talk to clients
            ZMQ.Socket publisher = context.createSocket(ZMQ.PUB);
            publisher.bind("tcp://*:5555");

            Random rand = new Random();

            while (!Thread.currentThread().isInterrupted()) {
                //Send to the subscribers
                int id = rand.nextInt(10);
                int data = rand.nextInt(100);
                publisher.sendMore(Integer.toString(id));
                publisher.send(Integer.toString(data));
            }
        }
    }
}