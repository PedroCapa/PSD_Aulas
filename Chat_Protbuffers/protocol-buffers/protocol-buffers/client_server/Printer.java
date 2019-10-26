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
        byte[] ba = {0};
        ba[0] = (byte)x;
        envia(ba, cos);
    }
    //Testar a imprimir o menu
    static void menu(Sys.Builder sys, CodedOutputStream cos){
        String str = "";
        for(Room r: sys.getRoomList()){
            str = str + "\n" + r.getName();
        }
        
        byte[] ba = str.getBytes();
        envia(ba, cos);
    }

    static void envia(byte[] ba, CodedOutputStream cos){
        try{
            cos.writeFixed32NoTag(ba.length);
            cos.writeRawBytes(ba);
            cos.flush();
        }
        catch(java.io.IOException e){
            System.out.println(e.getMessage());
        }
    }


    static void conectado(CodedOutputStream cos, String name, Sys.Builder sys){
        for(Room r: sys.getRoomList()){
            if(r.getName().equals(name)){
                for(Chat c: r.getChatList()){
                    byte[] ba = c.toByteArray();
                    envia(ba, cos);
                }
                break;
            }
        }
    }
}
