package client_server;

import client_server.Protos.Sys;
import client_server.Protos.Person;
import client_server.Protos.Chat;


import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.*;
import java.net.*;

import java.util.List;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        try{
            int port = java.lang.Integer.parseInt(args[0]);
            ServerSocket srv = new ServerSocket(port);
            Sys.Builder sys = Sys.newBuilder();
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
    Sys.Builder sys;
    Output out;

    ClientHandler(CodedInputStream cis, CodedOutputStream cos, Sys.Builder sys, Output out) {
        this.cis = cis;
        this.cos = cos;
        this.sys = sys;
        this.out = out;
    }

    public void run() {
        try {
            //Fase de autenticação
            authentication();

            //Troca de mensagens
            while (true) {
                int len = cis.readRawLittleEndian32(); //Se a pessoa que escreveu enviar para toda a gente o read vai ser lido por todos
                byte[] ba = cis.readRawBytes(len);
                Chat chat = Chat.parseFrom(ba);
                Printer.print(chat);          
                //Adicionar ao chat a mensagem Talvez fazer no print pq do synchronized
                this.sys.addChat(chat);
                this.out.sendChat(chat);

                //Printer.print(sys, null, p, this.out, null);//Enviar para toda a gente a mensagem
            }
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void authentication(){
        
        try{
        int len = cis.readRawLittleEndian32();
        byte[] per = cis.readRawBytes(len);

        Person p = Person.parseFrom(per);
        int x = addPerson(p);

        if(x == -1){
            byte[] ba = {0};
            ba[0] = (byte)x;
            cos.writeFixed32NoTag(ba.length);
            cos.writeRawBytes(ba);
            cos.flush();
            authentication();   
        }
            byte[] ba = {0};
            ba[0] = (byte)x;
            cos.writeFixed32NoTag(ba.length);
            cos.writeRawBytes(ba);
            cos.flush();

            for(Chat c: sys.getChatList()){
                ba = c.toByteArray();
                cos.writeFixed32NoTag(ba.length);
                cos.writeRawBytes(ba);
                cos.flush();
            }
        }
        catch(java.io.IOException e){
            System.out.println(e.getMessage());
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public synchronized int addPerson(Person person){

        List<Person> persons = this.sys.getPersonList();
        boolean flag = false;
        System.out.println(persons.size());
        for(Person p: persons){
            flag = p.getName().equals(person.getName());
            if(flag){
                System.out.println(p.getName() + "   " + p.getPass());
                String real_passwd = p.getPass();
                String used_passwd = person.getPass();
                if(real_passwd.equals(used_passwd)){

                    return 1;
                }
                else {
                    return -1;
                }
            }
        }
        sys.addPerson(person);
        System.out.println("Não existe ninguem com o nome " + person.getName());
        return 0;
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