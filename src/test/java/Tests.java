import com.unrealdinnerbone.apollo.core.api.Scenario;
import com.unrealdinnerbone.apollo.core.api.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class Tests
{
    @Test
    public void testScenEquals() {
        Scenario scenario = new Scenario("Test", 1, true, true, Type.SCENARIO, new ArrayList<>(), new ArrayList<>(), true, true);
        Scenario scenario1 = new Scenario("Test2", 1, true, true, Type.SCENARIO, new ArrayList<>(), new ArrayList<>(), true, true);
        Assertions.assertTrue(() -> scenario.equals(scenario1));
    }
}
