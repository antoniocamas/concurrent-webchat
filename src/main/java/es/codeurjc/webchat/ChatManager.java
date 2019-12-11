package es.codeurjc.webchat;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ChatManager {

	private Map<String, Chat> chats = new ConcurrentHashMap<>();
	private Map<String, User> users = new ConcurrentHashMap<>();
	private Map<String, ExecutorService> executors = new ConcurrentHashMap<>();
	private final Semaphore chatSemaphore; 
	
	public ChatManager(int maxChats) {
		chatSemaphore = new Semaphore(maxChats, true);
	}

	public void newUser(User user) {
		//To avoid exclusion here putting the executor has to be previous to 
		// putting the user. Otherwise there will be race conditions when a 
		// users are iterated and executor for a given user is tried to be used.
		
		executors.putIfAbsent(user.getName(), Executors.newSingleThreadExecutor());
		User retUser = users.putIfAbsent(user.getName(), user);
				
		if(retUser != null){
			throw new IllegalArgumentException("There is already a user with name \'"
					+ user.getName() + "\'");
		}
	}

	public Chat newChat(String name, long timeout, TimeUnit unit) throws InterruptedException,
			TimeoutException {

		if (chatSemaphore.tryAcquire(timeout, unit)) {
			Chat chat = new Chat(this, name);
			Chat retChat = chats.putIfAbsent(name, chat);
			if(retChat == null) {
				for(User user : users.values()){
					this.launchCommandInUser(user.getName(), ()-> user.newChat(chat));
				}
			}
			else {
				chatSemaphore.release();
			}
		}
		else {
			throw new TimeoutException("There is no enought capacity to create a new chat");
		}
		return chats.get(name);
	}
	

	public void closeChat(Chat chat) {
		Chat removedChat = chats.remove(chat.getName());
		if (removedChat == null) {
			throw new IllegalArgumentException(
					"Trying to remove an unknown chat with name \'"
							+ chat.getName() + "\'");

		}

		chatSemaphore.release();
		
		for(User user : users.values()){
			this.launchCommandInUser(user.getName(), ()-> user.chatClosed(removedChat));
		}

	}

	public void launchCommandInUser(String userName, Runnable command) {
		Executor executor = executors.get(userName);
		if(executor!=null) {
			executors.get(userName).execute(command);
		}
	}
	
	public Collection<Chat> getChats() {
		return Collections.unmodifiableCollection(chats.values());
	}

	public Chat getChat(String chatName) {
		return chats.get(chatName);
	}

	public Collection<User> getUsers() {
		return Collections.unmodifiableCollection(users.values());
	}

	public User getUser(String userName) {
		return users.get(userName);
	}

	public void close() throws InterruptedException {
		for(User u : users.values()){
			executors.get(u.getName()).shutdown();
			executors.get(u.getName()).awaitTermination(1, TimeUnit.SECONDS);
		}
	}
}
