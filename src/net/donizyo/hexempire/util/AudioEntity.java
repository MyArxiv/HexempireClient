package net.donizyo.hexempire.util;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.spi.MidiFileReader;

public abstract class AudioEntity implements Runnable {
	private final int audioId;
	private final File file;
	MidiFileReader reader;
	private final Sequence sequence;

	protected AudioEntity(File audioFile) throws InvalidMidiDataException, IOException {
		file = audioFile;
		audioId = register();
		sequence = MidiSystem.getSequence(file);
	}

	public int getId() {
		return audioId;
	}

	public File getSource() {
		return file;
	}

	public Sequence getSequence() {
		return sequence;
	}

	private native int register();

	public abstract void play();

	public abstract void playNow();

	public static AudioEntity newInstance() {
		return null;
	}

}
