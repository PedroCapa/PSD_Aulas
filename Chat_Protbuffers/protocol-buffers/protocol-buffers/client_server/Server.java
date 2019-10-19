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
            addPerson(p);

            //Fazer um metodo no printer que recebe um Sys e um cos para enviar todas as mensagens em atraso para o cliente
            //Printer.print(sys, null, p, null, cos);
            //Printer.printPerson(sys);

            while (true) {
                len = cis.readRawLittleEndian32(); //Se a pessoa que escreveu enviar para toda a gente o read vai ser lido por todos
                byte[] ba = cis.readRawBytes(len);
                Chat chat = Chat.parseFrom(ba);
                Printer.print(chat);          
                //Adicionar ao chat a mensagem Talvez fazer no print pq do synchronized
                this.sys.toBuilder().addChat(chat);
                this.out.sendChat(chat);

                //Printer.print(sys, null, p, this.out, null);//Enviar para toda a gente a mensagem
            }
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
      }

    public synchronized void addPerson(Person person){

        List<Person> persons = this.sys.getPersonList();
        boolean flag = true;
        for(Person p: persons){
            flag = p.getName().equals(person.getName());
            if(!flag){
                break;
            }
        }
        if(flag){
            Sys.Builder s = this.sys.toBuilder();
            s.addPerson(person);
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

    public void sendChat(Chat chat){
        byte [] cb = chat.toByteArray();
        for(CodedOutputStream output_stream: cos){
            try{
                output_stream.writeFixed32NoTag(cb.length);
                output_stream.writeRawBytes(cb);
                output_stream.flush();
            }
            catch(IOException exc){System.out.println(exc.getMessage());}
        }
    }
}