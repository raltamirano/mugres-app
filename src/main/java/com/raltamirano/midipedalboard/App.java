package com.raltamirano.midipedalboard;

import com.raltamirano.midipedalboard.commands.Note;
import com.raltamirano.midipedalboard.commands.Play;
import com.raltamirano.midipedalboard.commands.Wait;
import com.raltamirano.midipedalboard.common.Key;
import com.raltamirano.midipedalboard.filters.*;
import com.raltamirano.midipedalboard.model.Action;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sound.midi.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static com.raltamirano.midipedalboard.common.Value.WHOLE;
import static javax.sound.midi.ShortMessage.NOTE_OFF;
import static javax.sound.midi.ShortMessage.NOTE_ON;

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
public class App implements CommandLineRunner {
	private Pedalboard pedalboard = null;

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Override
	public void run(String... args) {
		final Transmitter inputPort = createInputPort();
		final Receiver outputPort = createOutputPort();

		pedalboard = new Pedalboard(outputPort);
		pedalboard.getSong().setKey(Key.CS);

		// Actions for every pedal
		pedalboard.getSong().setAction(1, playNote(61, 100, 1));
		pedalboard.getSong().setAction(2, playNote(63, 100, 1));
		pedalboard.getSong().setAction(3, playNote(65, 100, 1));
		pedalboard.getSong().setAction(4, playNote(66, 100, 1));
		pedalboard.getSong().setAction(5, playNote(68, 100, 1));

		// Configure output processor filters
		pedalboard.getProcessor().appendFilter(new Monitor("Input"));

//		pedalboard.getProcessor().appendFilter(new Legato());
//		pedalboard.getProcessor().appendFilter(new Toggle());
//		pedalboard.getProcessor().appendFilter(new Octave(-1));
//		pedalboard.getProcessor().appendFilter(new Chord());
//		pedalboard.getProcessor().appendFilter(new Arp("1e3e1e3e23"));

        pedalboard.getProcessor().appendFilter(new FixNoteLength(WHOLE));
		pedalboard.getProcessor().appendFilter(new Monitor("Output"));

//		pedalboard.getSong().createPattern("Intro")
//				.appendGroove(new File(BASE_DIR + "groove2-fill1.mid"));
//
//		pedalboard.getSong().createPattern("Pattern 1")
//				.appendGroove(new File(BASE_DIR + "groove1.mid"))
//					.appendFill(new File(BASE_DIR + "groove1-fill1.mid"))
//					.appendFill(new File(BASE_DIR + "groove1-fill2.mid"));
//
//		pedalboard.getSong().createPattern("Pattern 2")
//				.appendGroove(new File(BASE_DIR + "groove2.mid"))
//					.appendFill(new File(BASE_DIR + "groove2-fill1.mid"))
//					.appendFill(new File(BASE_DIR + "groove2-fill2.mid"));
//
//		pedalboard.getSong().setAction(1, playPattern("Intro")
//									.then(waitFor(HALF_SECOND))
//									.then(playPattern("Pattern 1")));
//		pedalboard.getSong().setAction(2, playPattern("Pattern 1"));
//		pedalboard.getSong().setAction(3, playPattern("Pattern 2"));
//		pedalboard.getSong().setAction(4, FINISH);
//		pedalboard.getSong().setAction(5, STOP);

		final Scanner scanner = new Scanner(System.in);
		char c = scanner.next().charAt(0);
		while (c != 'q') {
			switch (c) {
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
					pedalboard.pedal(Integer.parseInt(String.valueOf(c)), true);
				default:
					break;
			}
			c = scanner.next().charAt(0);
		}

		System.exit(0);
	}

	private Action playPattern(final String pattern) {
		return Action.of(pedalboard.getCommands().get(Play.NAME),
				"pattern", pattern);
	}

	private Action playNote(final int note, final int velocity,
							final int channel) {
		return Action.of(pedalboard.getCommands().get(Note.NAME),
				"note", note,
				"velocity", velocity,
				"channel", channel);
	}

	private Action waitFor(final long millis) {
		return Action.of(pedalboard.getCommands().get(Wait.NAME),
				"millis", millis);
	}

	private Receiver createCommandListener() {
		return new Receiver() {
			@Override
			public void send(MidiMessage message, long timeStamp) {
				if (message instanceof ShortMessage) {
					final ShortMessage sm = (ShortMessage) message;
					if (sm.getCommand() == NOTE_ON || sm.getCommand() == NOTE_OFF) {
						final int pedalNumber = sm.getData1() - 59;
						final boolean pedalDown = sm.getCommand() == NOTE_ON && sm.getData2() > 0;
						pedalboard.pedal(pedalNumber, pedalDown);
					}
				}
			}

			@Override
			public void close() {
			}
		};
	}

	private Transmitter createInputPort() {
		final String portName = System.getProperty("mp.inputPort");
		final List<MidiDevice.Info> candidates = Arrays.stream(MidiSystem.getMidiDeviceInfo())
				.filter(d -> d.getName().equals(portName)).collect(Collectors.toList());

		MidiDevice midiDevice = null;
		boolean open = false;
		for(MidiDevice.Info candidate : candidates) {
			try {
				open = false;
				midiDevice = MidiSystem.getMidiDevice(candidate);
				midiDevice.open();
				open = true;
				final Transmitter transmitter = midiDevice.getTransmitter();
				transmitter.setReceiver(createCommandListener());
				return transmitter;
			} catch (Exception e) {
				if (midiDevice != null && open)
					midiDevice.close();
			}
		}

		throw new RuntimeException("Invalid MIDI input port: " + portName);
	}

	private Receiver createOutputPort() {
		final String portName = System.getProperty("mp.outputPort");
		final List<MidiDevice.Info> candidates = Arrays.stream(MidiSystem.getMidiDeviceInfo())
				.filter(d -> d.getName().equals(portName)).collect(Collectors.toList());

		MidiDevice midiDevice = null;
		boolean open = false;
		for(MidiDevice.Info candidate : candidates) {
			try {
				open = false;
				midiDevice = MidiSystem.getMidiDevice(candidate);
				midiDevice.open();
				open = true;
				return midiDevice.getReceiver();
			} catch (Exception e) {
				if (midiDevice != null && open)
					midiDevice.close();
			}
		}

		throw new RuntimeException("Invalid MIDI output port: " + portName);
	}

//	private static final Action NOOP = Action.of(COMMANDS.get(NoOp.NAME));
//	private static final Action STOP = Action.of(COMMANDS.get(Stop.NAME));
//	private static final Action FINISH = Action.of(COMMANDS.get(Finish.NAME));
	private static final String BASE_DIR = System.getProperty("mp.midiFilesBaseDir");
}
