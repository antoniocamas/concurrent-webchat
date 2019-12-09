package es.codeurjc.webchat.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class ChatManagerNewUserTest {

	@Test
	public void test_01_GIVEN_Chat_When_newUser_Then_allUsersNotified() throws InterruptedException, TimeoutException {

		final int numUsers = 3;
		CountDownLatch latch = new CountDownLatch(numUsers);
		DelayedAnswerWithCountDownForMockBuilder countDownLatch = new DelayedAnswerWithCountDownForMockBuilder(latch);
		ChatManager chatManager = new ChatManager(5);
		List<User> users = new ArrayList<>();
		
		Chat chat = chatManager.newChat("testChat", 5, TimeUnit.SECONDS);
		for(int i=0; i<numUsers; i++) {
			users.add(mock(TestUser.class));
			when(users.get(i).getName()).thenReturn("user_" + i);
			chatManager.newUser(users.get(i));
			chat.addUser(users.get(i));
		}
		
		for(User user: users) {
			doAnswer(countDownLatch.build()).when(user).newUserInChat(any(Chat.class), any(User.class));
		}
		
		User newUser = mock(TestUser.class);
		when(newUser.getName()).thenReturn("theNewKidOnTheBlock");
		chatManager.newUser(newUser);
		chat.addUser(newUser);
		
		latch.await(3000, TimeUnit.MILLISECONDS);
		chatManager.close();
		
		for(User user: users) {
			verify(user).newUserInChat(chat, newUser);
		}
	}
	
	@Test
	public void test_02_GIVEN_ChatManger_When_newUser_Then_no_user_duplicated() throws Throwable {

		ChatManager chatManager = new ChatManager(50);
		final int numUsers = 10;
		TestConcurrencyManager concurrencyMngr = new TestConcurrencyManager(chatManager, numUsers); 		
			
		for (int i=0 ; i < numUsers; i++)
		{
			concurrencyMngr.submitTask(() -> this.whenForTest_02_createRepeadtedUser(chatManager));
		}
		
		concurrencyMngr.assertThatAllExecutionsFinishOK(numUsers);

		concurrencyMngr.shutdownAllExecutors(); 
				
		assertThat(chatManager.getUsers().size()).isEqualTo(1);
					
	}
	
	private boolean whenForTest_02_createRepeadtedUser(ChatManager chatManager) {
		try {
			chatManager.newUser(new TestUser("repeatedUserName"));
		} catch (IllegalArgumentException e) {}
		
		return true;
	}


	
	
}