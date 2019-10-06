package com.raltamirano.midipedalboard;

import com.raltamirano.midipedalboard.commands.NoOp;
import com.raltamirano.midipedalboard.commands.Play;
import com.raltamirano.midipedalboard.commands.Stop;
import com.raltamirano.midipedalboard.model.Action;
import com.raltamirano.midipedalboard.model.Song;
import com.raltamirano.midipedalboard.orchestration.Command;
import com.raltamirano.midipedalboard.orchestration.Orchestrator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sound.midi.*;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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
	private Song song;
	private Orchestrator orchestrator;

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Override
	public void run(String... args) {
		Transmitter inputPort = getInputPort();
		inputPort.setReceiver(createCommandListener());

		Receiver outputPort = getOutputPort();
		song = new Song("Demo 1", 124);

		song.createPattern("Pattern 1")
				.appendGroove(new File(BASE_DIR + "groove1.mid"))
					.appendFill(new File(BASE_DIR + "groove1-fill1.mid"))
					.appendFill(new File(BASE_DIR + "groove1-fill2.mid"));

		song.createPattern("Pattern 2")
				.appendGroove(new File(BASE_DIR + "groove2.mid"))
					.appendFill(new File(BASE_DIR + "groove2-fill1.mid"))
					.appendFill(new File(BASE_DIR + "groove2-fill2.mid"));

		song.setAction(1, playPattern("Pattern 1"));
		song.setAction(2, playPattern("Pattern 2"));
		song.setAction(3, NOOP);
		song.setAction(4, NOOP);
		song.setAction(5, STOP);

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
					onPedal(Integer.parseInt(String.valueOf(c)));
				default:
					break;
			}
			c = scanner.next().charAt(0);
		}

		System.exit(0);
	}

	private void onPedal(final int pedal) {
		final Action action = song.getAction(pedal);
		if (action != null)
			action.execute(orchestrator);
	}

	private Action playPattern(final String pattern) {
		return Action.of(COMMANDS.get(Play.NAME),
				"pattern", pattern);
	}

	private Receiver createCommandListener() {
		return new Receiver() {
			@Override
			public void send(MidiMessage message, long timeStamp) {
				if (message instanceof ShortMessage) {
					final ShortMessage sm = (ShortMessage) message;
					if (sm.getCommand() == NOTE_ON) {
						switch (sm.getData1()) {
							case 60:
								onPedal(1);
								break;
							case 61:
								onPedal(2);
								break;
							case 62:
								onPedal(3);
								break;
							case 63:
								onPedal(4);
								break;
							case 64:
								onPedal(5);
								break;
						}
					}
				}
			}

			@Override
			public void close() {
			}
		};
	}

	private Transmitter getInputPort() {
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
				return midiDevice.getTransmitter();
			} catch (Exception e) {
				if (midiDevice != null && open)
					midiDevice.close();
			}
		}

		throw new RuntimeException("Invalid MIDI input port: " + portName);
	}

	private Receiver getOutputPort() {
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

	private static final Map<String, Command> COMMANDS = new HashMap<>();

	static {
		addCommand(new NoOp());
		addCommand(new Stop());
		addCommand(new Play());
	}

	private static final Action NOOP = Action.of(COMMANDS.get(NoOp.NAME));
	private static final Action STOP = Action.of(COMMANDS.get(Stop.NAME));

	private static void addCommand(final Command command) {
		final String commandName = command.getName();
		if (COMMANDS.containsKey(commandName))
			throw new RuntimeException("Already registered command: " + commandName);
		COMMANDS.put(commandName, command);
	}

	private static final String BASE_DIR = System.getProperty("mp.midiFilesBaseDir");
}
