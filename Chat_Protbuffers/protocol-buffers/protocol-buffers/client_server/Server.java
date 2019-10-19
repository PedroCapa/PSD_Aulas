package client_server;

import client_server.Protos.Sys;
import client_server.Protos.Person;
import client_server.Protos.Chat;


import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.io.*;
import java.net.*;

import java.util.List;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        try{
            int port = java.lang.Integer.parseInt(args[0]);
            ServerSocket srv = new ServerSocket(port);
            Sys sys = Sys.newBuilder().build();
            Output out = new Output();
            while (true) {
                Socket cli=srv.accept();
                CodedInputStream cis = CodedInputStream.newInstance(cli.getInputStream());
                CodedOutputStream cos = CodedOutputStream.newInstance(cli.getOutputStream());
                out.add(cos);
                (new ClientHandler(cis, cos, sys, out)).start();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    CodedInputStream cis;
    CodedOutputStream cos;
    Sys sys;
    Output out;

    ClientHandler(CodedInputStream cis, CodedOutputStream cos, Sys sys, Output out) {
        this.cis = cis;
        this.cos = cos;
        this.sys = sys;
        this.out = out;
    }

    public void run() {
        try {
            //O Cliente tem que enviar a pessoa que se conectou pq o cos muda sempre que ele se conecta
            int len = cis.readRawLittleEndian32();
            byte[] per = cis.readRawBytes(len);

            //Converter byte[] para Person
            Person p = Person.parseFrom(per);

            //Fazer um metodo no printer que recebe um Sys e um cos para enviar todas as mensagens em atraso para o cliente
            Printer.print(sys, null, p, null, cos);

            Printer.printPerson(sys);

            while (true) {
                len = cis.readRawLittleEndian32(); //Se a pessoa que escreveu enviar para toda a gente o read vai ser lido por todos
                byte[] ba = cis.readRawBytes(len);

                String s = ba.toString();
          
                //Adicionar ao chat a mensagem Talvez fazer no print pq do synchronized


                Printer.print(sys, s, p, this.out, null);//Enviar para toda a gente a mensagem
            }
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
      }
}


class Output{
    List<CodedOutputStream> cos;

    public Output(List<CodedOutputStream> cos){
        this.cos = cos;
    }

    public Output(){
        this.cos = new ArrayList<>();
    }

    public void add(CodedOutputStream c){
        this.cos.add(c);
    }
}