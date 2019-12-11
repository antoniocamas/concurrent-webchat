package es.codeurjc.webchat.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class ChatRemoveUserTest {

	@Test
	public void test_01_GIVEN_Chat_When_removeUser_Then_allUserNotified() 
			throws InterruptedException, TimeoutException {

		final int numUsers = 3;
		CountDownLatch latch = new CountDownLatch(numUsers-1);
		DelayedAnswerWithCountDownForMockBuilder countDownLatch = 
				new DelayedAnswerWithCountDownForMockBuilder(latch);
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
			doAnswer(countDownLatch.build())
				.when(user)
				.userExitedFromChat(any(Chat.class), any(User.class));
		}
		
		chat.removeUser(users.get(0));
		
		latch.await(3000, TimeUnit.MILLISECONDS);
		chatManager.close();

		verify(users.get(0), never()).userExitedFromChat(any(Chat.class), any(User.class));
		for(int i = 1; i< numUsers;i++) {
			verify(users.get(i)).userExitedFromChat(chat, users.get(0));
		}
	}
	
	@Test
	public void test_02_GIVEN_Chat_When_usersSendingMessages_and_removeUser_Then_no_Race_Conditions() 
			throws Throwable {
		
		final int numUsers = 4;
		final int numOfMessagesPerUser = 50;
		ChatManager	chatManager = new ChatManager(1);
		TestConcurrencyManager concurrencyMngr = new TestConcurrencyManager(chatManager, numUsers);
		
		chatManager.newChat("testChat", 5, TimeUnit.SECONDS);
		
		for (int i=0 ; i < numUsers-1; i++)
		{
			int userNumber = i;
			
			concurrencyMngr.submitTask(
					() -> this.whenForTest_03_usersSendingMessages(
							chatManager, userNumber, numOfMessagesPerUser));
		}
		concurrencyMngr.submitTask(
			() -> this.whenForTest_03_userAddedAndRemoved(
					chatManager, numUsers, numOfMessagesPerUser));
		
		concurrencyMngr.assertThatAllExecutionsFinishOK(numUsers);
		
		concurrencyMngr.shutdownAllExecutors(); 
		
		assertThat(chatManager.getChat("testChat").getUsers().size()).isEqualTo(numUsers-1);
		assertThat(chatManager.getChat("testChat").getUser("userName_" + numUsers)).isNull();
	}
	
	private boolean whenForTest_03_usersSendingMessages(
			ChatManager chatManager, 
			final int userNumber, 
			final int numOfMessagesPerUser) throws InterruptedException, TimeoutException {
		
		chatManager.newUser(new TestUser("userName_" + userNumber));		

		chatManager.getChat("testChat").addUser(chatManager.getUser("userName_" + userNumber));
		String message = "userName_" + userNumber + " sending message ";
		for(int i=0; i < numOfMessagesPerUser; i++)
		{
			chatManager.getChat("testChat")
				.sendMessage(
						chatManager.getUser("userName_" + userNumber), 
						message + i);

		}
		return true;
	}
	
	private boolean whenForTest_03_userAddedAndRemoved(
			ChatManager chatManager, final int userNumber, int numOfMessagesPerUser) 
					throws InterruptedException, TimeoutException {

	chatManager.newUser(new TestUser("userName_" + userNumber));		

	String message = "userName_" + userNumber + " sending message ";
	for(int i=0; i < numOfMessagesPerUser; i++)
	{
		chatManager.getChat("testChat").addUser(chatManager.getUser("userName_" + userNumber));
		chatManager.getChat("testChat")
		.sendMessage(
				chatManager.getUser("userName_" + userNumber), 
				message + i);
		chatManager.getChat("testChat").removeUser(chatManager.getUser("userName_" + userNumber));
	}
	return true;
}

}
