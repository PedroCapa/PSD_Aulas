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

            cos.writeFixed32NoTag(ba.length);
            cos.writeRawBytes(ba);
            cos.flush();
            
            int len = cis.readRawLittleEndian32();
            ba = cis.readRawBytes(len);
            int x = (int)ba[0];

            if(x == -1){
                System.out.println("Palavra passe incorreta");
                return;
            }

            else if(x == 0)
                System.out.println("Conta criada com sucesso");

            else if(x == 1){
                System.out.println("Sessão iniciada com sucesso");
            }
            (new LeServidor(cis, cos)).start();

            while (true) {
                String str = in.readLine();
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

