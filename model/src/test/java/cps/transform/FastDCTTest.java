package cps.transform;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class FastDCTTest {

    @Test
    public void toY() {
        List<Double> input = Arrays.asList(
                0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0
        );

        List<Double> expectedResult = Arrays.asList(
            0.0,2.0,4.0,6.0,7.0,5.0,3.0,1.0
        );

        var result = FastDCT.toY(input);

        assertEquals(result, expectedResult);
    }

    @Test
    public void toX() {
    }
}