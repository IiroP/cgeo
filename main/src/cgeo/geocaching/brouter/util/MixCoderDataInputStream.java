/**
 * DataInputStream for decoding fast-compact encoded number sequences
 *
 * @author ab
 */
package cgeo.geocaching.brouter.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;


public final class MixCoderDataInputStream extends DataInputStream {
    private static final int[] vl_values = BitCoderContext.vl_values;
    private static final int[] vl_length = BitCoderContext.vl_length;
    private int lastValue;
    private int repCount;
    private int diffshift;
    private int bits; // bits left in buffer
    private int b; // buffer word

    public MixCoderDataInputStream(InputStream is) {
        super(is);
    }

    public int readMixed() throws IOException {
        if (repCount == 0) {
            boolean negative = decodeBit();
            int d = decodeVarBits() + diffshift;
            repCount = decodeVarBits() + 1;
            lastValue += negative ? -d : d;
            diffshift = 1;
        }
        repCount--;
        return lastValue;
    }

    public final boolean decodeBit() throws IOException {
        fillBuffer();
        boolean value = ((b & 1) != 0);
        b >>>= 1;
        bits--;
        return value;
    }

    public final int decodeVarBits2() throws IOException {
        int range = 0;
        while (!decodeBit()) {
            range = 2 * range + 1;
        }
        return range + decodeBounded(range);
    }

    /**
     * decode an integer in the range 0..max (inclusive).
     *
     * @see #encodeBounded
     */
    public final int decodeBounded(int max) throws IOException {
        int value = 0;
        int im = 1; // integer mask
        while ((value | im) <= max) {
            if (decodeBit()) {
                value |= im;
            }
            im <<= 1;
        }
        return value;
    }


    /**
     * @see #encodeVarBits
     */

    public final int decodeVarBits() throws IOException {
        fillBuffer();
        int b12 = b & 0xfff;
        int len = vl_length[b12];
        if (len <= 12) {
            b >>>= len;
            bits -= len;
            return vl_values[b12]; // full value lookup
        }
        if (len <= 23) // // only length lookup
        {
            int len2 = len >> 1;
            b >>>= (len2 + 1);
            int mask = 0xffffffff >>> (32 - len2);
            mask += b & mask;
            b >>>= len2;
            bits -= len;
            return mask;
        }
        if ((b & 0xffffff) != 0) {
            // here we just know len in [25..47]
            // ( fillBuffer guarantees only 24 bits! )
            b >>>= 12;
            int len3 = 1 + (vl_length[b & 0xfff] >> 1);
            b >>>= len3;
            int len2 = 11 + len3;
            bits -= len2 + 1;
            fillBuffer();
            int mask = 0xffffffff >>> (32 - len2);
            mask += b & mask;
            b >>>= len2;
            bits -= len2;
            return mask;
        }
        return decodeVarBits2(); // no chance, use the slow one
    }

    private void fillBuffer() throws IOException {
        while (bits < 24) {
            int nextByte = read();

            if (nextByte != -1) {
                b |= (nextByte & 0xff) << bits;
            }
            bits += 8;
        }
    }

}
