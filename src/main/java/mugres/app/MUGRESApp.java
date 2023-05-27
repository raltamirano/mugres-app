package mugres.app;

import mugres.MUGRES;
import mugres.app.config.MUGRESConfig;

/**
 * <p>MUGRES main application.</p>
 * <br />
 * Use the following VM options to define input/output MIDI ports:
 * <br /><br />
 * <p>-Dmugres.inputPort="mugres-in"</p>
 * <p>-Dmugres.outputPort="mugres-out"</p>
 */
public class MUGRESApp {
	private MUGRESConfig mugresConfig;

	public MUGRESApp() {
		mugresConfig = MUGRESConfig.read();

		MUGRES.useMidiInputPort(mugresConfig.getMidiInputPort());
		MUGRES.useMidiOutputPort(mugresConfig.getMidiOutputPort());
	}

	public MUGRESConfig getConfig() {
		return mugresConfig;
	}
}
