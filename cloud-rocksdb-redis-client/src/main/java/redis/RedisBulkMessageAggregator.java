package redis;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.redis.ArrayHeaderRedisMessage;
import io.netty.handler.codec.redis.FixedRedisMessagePool;
import io.netty.handler.codec.redis.FullBulkStringRedisMessage;
import io.netty.handler.codec.redis.RedisMessage;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.List;

/**
 * Created by fafu on 2017/8/4.
 */
public class RedisBulkMessageAggregator extends MessageToMessageDecoder<RedisMessage>
{
    private static AttributeKey<FullBulkRedisMessage> REDIS_MSG_ATTR_KEY = AttributeKey.valueOf("FULL_REDIS_MESSAGE");

    @Override
    protected void decode(ChannelHandlerContext ctx, RedisMessage msg, List<Object> out) throws Exception {
        if(msg instanceof ArrayHeaderRedisMessage){
            Attribute<FullBulkRedisMessage> keys =  ctx.channel().attr(REDIS_MSG_ATTR_KEY);
            if(keys.get()!=null&&keys.get().getHead() != null){
                ctx.channel().writeAndFlush(FixedRedisMessagePool.INSTANCE.getError("ERR syntax error"));
                return;
            }
            FullBulkRedisMessage fullRedisMessage = new FullBulkRedisMessage();
            ArrayHeaderRedisMessage headerRedisMessage = (ArrayHeaderRedisMessage)msg;
            fullRedisMessage.setBodyLength(headerRedisMessage.length());
            fullRedisMessage.setHead(headerRedisMessage);
            keys.set(fullRedisMessage);
        }else if(msg instanceof FullBulkStringRedisMessage){
            Attribute<FullBulkRedisMessage> keys =  ctx.channel().attr(REDIS_MSG_ATTR_KEY);
            if(keys.get() == null || keys.get().getHead() == null){
                ctx.channel().writeAndFlush(FixedRedisMessagePool.INSTANCE.getError("ERR syntax error"));
                return;
            }
            FullBulkRedisMessage fullRedisMessage = keys.get();
            long len = fullRedisMessage.getBodyLength();
            if(len == fullRedisMessage.getBody().size()){
                ctx.channel().writeAndFlush(FixedRedisMessagePool.INSTANCE.getError("ERR syntax error"));
                return;
            }
            fullRedisMessage.getBody().add((FullBulkStringRedisMessage)msg);
            if(fullRedisMessage.getBody().size() == fullRedisMessage.getBodyLength()){
                keys.set(null);//reset to empty.
                out.add(fullRedisMessage);
            }
        }else{
            out.add(msg);
        }
    }
}
