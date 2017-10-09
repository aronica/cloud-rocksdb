package redis;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.redis.FullBulkStringRedisMessage;
import io.netty.handler.codec.redis.RedisMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class MasterRedisHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(MasterRedisHandler.class);

    private EtcdClientProxy proxy;

    public MasterRedisHandler(EtcdClientProxy proxy) {
        this.proxy = proxy;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RedisMessage command = (RedisMessage)msg;
        if(command instanceof FullBulkRedisMessage){
            executeFullRedisMessage((FullBulkRedisMessage)command,ctx);
        }

    }

    private void executeFullRedisMessage(FullBulkRedisMessage fullRedisMessage, ChannelHandlerContext ctx) throws Exception {
        List<FullBulkStringRedisMessage> body = fullRedisMessage.getBody();
        FullBulkStringRedisMessage cmd = body.get(0);
        ByteBuf buf = cmd.content();
        String command = buf.toString(Charset.forName("UTF-8"));
        RedisMessageEnum messageEnum = RedisMessageEnum.valueOf(command);
        if(){
            doGet(body,ctx);
        }else if("set".equalsIgnoreCase(command)){
            doSet(body,ctx);
        }
    }

    private void doSet(List<FullBulkStringRedisMessage> body, ChannelHandlerContext ctx) throws InvalidRedisMessageException {

    }

    private void doGet(List<FullBulkStringRedisMessage> body, ChannelHandlerContext ctx) throws InvalidRedisMessageException {
        if(body.size()<2){
            throw new InvalidRedisMessageException("Get request requires a key");
        }
        ByteBuf buf = body.get(1).content();
        byte[] key = ObjectUtil.getContent(buf);
        try {
            byte[] value = proxy.get(key);
            if(value != null){

            }
        } catch (KvException e) {
            log.error("",e);
        } catch (TimeoutException e) {
            log.error("",e);
        }
        FullBulkStringRedisMessage bulkStringRedisMessage = new FullBulkStringRedisMessage(null);
            channel.writeAndFlush(bulkStringRedisMessage);
}
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("",cause);
        ctx.close();
    }
}
