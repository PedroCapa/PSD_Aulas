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

    static BufferedReader in;
    static CodedOutputStream cos;
    static CodedInputStream  cis;

    public static void main(String[] args) {
        try{
            if(args.length<2)
                System.exit(1);
            String host = args[0];
            int port = Integer.parseInt(args[1]);

            Socket s = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(System.in));
            cis = CodedInputStream.newInstance(s.getInputStream());
            cos = CodedOutputStream.newInstance(s.getOutputStream());

            Person p = authentication();
            
            System.out.println("Bem vindo " + p.getName());

            Estado state = new Estado(1);
            (new LeServidor(cis, cos, state)).start();

            while (true) {
                String str = in.readLine();

                if(str != null && (state.state == 0 || state.state == 1 || state.state == -1)){
                    byte[] cb = str.getBytes();
                    Printer.envia(cb, cos);
                }
                else if(str != null && state.state == 100){
                    System.out.println("Entrei na sala");
                    Chat.Builder chat = Chat.newBuilder();
                    chat.
                        setPerson(p.getName()).
                        setNote(str);

                    byte [] cb = chat.build().toByteArray();

                    Printer.envia(cb, cos);
                }
                else if (str == null && state.state == 100){
                    System.out.println("Sair da sala");
                    state.state = 1;
                    byte[] ba = {'\0'};
                    cos.writeFixed32NoTag(1); //Da exceção pq estou a enviar um null
                    cos.writeRawBytes(ba);
                    cos.flush();
                }
                else{
                    System.out.println("Sair da app");
                    byte[] ba = {'\0'};
                    cos.writeFixed32NoTag(1); //Da exceção pq estou a enviar um null
                    cos.writeRawBytes(ba);
                    cos.flush();
                    break;
                }
            }
            s.shutdownOutput();
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("Shut down client");
    }

    public static Person authentication(){
        
        try{
                //Ler do teclado nome e pass
            System.out.println("Username");
            String username = in.readLine();

            System.out.println("Password");
            String password = in.readLine();
            
            //Enviar para servidor a autenticação

            Person person = Person.newBuilder()
                                .setName(username)
                                .setPass(password)
                                .build();

            byte [] ba = person.toByteArray();
            Printer.envia(ba, cos);

            //Processar o resultado e saltar para a próxima fase
            //Talvez deva substituir por um método a parte de receber do servidor

            int len = cis.readRawLittleEndian32();
            ba = cis.readRawBytes(len);
            int x = ba[0];

            if(x == -1){
                System.out.println("Palavra passe incorreta");
                return authentication();
            }
            else if(x == 0)
                System.out.println("Conta criada com sucesso");
            else 
                System.out.println("Sessão iniciada com sucesso");
            return person;
        }
        catch(IOException exc){}
        
        return null;
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
    Estado state;

    LeServidor(CodedInputStream cis, CodedOutputStream cos, Estado state) {
        this.cis = cis;
        this.cos = cos;
        this.state = state;
    }

    public void run() {
        try {
            while (true) {
                int len = cis.readRawLittleEndian32();
                byte[] ba = cis.readRawBytes(len);

                if(this.state.state == 100){
                    Chat chat = Chat.parseFrom(ba);
                    Printer.print(chat);
                }
                else if(ba[0] == -1){
                    System.out.println("A sala não existe");
                }
                else if(ba[0] == 0){
                    System.out.println("Entrou com sucesso\n");
                    this.state.state = 100;
                }
                else{
                    String str = new String(ba);
                    System.out.println("Salas de Chat disponiveis: (-_-)");
                    System.out.println(str);
                }
            }
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
      }
}

class Estado{

    int state;

    public Estado(int estado){
        this.state = state;
    }
}