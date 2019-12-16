//  Hello World client in Java
//  Connects REQ socket to tcp://localhost:5555
//  Sends "Hello" to server, expects "World" back
import java.net.*;
import java.io.*;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import java.util.Random;

public class Cliente
{
    public static void main(String[] args)
    {
        try (ZContext context = new ZContext()) {
            System.out.println("Connecting to hello world server");

      		//  Socket to talk to server
            ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
            subscriber.connect("tcp://localhost:5555");

            Random rand = new Random();
            int id = rand.nextInt(10);

            subscriber.subscribe(Integer.toString(id));
            String reply = subscriber.recvStr();
            System.out.println(reply);
        }
    }
}