import mugres.core.MUGRES;
import mugres.core.common.Context;
import mugres.core.common.Pitch;
import mugres.core.common.Played;
import mugres.core.common.Signal;
import mugres.core.common.io.Input;
import mugres.core.common.io.Output;
import mugres.core.live.processors.Processor;
import mugres.core.live.processors.drummer.Drummer;
import mugres.core.live.processors.drummer.commands.Finish;
import mugres.core.live.processors.drummer.commands.Play;
import mugres.core.live.processors.drummer.commands.Stop;
import mugres.core.live.processors.drummer.commands.Wait;
import mugres.core.live.processors.transformer.Transformer;
import mugres.core.live.processors.transformer.config.Configuration;
import mugres.core.live.processors.transformer.filters.Monitor;

import java.io.File;
import java.util.Scanner;

import static java.lang.System.currentTimeMillis;

/**
 * <p>MUGRES MIDI Pedalboard application.</p>
 * <br />
 * Use the following VM option to set the base directory for MIDI files:
 * <br /><br />
 * -Dmugres.pedalboard.midiFilesBaseDir=r:\all\projects\dev\raltamirano.github\midi-pedalboard\src\etc\midi\
 * <br /><br />
 * Use the following VM options to define input/output MIDI ports:
 * <br /><br />
 * <p>-Dmugres.inputPort="loopMIDI Port"</p>
 * <p>-Dmugres.outputPort="loopMIDI Port 1"</p>
 */
public class MidiPedalboard {
	public static void main(String[] args) {
		new MidiPedalboard().run();
	}

	public void run() {
		final Context context = Context.createBasicContext();
		final Input input = Input.midiInput(MUGRES.getMidiInputPort());
		final Output output = Output.midiSink(MUGRES.getMidiOutputPort());

		final Processor processor = setupDrummer(context, input, output);
//		final Processor processor = setupTransformer(context, input, output);

		final Scanner scanner = new Scanner(System.in);
		char c = scanner.next().charAt(0);
		while (c != 'q') {
			switch (c) {
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
					processor.process(makeTestSignal(c));
				default:
					break;
			}
			c = scanner.next().charAt(0);
		}

		System.exit(0);
	}

	private Signal makeTestSignal(final char c) {
		return Signal.on(currentTimeMillis(), TEST_MIDI_CHANNEL,
				Played.of(Pitch.of(59 + Integer.valueOf(String.valueOf(c))), 100));
	}

	private Drummer setupDrummer(final Context context,
								 final Input input,
								 final Output output) {
		final mugres.core.live.processors.drummer.config.Configuration config =
				new mugres.core.live.processors.drummer.config.Configuration("Live");

		config.createPattern("Intro")
				.appendGroove(new File(BASE_DIR + "groove2-fill1.mid"));

		config.createPattern("Pattern 1")
				.appendGroove(new File(BASE_DIR + "groove1.mid"))
					.appendFill(new File(BASE_DIR + "groove1-fill1.mid"))
					.appendFill(new File(BASE_DIR + "groove1-fill2.mid"));

		config.createPattern("Pattern 2")
				.appendGroove(new File(BASE_DIR + "groove2.mid"))
					.appendFill(new File(BASE_DIR + "groove2-fill1.mid"))
					.appendFill(new File(BASE_DIR + "groove2-fill2.mid"));

		config.setAction(60,
				Play.INSTANCE.action("pattern", "Intro")
						.then(Wait.INSTANCE.action("millis", Wait.HALF_SECOND))
						.then(Play.INSTANCE.action("pattern", "Pattern 1"))
		);
		config.setAction(61, Play.INSTANCE.action("pattern", "Pattern 1"));
		config.setAction(62, Play.INSTANCE.action("pattern", "Pattern 2"));
		config.setAction(63, Finish.INSTANCE.action());
		config.setAction(64, Stop.INSTANCE.action());

		return new Drummer(context, input, output, config, MUGRES.getMidiOutputPort());
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
	private static final int TEST_MIDI_CHANNEL = 1;
}
