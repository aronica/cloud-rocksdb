package redis;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.redis.RedisMessage;
import io.netty.util.AttributeKey;

import java.util.List;

/**
 * Created by fafu on 2017/8/4.
 */
public class RedisBulkMessageScatter extends MessageToMessageEncoder<RedisMessage>
{
    private static AttributeKey<FullBulkRedisMessage> REDIS_MSG_ATTR_KEY = AttributeKey.valueOf("FULL_REDIS_MESSAGE");

    @Override
    protected void encode(ChannelHandlerContext ctx, RedisMessage msg, List<Object> out) throws Exception {
        if(msg instanceof FullBulkRedisMessage){
            FullBulkRedisMessage fullBulkRedisMessage = (FullBulkRedisMessage)msg;
            out.add(fullBulkRedisMessage.getHead());
            out.addAll(fullBulkRedisMessage.getBody());
            return;
        }
        out.add(msg);
    }
}
