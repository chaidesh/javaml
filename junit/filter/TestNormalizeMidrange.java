/**
 * %SVN.HEADER%
 */
package junit.filter;

import junit.framework.Assert;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.filter.DatasetFilter;
import net.sf.javaml.filter.normalize.NormalizeMidrange;

import org.junit.Test;

public class TestNormalizeMidrange {

    @Test
    public void testNaNWhenAllSameValues() {
        Dataset data = new DefaultDataset();
        add(data, 0, 0, 1);
        add(data, 1, 1, 1);
        add(data, 2, 2, 1);
        add(data, 0, 1, 1);
        System.out.println("Before filter");
        System.out.println(data);
        DatasetFilter f = new NormalizeMidrange(0.5, 1);
        f.filter(data);
        System.out.println("--");
        System.out.println("After filter");
        System.out.println(data);
        Assert.assertFalse(new Float(data.instance(0).value(2)).equals(Float.NaN));
        Assert.assertFalse(new Float(data.instance(1).value(2)).equals(Float.NaN));
        Assert.assertFalse(new Float(data.instance(2).value(2)).equals(Float.NaN));
        Assert.assertFalse(new Float(data.instance(3).value(2)).equals(Float.NaN));
        Assert.assertFalse(new Float(data.instance(0).value(0)).equals(Float.NaN));
        Assert.assertFalse(new Float(data.instance(1).value(1)).equals(Float.NaN));
        Assert.assertFalse(new Float(data.instance(2).value(0)).equals(Float.NaN));
        Assert.assertFalse(new Float(data.instance(3).value(1)).equals(Float.NaN));

    }

    private void add(Dataset data, float x, float y, float z) {
        double[] values = { x, y, z };
        DenseInstance in = new DenseInstance(values);
        data.add(in);
    }

}
