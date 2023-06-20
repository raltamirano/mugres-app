package mugres.app.control.tracker.storage;

import java.util.HashMap;
import java.util.Map;

/**
 * MUGRES Song's metadata for the Song Editor.
 */
public class EditorMetadata {
    private final Map<String, Integer> patternOrder = new HashMap<>();
    private final Map<String, Integer> partyOrder = new HashMap<>();

    public Map<String, Integer> getPatternOrder() {
        return patternOrder;
    }

    public Map<String, Integer> getPartyOrder() {
        return partyOrder;
    }
}
