import java.util.*;
import java.lang.Thread;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Utilizador{
	String nome;
	String pass;
	Lock l = new ReentrantLock();
	Condition autenticado = l.newCondition();

	public Utilizador(String n, String p){
		this.nome = n;
		this.pass = p;
	}

	public boolean login(String p){
		return this.pass.equals(p);
	}
}