package es.codeurjc.webchat.test;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.ConcurrentModificationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class ChatManagerAndChatConcurrencyTest {

	private ChatManager chatManager = new ChatManager(50);
	
	@Test
	public void GIVEN_ChatManger_and_Users_When_newChat_Then_no_exception() 
			throws Throwable {

		System.out.println("Starting Test Case");
		final int numUsers = 4;
		
		ExecutorService executor =	Executors.newFixedThreadPool(numUsers);
		CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executor);
		
			
		for (int i=0 ; i < numUsers; i++)
		{
			int userNumber = i;
			completionService.submit(() -> this.userActionSimulator(userNumber));
		}
	
		for (int i = 0; i<numUsers; i++) 
		{
			Future<Boolean> futureExecutionResut = completionService.take();
			
			assertThat(futureExecutionResut.isDone())
				.as("The execution of at least one user has failed")
				.isTrue();
			
			boolean success = futureExecutionResut.get();
			assertThat(success).as("The execution of at least one user has failed").isTrue();
		}
		
		this.chatManager.close();
		
		executor.shutdown();
		executor.awaitTermination(2, TimeUnit.SECONDS); 
		
		assertThat(chatManager.getUsers().size()).isEqualTo(numUsers);
		assertThat(chatManager.getChats().size()).isEqualTo(5);
		for (Chat chat: chatManager.getChats()) {
			assertThat(chat.getUsers().size()).isEqualTo(numUsers);
		}
	}
	
	private boolean userActionSimulator(final int userNumber) 
			throws InterruptedException, TimeoutException {
		
		User user = new TestUser("userName_" + userNumber);
		
		System.out.println(
				"[" + Thread.currentThread().getName() + "]" + "Creating user " + user.getName());
		
		try {
			try {
				chatManager.newUser(user);
			} catch (IllegalArgumentException e) {
				return false;
			}
			
			for(int i=0; i < 5; i++)
			{
				Chat chat = chatManager.newChat("Chat_" + i, 5, TimeUnit.SECONDS);
				chat.addUser(user);
				
				for(User userInChat: chat.getUsers())
				{
					System.out.println(
							"[" + Thread.currentThread().getName() + "]" 
									+ "Chat " + chat.getName() + " has this users " 
									+ userInChat.getName());
				}
			}
		} catch (ConcurrentModificationException exc) {
			System.out.println("ConcurrentModificationException in user " + user.getName() + " ");
			return false;
		}
		
		return true;
	}
	
}