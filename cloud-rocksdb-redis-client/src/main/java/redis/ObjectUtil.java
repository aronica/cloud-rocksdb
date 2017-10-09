package redis;

import io.netty.buffer.ByteBuf;

import static io.netty.util.CharsetUtil.UTF_8;

/**
 * Created by fafu on 2017/5/31.
 */
public class ObjectUtil {

    public static byte[] convert(String val){
        return val.getBytes(UTF_8);
    }

    public static String convert(byte[] val){
        return new String(val,UTF_8);
    }

    public static byte[] getContent(ByteBuf buf){
        int readable = buf.readableBytes();
        byte[] ret = new byte[readable];
        buf.readBytes(ret);
        return ret;
    }
}
