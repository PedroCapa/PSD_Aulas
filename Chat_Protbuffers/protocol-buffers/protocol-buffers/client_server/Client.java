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
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            CodedInputStream cis = CodedInputStream.newInstance(s.getInputStream());
            CodedOutputStream cos = CodedOutputStream.newInstance(s.getOutputStream());
            
            Person p = createPerson(args[2], args[3]);
            
            byte[] ba = p.toByteArray();

            System.out.println("Pretendo entrar como " + p.getName());

            //Enviar a pessoa para o servidor
            cos.writeFixed32NoTag(ba.length);
            cos.writeRawBytes(ba);
            cos.flush();

            System.out.println("Já fui enviado ao servidor");
        
            //A thread criada vai ler do servidor
            (new LeServidor(cis, cos)).start();

            System.out.println("Vou entrar no ciclo");

            while (true) {
                //Aqui crio uma classe chat com a mensagem escrita do teclado e envio para o servidor
                System.out.println("Eu estou à espera de uma mensagem");
                String str = in.readLine();
                System.out.println("Acabei de ler uma mensagem");
                Chat.Builder chat = Chat.newBuilder();
                chat.
                    setPerson(p.getName()).
                    setNote(str);

                byte [] cb = chat.build().toByteArray();

                cos.writeFixed32NoTag(cb.length);
                cos.writeRawBytes(cb);
                cos.flush();
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
            System.out.println("A thread LeServidor foi criada");
            while (true) {
                int len = cis.readRawLittleEndian32();
                byte[] ba = cis.readRawBytes(len);
                Chat chat = Chat.parseFrom(ba);
                Printer.print(chat);
            }
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
      }
}

