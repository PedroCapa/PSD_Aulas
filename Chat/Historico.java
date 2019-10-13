import java.util.*;
import java.lang.Thread;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Historico{
	List<Mensagem> mensagens;
	Map<String, Utilizador> utilizador;
	Condition c;
	Lock l;

	public Historico(){
		this.utilizador = new HashMap<>();
		this.mensagens = new ArrayList<>();
		this.l = new ReentrantLock();
		this.c = l.newCondition();
	}

	public void adicionaMensagem(String username, String novo){
		l.lock();
		try{
			Mensagem m = new Mensagem(username, novo);
			this.mensagens.add(m);
		}
		finally{
			l.unlock();
		}
	}

	public int tamanho(){
		return mensagens.size();
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

	public Mensagem get(int i) throws IndexOutOfBoundsException{
		l.lock();
		try{
			return mensagens.get(i);
		}
		catch(IndexOutOfBoundsException e){
			throw new IndexOutOfBoundsException(e.getMessage());
		}
		finally{
			l.unlock();
		}
	}

	public boolean login(String nome, String pass){
		return this.utilizador.get(nome).login(pass);
	}

	public Map<String, Utilizador> getClientes(){
		return this.utilizador;
	}

	public Utilizador registar(String nome, String pass){
		if(this.utilizador.containsKey(nome)){
			return null;
		}
		else{
			return new Utilizador(nome, pass);
		}
	}
}

class Mensagem{
	private String s;
	private String mensagem;

	public Mensagem(String cs, String str){
		this.s = cs;
		this.mensagem = str;
	}

	public String getSocket(){
		return this.s;
	}

	public String getMensagem(){
		return this.mensagem;
	}

	public String toString(){
		return (this.s.toString() + ":" + this.mensagem);
	}
}