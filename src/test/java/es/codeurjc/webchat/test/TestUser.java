package es.codeurjc.webchat.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.User;

public class TestUser implements User {

	private String name;
	
	private List<String> errors = new ArrayList<>();

	public List<String> getErrors() {
		return errors;
	}

	public TestUser(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public String getColor(){
		return "007AFF";
	}

	@Override
	public void newChat(Chat chat) {
		println("New chat " + getChatNameCountingErrors(chat));
	}

	@Override
	public void chatClosed(Chat chat) {
		println("Chat " + getChatNameCountingErrors(chat) + " closed ");
	}

	@Override
	public void newUserInChat(Chat chat, User user) {
		println("New user " + user.getName() + " in chat " + getChatNameCountingErrors(chat));
	}

	@Override
	public void userExitedFromChat(Chat chat, User user) {
		println("User " + user.getName() + " exited from chat " + getChatNameCountingErrors(chat));
	}

	@Override
	public void newMessage(Chat chat, User user, String message) {
		println("New message '" + message + "' from user " + user.getName()
				+ " in chat " + getChatNameCountingErrors(chat));
	}

	@Override
	public String toString() {
		return "User[" + name + "]";
	}
	
	private void println(String message) {
		System.out.println("[" + Thread.currentThread().getName() + "][" + this.name + "] " + message);
	}
	
	private String getChatNameCountingErrors(Chat chat) {
		try {
			return chat.getName();
		} catch (Exception exc) {
			saveException(exc);
			throw exc;
		}
	}
	private void saveException(Exception exc) {
		StringWriter sw = new StringWriter();
		exc.printStackTrace(new PrintWriter(sw));
		this.errors.add(sw.toString());
	}
	
}
