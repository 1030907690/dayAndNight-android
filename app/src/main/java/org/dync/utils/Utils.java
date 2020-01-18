package org.dync.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;


public class Utils {

    public static Integer multiply(int v1, int v2) {
        BigDecimal b1 = new BigDecimal(Integer.toString(v1));
        BigDecimal b2 = new BigDecimal(Integer.toString(v2));
        return b1.multiply(b2).intValue();
    }


    public static int round(int v1, int v2) {
        if (v2 < 0) {
            throw new IllegalArgumentException("此参数错误");
        }
        BigDecimal one = new BigDecimal(Integer.toString(v1));
        BigDecimal two = new BigDecimal(Integer.toString(v2));
        return one.divide(two, 2, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
                is.close();
                os.close();
            }
        } catch (Exception ex) {
        }
    }
}
