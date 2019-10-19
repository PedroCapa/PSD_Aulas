package client_server;

import client_server.Protos.Sys;
import client_server.Protos.Person;
import client_server.Protos.Chat;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.AbstractList;

class Printer {

    static synchronized void print(Sys sys, String note, Person p, Output out, CodedOutputStream cos) throws java.io.IOException{
        //Verificar se o get retorna a lista ou uma copia da lista
        try{
            if(note == null){
                /*List<Person> list = */
                //list.add(p);
                System.out.println("--------------------------------");
                System.out.println("Tenho " + sys.getPersonList().size() + " pessoas");
                Sys.Builder b = Sys.newBuilder().addAllPerson(sys.getPersonList());
                System.out.println("Adicionei todas as pessoas " + b.getPersonList().size());
                b.addPerson(Person.newBuilder().setName(p.getName()).setPass(p.getPass()).build());
                
                /*
                for(Person per: sys.getPersonList()){
                    System.out.println("Dentro do for para adicionar à lista");
                    System.out.println("                 Pessoa:" + per.getName());
                    b.addPerson(Person.newBuilder().setName(per.getName()).setPass(per.getPass()).build());
                }
                */
                sys = b.build();

                System.out.println("Composto por: " + sys.getPersonList().size() + " pessoas");
                for(Person per: sys.getPersonList()){
                    System.out.println("Pessoa: " + per.getName());
                }

                System.out.println("--------------------------------\n\n\n\n");                

                for (Chat c : sys.getChatList()){
                    byte[] ba = c.getNote().getBytes();
                    cos.writeFixed32NoTag(ba.length);
                    cos.writeRawBytes(ba);
                    cos.flush();
                }
            }
            else{  
                Sys.Builder b = Sys.newBuilder().addAllChat(sys.getChatList()).addAllPerson(sys.getPersonList());
                System.out.println("Enviei a mensagem e tenho " +   b.getPersonList().size()  + " pessoas");
                b.addChat(Chat.newBuilder().setNote(note).setPerson(p.getName()).build());

                sys = b.build();

                /*sys.getChatList().add(Chat.newBuilder()
                                .setNote(p.getName())
                                .setPerson(p.getPass())
                                .build());*/
                byte[] ba = note.getBytes();
                for(CodedOutputStream c: out.cos){
                    c.writeFixed32NoTag(ba.length);
                    c.writeRawBytes(ba);
                    c.flush();
                }
            }
        }
        catch(java.io.IOException e){
            System.out.println(e.getMessage());
        }
    }

    static synchronized void print(Chat c) {
        //Ira receber um cliente/ cos onde tem que enviar a mensagem ira receber a mensagem
        System.out.println(c.getPerson() + ": " + c.getNote() + "\n");
  }


    static synchronized void printPerson(Sys s){
        for(Person p: s.getPersonList()){
            System.out.println("Nome " + p.getName() + "            " + "Pass: " + p.getPass());
        }
    }


}

