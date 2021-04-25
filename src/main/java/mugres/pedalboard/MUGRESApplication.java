package mugres.pedalboard;

import mugres.core.MUGRES;
import mugres.core.common.io.Input;
import mugres.core.common.io.Output;
import mugres.pedalboard.config.MUGRESConfig;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Synthesizer;

/**
 * <p>MUGRES MIDI Pedalboard application.</p>
 * <br />
 * Use the following VM option to set the base directory for MIDI files:
 * <br /><br />
 * -Dmugres.pedalboard.midiFilesBaseDir=r:\all\projects\dev\raltamirano.github\mugres-pedalboard\src\main\resources\mugres\pedalboard\samples\patterns\
 * <br /><br />
 * Use the following VM options to define input/output MIDI ports:
 * <br /><br />
 * <p>-Dmugres.inputPort="loopMIDI Port"</p>
 * <p>-Dmugres.outputPort="loopMIDI Port 1"</p>
 */
public class MUGRESApplication {
	private MUGRESConfig mugresConfig;
	private final Input input;
	private final Output output;

	public MUGRESApplication() {
		mugresConfig = MUGRESConfig.read();

		MUGRES.useMidiInputPort(mugresConfig.getMidiInputPort());
		MUGRES.useMidiOutputPort(mugresConfig.getMidiOutputPort());

		input = createInput();
		output = createOutput();
	}

	public MUGRESConfig getConfig() {
		return mugresConfig;
	}

	public Input getInput() {
		return input;
	}

	public Output getOutput() {
		return output;
	}

	private Input createInput() {
		return Input.midiInput(MUGRES.getMidiInputPort());
	}

	private Output createOutput() {
		try {
			return Output.midiOutput(MUGRES.getMidiOutputPort());
		} catch (final Throwable t) {
			System.out.println("Could not connect to MUGRES MIDI output port. Trying default synthesizer..");
			return Output.midiOutput(createSynthesizer());
		}
	}

	private Receiver createSynthesizer() {
		try {
			final Synthesizer synthesizer = MidiSystem.getSynthesizer();
			synthesizer.open();
			return synthesizer.getReceiver();
		} catch (final Throwable t) {
			throw new RuntimeException("Could not create synthesizer instance", t);
		}
	}
}
