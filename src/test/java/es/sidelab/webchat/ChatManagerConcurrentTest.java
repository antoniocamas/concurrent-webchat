package es.sidelab.webchat;

import static org.assertj.core.api.Assertions.assertThat;

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

public class ChatManagerConcurrentTest {

	private ChatManager chatManager = new ChatManager(50);
	
	@Test
	public void test_01_GIVEN_ChatManger_When_newUser_Then_no_user_duplicated() throws Throwable {

		final int numUsers = 10;
		ExecutorService executor =	Executors.newFixedThreadPool(10);
		CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executor);
		
			
		for (int i=0 ; i < numUsers; i++)
		{
			int userNumber = i;
			completionService.submit(() -> this.test_01_userActionSimulator(userNumber));
		}
		
		for (int i = 0; i<numUsers; i++) 
		{
			Future<Boolean> futureExecutionResut = completionService.take();
			boolean done = futureExecutionResut.isDone();
			assertThat(done).as("The execution of at least one executor has failed").isTrue();
		}
		
		assertThat(chatManager.getUsers().size()).isEqualTo(1);
					
	}
	
	private boolean test_01_userActionSimulator(final int userNumber) {
		try {
			chatManager.newUser(new TestUser("repeatedUserName"));
		} catch (IllegalArgumentException e) {}
		
		return true;
	}

	@Test
	public void test_02_GIVEN_ChatManger_When_newChat_Then_no_chat_duplicated() throws Throwable {

		final int numUsers = 10;
		ExecutorService executor =	Executors.newFixedThreadPool(10);
		CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executor);
		
			
		for (int i=0 ; i < numUsers; i++)
		{
			int userNumber = i;
			completionService.submit(() -> this.test_02_userActionSimulator(userNumber));
		}
		
		for (int i = 0; i<numUsers; i++) 
		{
			Future<Boolean> futureExecutionResut = completionService.take();
			boolean done = futureExecutionResut.isDone();
			assertThat(done).as("The execution of at least one executor has failed").isTrue();
		}
		
		assertThat(chatManager.getChats().size()).isEqualTo(10);
					
	}
	
	private boolean test_02_userActionSimulator(final int userNumber) throws InterruptedException, TimeoutException {
		try {
			chatManager.newUser(new TestUser("userName_" + userNumber));
		} catch (IllegalArgumentException e) {}
		
		for(int i=0; i < 10; i++)
		{
			chatManager.newChat("Chat_" + i, 5, TimeUnit.SECONDS);
		}
		
		return true;
	}

}