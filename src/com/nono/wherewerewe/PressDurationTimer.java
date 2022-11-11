package com.nono.wherewerewe;

public class PressDurationTimer implements Runnable {
	private final LongpressListener listener;
	
	public PressDurationTimer(LongpressListener listener) {
		this.listener = listener;
	}
	
	public void run() {
		listener.longPress();
	}
}
