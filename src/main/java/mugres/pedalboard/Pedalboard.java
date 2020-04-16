package mugres.pedalboard;

import mugres.core.MUGRES;
import mugres.core.common.Context;
import mugres.core.common.io.Input;
import mugres.core.common.io.Output;
import mugres.core.function.builtin.drums.BlastBeat;
import mugres.core.function.builtin.drums.HalfTime;
import mugres.core.live.processors.Processor;
import mugres.core.live.processors.Status;
import mugres.core.live.processors.drummer.Drummer;
import mugres.core.live.processors.drummer.commands.*;
import mugres.core.live.processors.transformer.Transformer;
import mugres.core.live.processors.transformer.config.Configuration;
import mugres.core.live.processors.transformer.filters.Monitor;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Synthesizer;
import java.io.File;

import static java.util.Arrays.asList;
import static mugres.core.common.DrumKit.CR1;
import static mugres.core.common.DrumKit.CR2;
import static mugres.core.live.processors.drummer.Drummer.SwitchMode.IMMEDIATELY_FILL;
import static mugres.core.live.processors.drummer.Drummer.SwitchMode.NORMAL;

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
public class Pedalboard {
	private final Context context;
	private final Input input;
	private final Output output;
	private final Processor processor;

	public Pedalboard() {
 		context = Context.createBasicContext();
		input = createInput();
		output = createOutput();

//		processor = setupDrummer(context, input, output);
		processor = setupDrummerBuiltinFunctions(context, input, output);
//		processor = setupTransformer(context, input, output);

		processor.addStatusListener(this::statusListener);
	}

	public Processor getProcessor() {
		return processor;
	}

	private Input createInput() {
		return Input.midiInput(MUGRES.getMidiInputPort());
	}

	private Output createOutput() {
		try {
			return Output.midiSink(MUGRES.getMidiOutputPort());
		} catch (final Throwable t) {
			System.out.println("Could not connect to MUGRES MIDI output port. Trying default synthesizer..");
			return Output.midiSink(createSynthesizer());
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

	private void statusListener(final Status status) {
		System.out.println(status.getText());
	}

	private Drummer setupDrummer(final Context context,
								 final Input input,
								 final Output output) {
		final mugres.core.live.processors.drummer.config.Configuration config =
				new mugres.core.live.processors.drummer.config.Configuration("Live");

		config.createGroove("Intro")
				.appendMain(new File(BASE_DIR + "groove2-fill1.mid"));

		config.createGroove("Pattern 1")
				.appendMain(new File(BASE_DIR + "groove1.mid"))
					.appendFill(new File(BASE_DIR + "groove1-fill1.mid"))
					.appendFill(new File(BASE_DIR + "groove1-fill2.mid"));

		config.createGroove("Pattern 2")
				.appendMain(new File(BASE_DIR + "groove2.mid"))
					.appendFill(new File(BASE_DIR + "groove2-fill1.mid"))
					.appendFill(new File(BASE_DIR + "groove2-fill2.mid"));

		config.setAction(60,
				Play.INSTANCE.action(
						"pattern", "Intro",
						"switchMode", NORMAL)
						.then(Wait.INSTANCE.action("millis", Wait.HALF_SECOND))
						.then(Play.INSTANCE.action("pattern", "Pattern 1",
								"switchMode", NORMAL))
		);
		config.setAction(61, Play.INSTANCE.action(
				"pattern", "Pattern 1",
				"switchMode", NORMAL));
		config.setAction(62, Play.INSTANCE.action(
				"pattern", "Pattern 2",
				"switchMode", NORMAL));
		config.setAction(63, Finish.INSTANCE.action());
		config.setAction(64, Stop.INSTANCE.action());

		return new Drummer(context, input, output, config);
	}

	private Drummer setupDrummerBuiltinFunctions(final Context context,
												 final Input input,
												 final Output output) {
		final mugres.core.live.processors.drummer.config.Configuration config =
				new mugres.core.live.processors.drummer.config.Configuration("Drumming functions");

		config.createGroove("Pattern 1", context, 4, new BlastBeat());
		config.createGroove("Pattern 2", context, 4, new HalfTime());

		config.setAction(60, Play.INSTANCE.action(
				"pattern", "Pattern 1",
				"switchMode", IMMEDIATELY_FILL));
		config.setAction(61, Play.INSTANCE.action(
				"pattern", "Pattern 2",
				"switchMode", IMMEDIATELY_FILL));
		config.setAction(62, Hit.INSTANCE.action(
				"options", asList(CR1, CR2),
				"velocity", 110));
		config.setAction(63, Finish.INSTANCE.action());
		config.setAction(64, Stop.INSTANCE.action());

		return new Drummer(context, input, output, config);
	}


	private Transformer setupTransformer(final Context context,
										 final Input input,
										 final Output output) {
		final Configuration config =
				new Configuration();

		config.appendFilter(Monitor.withLabel("Input"));
		config.appendFilter(Monitor.withLabel("Output"));

		return new Transformer(context, input, output, config);
	}

	private static final String BASE_DIR = System.getProperty("mugres.pedalboard.midiFilesBaseDir");
}
