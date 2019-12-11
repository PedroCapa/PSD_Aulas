//  Hello World client in Java
//  Connects REQ socket to tcp://localhost:5555
//  Sends "Hello" to server, expects "World" back
import java.net.*;
import java.io.*;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cliente
{
    public static void main(String[] args)
    {
        try (ZContext context = new ZContext()) {
            System.out.println("Connecting to hello world server");

            //Socket to publish
            ZMQ.Socket publisher = context.createSocket(SocketType.REQ);
            publisher.connect("tcp://localhost:5555");

      		//  Socket to subscribe
            ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
            subscriber.connect("tcp://localhost:5556");

            Keyboard k = new Keyboard(subscriber, publisher);
            Thread t = new Thread(k);
            t.start();

            while (!Thread.currentThread().isInterrupted()) {
                String reply = subscriber.recvStr();
                System.out.println(reply);
            }

        }
    }
}

class Keyboard implements Runnable{
    
    private ZMQ.Socket subscriber;
    private ZMQ.Socket publisher;
    private String room;

    public Keyboard(ZMQ.Socket subs, ZMQ.Socket pub){
        this.subscriber = subs;
        this.publisher  = pub;
        this.room = "";
    }

    public void run(){
        try{
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
            String input;

            while((input = teclado.readLine()) != null){
                processInput(input);
            }
            
            teclado.close();
        }
        catch(IOException exc){}
    }

    public void processInput(String input){
        if(subMessage(input)){
            String id = getSubscription(input);
            subscriber.unsubscribe(this.room.getBytes(ZMQ.CHARSET));
            subscriber.subscribe(id.getBytes(ZMQ.CHARSET));
            this.room = id;
            System.out.println("Subscrevi " + input);
        }
        else{
            sendMessage(input);
            System.out.println("Enviei " + input);
        }
    }

    public boolean subMessage(String input){
        return Pattern.matches("room [A-Za-z]+", input);
    }

    public void sendMessage(String input){
        this.publisher.send(input.getBytes(ZMQ.CHARSET));
    }

    public String getSubscription(String input){
        String [] input_split = input.split(" ");
        String subscritpted_channel = input_split[1];
        return subscritpted_channel;
    }
}
