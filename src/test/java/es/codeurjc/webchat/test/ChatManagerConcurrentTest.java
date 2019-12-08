package es.codeurjc.webchat.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.junit.Before;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;

public class ChatManagerConcurrentTest {

	private ChatManager chatManager;
	
	@Test
	public void test_01_GIVEN_ChatManger_When_newUser_Then_no_user_duplicated() throws Throwable {

		this.chatManager = new ChatManager(50);
		final int numUsers = 10;
		ExecutorService executor =	Executors.newFixedThreadPool(10);
		CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executor);
		
			
		for (int i=0 ; i < numUsers; i++)
		{
			completionService.submit(() -> this.test_01_createRepeadtedUser());
		}
		
		for (int i = 0; i<numUsers; i++) 
		{
			Future<Boolean> futureExecutionResut = completionService.take();
			boolean done = futureExecutionResut.isDone();
			assertThat(done).as("The execution of at least one executor has failed").isTrue();
		}
		
		this.chatManager.close();
		executor.shutdown();
		executor.awaitTermination(2, TimeUnit.SECONDS); 
		
		
		assertThat(chatManager.getUsers().size()).isEqualTo(1);
					
	}
	
	private boolean test_01_createRepeadtedUser() {
		try {
			chatManager.newUser(new TestUser("repeatedUserName"));
		} catch (IllegalArgumentException e) {}
		
		return true;
	}

	@Test
	public void test_02_GIVEN_ChatManger_When_newChat_Then_no_chat_duplicated() throws Throwable {

		final int numUsers = 4;
		final int numOfChatsPerUser = 50;
		this.chatManager = new ChatManager(numUsers*numOfChatsPerUser);
		ExecutorService executor =	Executors.newFixedThreadPool(numUsers);
		CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executor);
		
			
		for (int i=0 ; i < numUsers; i++)
		{
			int userNumber = i;
			completionService.submit(
					() -> this.test_02_createUsersAndChats(userNumber, numOfChatsPerUser)
			);
		}
		
		
		for (int i = 0; i<numUsers; i++) 
		{
			Future<Boolean> futureExecutionResult = completionService.take();
			
			assertThat(futureExecutionResult.get())
				.as("The execution of at least one executor has failed")
				.isTrue();
					
			assertThat(futureExecutionResult.isDone())
				.as("The execution of at least one executor has finished Unexpectedly")
				.isTrue();
		}
		
		this.chatManager.close();
		executor.shutdown();
		executor.awaitTermination(2, TimeUnit.SECONDS); 
		
		assertThat(chatManager.getChats().size()).isEqualTo(numOfChatsPerUser);
					
	}
	
	private boolean test_02_createUsersAndChats(final int userNumber, final int numOfChatsPerUser) throws InterruptedException, TimeoutException {
		
		chatManager.newUser(new TestUser("userName_" + userNumber));		
		
		Chat returnedChat;
		for(int i=0; i < numOfChatsPerUser; i++)
		{
			String chatName = "Chat_" + i;
			returnedChat = chatManager.newChat(chatName, 5, TimeUnit.SECONDS);
			if(!returnedChat.getName().equals(chatName)) {
				System.out.println(returnedChat.getName() +" != " + chatName);
				return false;
			}
		}
		return true;
	}

}