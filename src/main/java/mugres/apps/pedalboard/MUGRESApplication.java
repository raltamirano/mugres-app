package mugres.apps.pedalboard;

import mugres.apps.pedalboard.config.MUGRESConfig;
import mugres.core.MUGRES;
import mugres.core.common.io.Input;
import mugres.core.common.io.Output;

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
 * <p>-Dmugres.inputPort="mugres-in"</p>
 * <p>-Dmugres.outputPort="mugres-out"</p>
 */
public class MUGRESApplication {
	private MUGRESConfig mugresConfig;

	public MUGRESApplication() {
		mugresConfig = MUGRESConfig.read();

		MUGRES.useMidiInputPort(mugresConfig.getMidiInputPort());
		MUGRES.useMidiOutputPort(mugresConfig.getMidiOutputPort());
	}

	public MUGRESConfig getConfig() {
		return mugresConfig;
	}
}
