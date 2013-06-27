package net.donizyo.hexempire;

import static net.donizyo.hexempire.Utils.DEBUG;

import java.awt.Canvas;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.swing.JOptionPane;

import net.donizyo.hexempire.util.AudioEntity;
import net.donizyo.hexempire.util.DoubleVector;
import net.donizyo.hexempire.util.IntVector;
import net.donizyo.hexempire.util.InvalidOperationException;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class Client implements Runnable, Communication, Configuration {
	private static final int x0 = 50;
	private static final int y0 = 10;
	private static final int d = 25;

	static final Logger logger = Logger.getLogger(Client.class.getSimpleName());
	final AtomicBoolean isGameOver;
	final ClientEntry clientEntry;
	final ClientTaskManager taskManager;
	final AudioManager audioManager;
	private final FpsManager fpsManager;
	private final Canvas canvas;
	private final ClientFrame frame;

	private String host;
	protected volatile int gameid = -1;
	protected volatile char movingNation = 1;
	protected volatile char moveLeft = 5;

	private AsynchronousChannelGroup channelGroup;
	AsynchronousSocketChannel channel;

	public Client(ClientFrame parentFrame, Canvas parentCanvas) throws IOException {
		frame = parentFrame;
		canvas = (parentFrame == null) ? parentCanvas : parentFrame.canvas;
		isGameOver = new AtomicBoolean(true);
		clientEntry = new ClientEntry(this);
		taskManager = new ClientTaskManager(this);
		fpsManager = new FpsManager();
		audioManager = new AudioManager() {

			@Override
			Set<AudioEntity> setAudioList() {
				String[] ss = {"D:/My Documents/Synthesia Music/G Major Music/4 - Harder/Danny Boy.mid"};
				Set<AudioEntity> entities = new HashSet<>(ss.length);
				try {
					entities.add(new AudioEntity(new File(ss[0])) {
						@Override
						public void run() {
						}
						@Override
						public void play() {
						}
						@Override
						public void playNow() {
						}
					});
				} catch (InvalidMidiDataException | IOException e) {
					e.printStackTrace();
				}
				return null;
			}
			
		};
		channelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(20));
	}

	private void initGL() {
		if (Utils.DEBUG)
			System.out.println("Initializing GL...");
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, canvas.getWidth(), 0, canvas.getHeight(), 1.0d, -1.0d);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glTranslatef(x0, y0, 0);
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
	}

	private static double a = Math.sqrt(3);
	private static double b = a / 2;

	private void drawGrid(int x, int y)
	{
		int p = x + d;
		int q = (int) (y + a * d);
		int r = (int) (y + b * d);
		GL11.glBegin(GL11.GL_POLYGON);
		GL11.glVertex2i(x, y);
		GL11.glVertex2i(p, y);
		GL11.glVertex2i((int) (x + 1.5 * d), r);
		GL11.glVertex2i(p, q);
		GL11.glVertex2i(x, q);
		GL11.glVertex2i((int) (x - .5 * d), r);
		GL11.glEnd();
	}
	
	private void drawGrids(int x, int y)
	{
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		for (int i = 0; i < LIM_COL; i++) {
			if (DEBUG)
				System.out.format("Drawing column : %d!%n", i);
			for (int j = 0; j < LIM_ROW; j++) {
				if (DEBUG)
					System.out.format("Drawing row : %d!%n", j);
				if ((j & 1) == 0)
					drawGrid(x + 3 * i * d, (int) (y + b * j * d));
				else
					drawGrid((int) (x + (3 * i - 1.5f) * d), (int) (y + b * j * d));
			}
		}
	}

	protected void drawGridInfo(int gid, int nid, int tn, int tm)
	{
		//TODO drawGridInfo implements
	}
	
	public void run() {
		try {
			Display.setParent(canvas);
			Display.setVSyncEnabled(true);
			Display.create();
		} catch (LWJGLException e1) {
			if (Utils.DEBUG)
				e1.printStackTrace();
			else
				logger.log(Level.SEVERE, null, e1);
		}
		System.out.println("Client running");
		initGL();
		System.out.println("Initializing FpsManager...");
		fpsManager.lastFPS = fpsManager.getTime();
		long sleepTime = 0l;
		long allowedInterval = 9_000_000_000l;
		long interval = 0l;
		while (!Display.isCloseRequested()) {
			fpsManager.updateFPS();//GL11.glViewport(0, 0, canvas.getWidth(), canvas.getHeight());
			if (!frame.isFocused()) {
				if (sleepTime == 0) {
					sleepTime = System.nanoTime();
				}
				interval = System.nanoTime() - sleepTime;
				if (interval >= allowedInterval) {
					interval = allowedInterval;
					Display.sync(FpsManager.FPS_DEFAULT_SLEEP);
					if (Utils.DEBUG)
						System.err.println("FrameNotForcused : Sleeping...");
				} else {
					Display.sync(FpsManager.FPS_DEFAULT_WAITING);
					if (Utils.DEBUG)
						System.err.println("FrameNotForcused : Waiting..." + interval);
				}
			} else {
				if (sleepTime != 0) {
					sleepTime = interval = 0;
				}
				drawGrids(0, 0);
				Display.update();
				try {
					mouseEvent();
				} catch (InvalidOperationException e) {
					if (Utils.DEBUG)
						e.printStackTrace();
					else
						logger.log(Level.SEVERE, null, e);
				}
				Display.sync(FpsManager.FPS_DEFAULT_RUNNING);
			}
		}
		Display.destroy();
		ClientEntry.destroyEngine();
	}

	private static final float h = (float) b;
	private static final DoubleVector a0 = new DoubleVector(1, 0);
	private static final DoubleVector b0 = new DoubleVector(.5f, h);
	private static final DoubleVector c0 = new DoubleVector(-.5f, h);
	private static final DoubleVector prj0 = new DoubleVector(x0, y0);
	private strictfp void mouseEvent() throws InvalidOperationException
	{
		if (Mouse.isButtonDown(0))
		{
			float x = Mouse.getX();
			float y = Mouse.getY();
			DoubleVector mouse = new DoubleVector(x, y);
			mouse.minusWith(prj0);
			DoubleVector a1 = mouse.minusWith(mouse.project(a0));
			DoubleVector b1 = mouse.minusWith(mouse.project(b0));
			DoubleVector c1 = mouse.minusWith(mouse.project(c0));
			DoubleVector coor = new DoubleVector(a1.size(), b1.size(), c1.size()).divideWith(a);
			IntVector intCoor = new IntVector(coor);
			System.out.println("Coordinate : " + intCoor);
		}
		clientEntry.mouseEvent(Mouse.getEventButton(), Mouse.getX(), Mouse.getY());
	}

	public void newServer(String name, int port) throws IOException {
		Server server = new Server(name, port);
		Server.add(server);
		connect(port);
	}

	protected boolean chat(String content) {
		if (content.length() < 0xFF)
			if (channel.write(ByteBuffer.wrap(Integer.toHexString(content.length() << HEXLEN * 2 | CHAT_NEW_BEGIN).getBytes(Utils.CHARSET))).isDone())
				if (channel.write(ByteBuffer.wrap(content.getBytes(Utils.CHARSET))).isDone())
					return channel.write(ByteBuffer.wrap(Integer.toHexString(CHAT_NEW_END).getBytes(Utils.CHARSET))).isDone();
		return false;
	}

	public void connect(int port) throws IOException
	{
		channel = AsynchronousSocketChannel.open(channelGroup);
		channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
		channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
		channel.connect(new InetSocketAddress(host, port), channel, new ConnectionHandler(this));
	}

	private class FpsManager {
		private static final int FPS_DEFAULT_RUNNING = 60;
		private static final int FPS_DEFAULT_SLEEP   = 1;
		private static final int FPS_DEFAULT_WAITING = 20;
		private int fps;
		private long lastFPS;
		private void updateFPS() {
			if (getTime() - lastFPS > 1000L) {
				fps = 0;
				lastFPS += 1000L;
			}
			fps += 1;
		}
		private long getTime() {
			return Sys.getTime() * 1000L / Sys.getTimerResolution();
		}
		public String toString() {
			return Integer.toString(fps);
		}
	}

	abstract class AudioManager {
		protected Set<AudioEntity> audioEntities;
		private Sequencer midi;
		private AudioManager() {
			try {
				midi = MidiSystem.getSequencer(false);
				midi.open();
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
			audioEntities = setAudioList();
		}

		abstract Set<AudioEntity> setAudioList();

		void createPlayer(final int audioId) throws InvalidMidiDataException, IOException {
			for (Iterator<AudioEntity> itr = audioEntities.iterator();
					itr.hasNext();) {
				AudioEntity entity = itr.next();
				if (entity.getId() == audioId) {
					createPlayer(entity);
					return;
				}
			}
		}

		void createPlayer(AudioEntity entity) throws InvalidMidiDataException, IOException {
			midi.setSequence(entity.getSequence());
			midi.start();
		}
	}

	protected void echo(String msg) {
		if (Utils.DEBUG) {
			System.out.println(msg);
		}
	}

	protected void send(int command) {
		channel.write(ByteBuffer.wrap(Integer.toHexString(command).getBytes(Utils.CHARSET)));
	}

	protected void notifyPeace(int nid, int command) {
		taskManager.queries.addLast(command);
		//TODO NOT IMPL, draw peace
		int result;
		if (frame != null) {
			result = JOptionPane.showConfirmDialog(frame, "Peace offer from the Nation [" + nid + "].",
					"Peace Treaty", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
			System.out.println(result);
		}
	}
}
