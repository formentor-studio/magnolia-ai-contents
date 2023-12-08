import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class BorrarTest {

    @Test
    void optional() {
        Optional opt = Optional.of("abc");
        Optional optt = opt.flatMap(value -> Optional.of(value.toString()));

        Assertions.assertTrue(optt.isPresent());
    }
}
