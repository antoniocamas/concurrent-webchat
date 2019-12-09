package es.codeurjc.webchat.test;

import java.util.concurrent.CountDownLatch;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class DelayedAnswerWithCountDownForMockBuilder {
	
	private CountDownLatch latch;
	private long timeToSleep = 0;
	
	public DelayedAnswerWithCountDownForMockBuilder delay(long timeToSleep) {
		this.timeToSleep = timeToSleep;
		return this;
	}

	public DelayedAnswerWithCountDownForMockBuilder(CountDownLatch latch, long timeToSleep) {
		this.latch = latch;
		this.timeToSleep = timeToSleep;
	}
	
	public DelayedAnswerWithCountDownForMockBuilder(CountDownLatch latch) {
		this.latch = latch;
	}
	
	public Answer<Void> build() throws InterruptedException {
		Answer<Void> answer =new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws InterruptedException {
				Thread.sleep(timeToSleep);
				latch.countDown();
				return null;
			}
		};
		waitForTheAnswerToBeWired();
		return answer;
	}
	
	private void waitForTheAnswerToBeWired() throws InterruptedException {
		Thread.sleep(10);
	}

}
