package com.raltamirano.midipedalboard;

import com.raltamirano.midipedalboard.model.Song;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sound.midi.*;
import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

/**
 * <p>MIDI Pedalboard application.</p>
 * <br />
 * Use the following VM option to set the base directory for MIDI files:
 * <br /><br />
 * -Dmp.midiFilesBaseDir=r:\all\projects\dev\raltamirano.github\midi-pedalboard\src\etc\midi\
 * <br /><br />
 * Use the following VM options to define input/output MIDI ports:
 * <br /><br />
 * <p>-Dmp.inputPort="loopMIDI Port"</p>
 * <p>-Dmp.outputPort="loopMIDI Port 1"</p>
 */
@SpringBootApplication
public class MidiPedalboardApplication implements CommandLineRunner {
	private static final String BASE_DIR = System.getProperty("mp.midiFilesBaseDir");

	private Transmitter inputPort;
	private Receiver outputPort;
	private Orchestrator orchestrator;

	public static void main(String[] args) {
		SpringApplication.run(MidiPedalboardApplication.class, args);
	}

	@Override
	public void run(String... args) {
		outputPort = getOutputPort();
		final Song song = new Song("Demo 1", 110);

		song.createPattern("Pattern 1")
				.appendGroove(new File(BASE_DIR + "groove1.mid"))
					.appendFill(new File(BASE_DIR + "groove1-fill1.mid"))
					.appendFill(new File(BASE_DIR + "groove1-fill2.mid"));

		song.createPattern("Pattern 2")
				.appendGroove(new File(BASE_DIR + "groove2.mid"))
					.appendFill(new File(BASE_DIR + "groove2-fill1.mid"))
					.appendFill(new File(BASE_DIR + "groove2-fill2.mid"));

		orchestrator = new Orchestrator(song, outputPort);

		final Scanner scanner = new Scanner(System.in);
		char c = scanner.next().charAt(0);
		while (c != 'q') {
			switch (c) {
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
					orchestrator.play("Pattern " + c);
					break;

				case 's':
					orchestrator.stop();
					break;

				default:
					break;
			}
			c = scanner.next().charAt(0);
		}

		System.exit(0);
	}

	private Transmitter getInputPort() {
		try {
			final MidiDevice midiDevice = MidiSystem.getMidiDevice(
					Arrays.stream(MidiSystem.getMidiDeviceInfo()).filter(
							d -> d.getName().equals(System.getProperty("mp.inputPort")))
							.findFirst().get());
			midiDevice.open();
			return midiDevice.getTransmitter();
		} catch (final MidiUnavailableException e) {
			throw new RuntimeException(e);
		}
	}

	private Receiver getOutputPort() {
		try {
			final MidiDevice midiDevice = MidiSystem.getMidiDevice(
					Arrays.stream(MidiSystem.getMidiDeviceInfo()).filter(
							d -> d.getName().equals(System.getProperty("mp.outputPort")))
							.findFirst().get());
			midiDevice.open();
			return midiDevice.getReceiver();
		} catch (final MidiUnavailableException e) {
			throw new RuntimeException(e);
		}
	}
}
