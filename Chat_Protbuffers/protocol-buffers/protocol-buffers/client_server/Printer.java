package client_server;

import client_server.Protos.Sys;
import client_server.Protos.Person;
import client_server.Protos.Chat;
import client_server.Protos.Room;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.AbstractList;

class Printer {
    
    static synchronized void print(Chat c) {
        //Ira receber um cliente/ cos onde tem que enviar a mensagem ira receber a mensagem
        System.out.println(c.getPerson() + ": " + c.getNote() + "\n");
    }


    static synchronized void printPerson(Sys s){
        for(Person p: s.getPersonList()){
            System.out.println("Nome " + p.getName() + "            " + "Pass: " + p.getPass());
        }
    }


    static void autenticacao(CodedOutputStream cos, int x){
        try{
            byte[] ba = {0};
            ba[0] = (byte)x;
            cos.writeFixed32NoTag(ba.length);
            cos.writeRawBytes(ba);
            cos.flush();
        }
        catch(java.io.IOException e){
            System.out.println(e.getMessage());
        } 

    }
    //Testar a imprimir o menu
    static void menu(Sys.Builder sys, CodedOutputStream cos){
        try{
            String str = "";
            for(Room r: sys.getRoomList()){
                str = str + "\n" + r.getName();
            }
            
            byte[] ba = str.getBytes();
            cos.writeFixed32NoTag(ba.length);
            cos.writeRawBytes(ba);
            cos.flush();
        }
        catch(java.io.IOException e){
            System.out.println(e.getMessage());
        }
    }


    static void conectado(CodedOutputStream cos, String name, Sys.Builder sys){
        try{
            System.out.println("Enviar mensagens da sala");
            for(Room r: sys.getRoomList()){
                if(r.getName().equals(name)){
                    System.out.println("Enviar mensagens da sala Desporto");
                    for(Chat c: r.getChatList()){
                        System.out.println("Enviei mensagem " + c.getNote());
                        byte[] ba = c.toByteArray();
                        cos.writeFixed32NoTag(ba.length);
                        cos.writeRawBytes(ba);
                        cos.flush();
                    }
                    break;
                }
            }
        }
        catch(java.io.IOException e){
            System.out.println(e.getMessage());
        }
    }
}
