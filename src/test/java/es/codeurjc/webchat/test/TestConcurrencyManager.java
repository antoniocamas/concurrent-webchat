package es.codeurjc.webchat.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import es.codeurjc.webchat.ChatManager;

public class TestConcurrencyManager {
	
	private ExecutorService executor;
	private CompletionService<Boolean> completionService;
	private ChatManager chatManager;

	public TestConcurrencyManager(ChatManager chatManager, int numberOfExecutors) {
		this.chatManager = chatManager;
		this.executor =	Executors.newFixedThreadPool(numberOfExecutors);
		this.completionService = new ExecutorCompletionService<>(executor);
	}
	
	public void submitTask(Callable<Boolean> callable) {
		this.completionService.submit(callable);
	}

	public void assertThatAllExecutionsFinishOK(final int numTask)
			throws InterruptedException, ExecutionException {
		for (int i = 0; i<numTask; i++) 
		{
			Future<Boolean> futureExecutionResult = this.completionService.take();
			
			assertThat(futureExecutionResult.get())
				.as("The execution of at least one executor has failed")
				.isTrue();
					
			assertThat(futureExecutionResult.isDone())
				.as("The execution of at least one executor has finished Unexpectedly")
				.isTrue();
		}
	}
		
	public void shutdownAllExecutors() throws InterruptedException {
		this.chatManager.close();
		this.executor.shutdown();
		this.executor.awaitTermination(2, TimeUnit.SECONDS);
	}


	
}
