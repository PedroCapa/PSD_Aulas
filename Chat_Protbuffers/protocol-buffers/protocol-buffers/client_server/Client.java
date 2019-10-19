package client_server;

import client_server.Protos.Sys;
import client_server.Protos.Person;
import client_server.Protos.Chat;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.ByteString;

import java.io.*;
import java.net.*;
import java.lang.Object;

public class Client {

    public static void main(String[] args) {
        try{
            if(args.length<4)
                System.exit(1);
            String host = args[0];
            int port = Integer.parseInt(args[1]);

            Socket s = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            CodedInputStream cis = CodedInputStream.newInstance(s.getInputStream());
            CodedOutputStream cos = CodedOutputStream.newInstance(s.getOutputStream());
            
            Person p = createPerson(args[2], args[3]);
            
            byte[] ba = p.toByteArray();

            System.out.println("Pretendo entrar como " + p.getName());

            //Enviar a pessoa para o servidor
            cos.writeFixed32NoTag(ba.length);
            cos.writeRawBytes(ba);
            cos.flush();
        
            //Criar thread, uma para ler do teclado e outra para ler do servidor escrever
            (new LeServidor(cis, cos)).start();

            while (true) {
                //Esta parte ele envia a mensagem para o servidor, penso que esteja feito
                String str = in.readLine();
                System.out.println(str);
                
            }
            //os.close();
            //s.shutdownOutput();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    static Person createPerson(String nome, String pass) {
        return
        Person.newBuilder()
        .setName(nome)
        .setPass(pass)
        .build();
    }

}

//Criar classe que le do servidor o que escreve

//No while o que ler do servidor faz um System.out.println

class LeServidor extends Thread {
    CodedInputStream cis;
    CodedOutputStream cos;

    LeServidor(CodedInputStream cis, CodedOutputStream cos) {
        this.cis = cis;
        this.cos = cos;
    }

    public void run() {
        try {
            while (true) {
                String str = "Envio mensagem de Pedro";
                byte[] ba = str.getBytes();

                cos.writeFixed32NoTag(ba.length);
                cos.writeRawBytes(ba);
                cos.flush();
                Thread.sleep(100000);

            }
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
        catch(InterruptedException e){
            System.out.println(e.getMessage());
        }
      }
}

