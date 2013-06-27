package net.donizyo.hexempire;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

class ClientFrame extends Frame implements Runnable {
	private static final long serialVersionUID = -5251662034771742635L;
	private static final String FRAME_DEFAULT_TITLE     = "Hexempire";
	private static final String FRAME_DEFAULT_ICONPATH  = "G:\\minecraftPROJECT\\MinecraftLauncher.png";
	private final Image icon = Toolkit.getDefaultToolkit().getImage(FRAME_DEFAULT_ICONPATH);
	private boolean isCloseRequested = false;
//	private final AtomicReference<Dimension> canvasSize = new AtomicReference<>();
	private SplashScreen splash;
	final Canvas canvas;
	private final Client client;
	
	private ClientFrame() throws IOException {
		this(FRAME_DEFAULT_TITLE);
	}

	public ClientFrame(String frameName) throws IOException {
		super(frameName);
		canvas = new Canvas();
		client = new Client(this, canvas);
		client.notifyPeace(3, 0x10101);//FIXME TEST
		super.setLayout(new BorderLayout());
		super.setIconImage(icon);
		canvas.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
//				canvasSize.set(canvas.getSize());
			}
		});
		super.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				canvas.requestFocusInWindow();
			}
		});
		super.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				isCloseRequested = true;
			}
		});
		super.add(canvas, BorderLayout.CENTER);
	}

	@Override
	public void run() {
		super.setPreferredSize(new Dimension(800, 600));
		super.setMinimumSize(new Dimension(800, 600));
		super.pack();
		super.setVisible(true);
		Thread clientThread = new Thread(client);
		clientThread.start();
		if (Utils.DEBUG) {
			System.out.println("Client thread[" + clientThread + "] started.");
		}
		while (!isCloseRequested) {
			// TODO nothing to do
			try {
				Thread.sleep(Utils.DELAY_LONG);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		dispose();
		System.exit(0);
	}

	public static void main(String[] args) {
		try {
			new Thread(new ClientFrame()).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
