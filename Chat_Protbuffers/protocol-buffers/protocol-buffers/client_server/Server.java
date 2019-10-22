package client_server;

import client_server.Protos.Sys;
import client_server.Protos.Person;
import client_server.Protos.Chat;
import client_server.Protos.Room;


import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.io.*;
import java.net.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Server {
    public static void main(String[] args) {
        try{
            int port = java.lang.Integer.parseInt(args[0]);
            ServerSocket srv = new ServerSocket(port);
            Sys.Builder sys = Sys.newBuilder();
            Output out = new Output();
            addRoom(sys, out);
            while (true) {
                Socket cli=srv.accept();
                CodedInputStream cis = CodedInputStream.newInstance(cli.getInputStream());
                CodedOutputStream cos = CodedOutputStream.newInstance(cli.getOutputStream());
                (new ClientHandler(cis, cos, sys, out)).start();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void addRoom(Sys.Builder sys, Output out){
        List<Chat> chat = new ArrayList<>();
        Room r1 = Room.newBuilder().setName("Desporto").build();
        Room r2 = Room.newBuilder().setName("Politica").build();

        sys.addRoom(r1);
        sys.addRoom(r2);

        out.salas.put("Desporto", new ArrayList<>());
        out.salas.put("Politica", new ArrayList<>());        
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
            int len = cis.readRawLittleEndian32();
            byte[] per = cis.readRawBytes(len);

            Person p = Person.parseFrom(per);
            int x = addPerson(p);
            
            Printer.autenticacao(this.cos, x);
            
            if(x == -1)
                return;

            while (true) {
                Printer.menu(this.sys, this.cos); 
    
                len = cis.readRawLittleEndian32();
                byte[] ba = cis.readRawBytes(len);

                if(this.out.salas.containsKey(new String(ba))){
                    byte[] by = {0};
                    cos.writeFixed32NoTag(by.length);
                    cos.writeRawBytes(by);
                    cos.flush();
                    chat_room(new String(ba));
                }
                else{
                    //Envia mensagem a dizer que a sala não existe
                    byte[] bytes = {-1};
                    cos.writeFixed32NoTag(bytes.length);
                    cos.writeRawBytes(bytes);
                    cos.flush();
                }
                //Caso ele faça ctrl D ele acaba a thread
            }
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
      }

    public void chat_room(String name){
        try{
            addCoded(name, this.cos);
            Printer.conectado(this.cos, name, this.sys); //para imprimir todas as mensagens da sala

            int len = 1;

            while(len != 0){
                len = cis.readRawLittleEndian32();
                byte[] ba = cis.readRawBytes(len);
                Chat chat = Chat.parseFrom(ba);
                System.out.println(chat.getNote());
                //Talvez verificar se foi usado o ctrl D
                addChat(chat, name);//Alterar esta pq tem se que ir buscar o room e so depois e que se adiciona o chat
                this.out.sendChat(chat, name);
            }
            removeCoded(name, cos);
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    public synchronized int addPerson(Person person){

        List<Person> persons = this.sys.getPersonList();
        boolean flag = false;
        Person per = Person.newBuilder().setName("admin").setPass("admin").build();

        for(Person p: persons){
            flag = p.getName().equals(person.getName());
            if(flag){
                System.out.println(p.getName() + "   " + p.getPass());
                Person kk = Person.newBuilder().setName(p.getName()).setPass(p.getPass()).build();
                per = kk;
                System.out.println(per.getName() + "   " + per.getPass());
                break;
            }
        }
        if(!flag){
            sys.addPerson(person);
            System.out.println("Não existe ninguem com o nome " + person.getName());
            return 0;
        }
        else if(!person.getPass().equals(per.getPass())){
            System.out.println("Palavra passe errada para o nome " + person.getName()); 
            return -1;
        }

        else{
            System.out.println("Login com sucesso para o nome " + person.getName()); 
            return 1;
        }
    }

    public synchronized void addChat(Chat c, String name){
        List<Room> room = this.sys.getRoomList();
        for(int i = 0; i < room.size(); i++){
            Room r = room.get(i);
            if(r.getName().equals(name)){
                r = r.toBuilder().addChat(c).build();
                this.sys.setRoom(i, r);
                break;
            }
        }
    }

    public void addCoded(String name, CodedOutputStream cos){
        this.out.add(name, cos);
    }

    public void removeCoded(String name, CodedOutputStream cos){
        this.out.remove(name, cos);        
    }
}


class Output{
    Map<String, List<CodedOutputStream>> salas;

    public Output(Map<String, List<CodedOutputStream>> salas){
        this.salas = salas;
    }

    public Output(){
        this.salas = new HashMap<>();
    }

    public void add(String name, CodedOutputStream c){
        this.salas.get(name).add(c);
    }

    public void remove(String name, CodedOutputStream c){
        this.salas.get(name).remove(c);
    }    

    public void sendChat(Chat chat, String name){
        byte [] cb = chat.toByteArray();
        for(CodedOutputStream output_stream: this.salas.get(name)){
            try{
                output_stream.writeFixed32NoTag(cb.length);
                output_stream.writeRawBytes(cb);
                output_stream.flush();
            }
            catch(IOException exc){System.out.println(exc.getMessage());}
        }
    }
}