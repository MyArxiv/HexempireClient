package net.donizyo.hexempire;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

@SuppressWarnings("serial")
public class ClientApplet extends Applet {
	private Canvas canvas;
	private Thread main;
	private AtomicBoolean isRunning = new AtomicBoolean(false);

	private void startLWJGL() {
		main = new Thread() {
			public void run() {
				isRunning.set(true);
				try {
					Display.setParent(canvas);
					Display.create();
					initGL();
				} catch (LWJGLException e) {
					e.printStackTrace();
					return;
				}
				loop();
			}
		};
		main.start();
	}

	private void stopLWJGL() {
		isRunning.set(false);
		try {
			main.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void init() {
		setLayout(new BorderLayout());
		canvas = new Canvas() {
			public final void addNotify() {
				super.addNotify();
				startLWJGL();
			}
			public final void removeNotify() {
				stopLWJGL();
				super.removeNotify();
			}
		};
		canvas.setSize(getWidth(), getHeight());
		add(canvas);
		canvas.setFocusable(true);
		canvas.requestFocus();
		canvas.setIgnoreRepaint(true);
		setVisible(true);
	}

	public void start() {
		
	}

	public void stop() {
		
	}

	public void destroy() {
		remove(canvas);
		super.destroy();
	}

	public void initGL() {
		
	}

	public void loop() {
		while (isRunning.get()) {
			Display.sync(60);
			Display.update();
		}
		Display.destroy();
	}
}
