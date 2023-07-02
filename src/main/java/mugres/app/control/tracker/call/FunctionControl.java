package mugres.app.control.tracker.call;

import mugres.function.Call;
import mugres.tracker.Event;

import java.util.List;

public interface FunctionControl {
    void setCall(final Call<List<Event>> call);
}
