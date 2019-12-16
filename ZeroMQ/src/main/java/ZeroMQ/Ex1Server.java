import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import java.util.Random;

public class Ex1Server
{
    public static void main(String[] args) throws Exception
    {
        try (ZContext context = new ZContext()) {
            //Socket to receive messages
            ZMQ.Socket socket = context.createSocket(ZMQ.REP);
            socket.bind("tcp://*:5555");
            // Socket to talk to clients
            ZMQ.Socket publisher = context.createSocket(ZMQ.PUB);
            publisher.bind("tcp://*:5556");

            Random rand = new Random();

            while (!Thread.currentThread().isInterrupted()) {
                //Send to the subscribers
                String message = socket.recvStr();
                String [] message_split = message.split(" ");
                String id = message_split[0].replace("\\", "");
                String data = "";
                for(int i = 1; i < message_split.length; i++){
                    data += message_split[i] + " ";
                }
                publisher.send(id + " " + data);
                Thread.sleep(1500);
            }
        }
    }
}

