import java.util.*;
import java.lang.Thread;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Historico{
	Map<String, Sala> salas;
	Map<String, Utilizador> utilizador;
	
	public Historico(){
		this.utilizador = new HashMap<>();
		this.salas = new HashMap<>();
	}

	public Map<String, Sala> getSalas(){
		return this.salas;
	}

	public Map<String, Utilizador> getUtilizador(){
		return this.utilizador;
	}

	public Sala getSala(String s){
		if(this.salas.containsKey(s))
			return this.salas.get(s);
		else
			return null;
	}

	public synchronized void adicionaMensagem(String username, String novo, String sala){
		this.salas.get(sala).adicionaMensagem(username, novo);
	}

	public synchronized int tamanho(String sala){
		return salas.get(sala).tamanho();
	}

	public synchronized Mensagem getMensagem(int i, String sala) throws IndexOutOfBoundsException{
		try{
			return salas.get(sala).getMensagem(i);
		}
		catch(IndexOutOfBoundsException e){
			throw new IndexOutOfBoundsException(e.getMessage());
		}
	}

	public synchronized boolean login(String nome, String pass){
		return this.utilizador.get(nome).login(pass);
	}

	public synchronized Map<String, Utilizador> getClientes(){
		return this.utilizador;
	}

	public synchronized Utilizador registar(String nome, String pass){
		if(this.utilizador.containsKey(nome)){
			return null;
		}
		else{
			return new Utilizador(nome, pass);
		}
	}
}

class Sala{
	private List<Mensagem> mensagem;
	private String nome;
	private Condition c;
	private Lock l;

	public Sala(List<Mensagem> m, String n){
		this.mensagem = new ArrayList<>(m);
		this.nome = n;
		this.l = new ReentrantLock();
		this.c = l.newCondition();
	}

	public List<Mensagem> getMensagem(){
		return this.mensagem();
	}

	public String getNome(){
		return this.nome;
	}

	public void adormece(){
		l.lock();
		try{
			c.await();
		}
		catch(InterruptedException e){}
		finally{
			l.unlock();
		}
	}

	public void acorda(){
		l.lock();
		try{
			c.signalAll();
		}
		finally{
			l.unlock();
		}
	}

	public synchronized int tamanho(){
		return this.mensagem.size();
	}

	public synchronized Mensagem getMensagem(int i) throws IndexOutOfBoundsException{
		try{
			return this.mensagem.get(i);
		}
		catch(IndexOutOfBoundsException e){
			throw new IndexOutOfBoundsException();
		}
	}

	public synchronized void adicionaMensagem(String username, String novo){
		Mensagem m = new Mensagem(username, novo);
		this.mensagem.add(m);
	}
}


class Mensagem{
	private String remetente;
	private String mensagem;

	public Mensagem(String r, String str){
		this.remetente = r;
		this.mensagem = str;
	}

	public String getSocket(){
		return this.remetente;
	}

	public String getMensagem(){
		return this.mensagem;
	}

	public String toString(){
		return (this.remetente.toString() + ":" + this.mensagem);
	}
}
