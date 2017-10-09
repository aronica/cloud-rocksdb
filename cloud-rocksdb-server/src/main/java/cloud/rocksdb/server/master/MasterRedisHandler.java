package cloud.rocksdb.server.master;

import cloud.rocksdb.server.ObjectUtil;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.redis.ArrayHeaderRedisMessage;
import io.netty.handler.codec.redis.FixedRedisMessagePool;
import io.netty.handler.codec.redis.FullBulkStringRedisMessage;
import io.netty.handler.codec.redis.RedisMessage;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MasterRedisHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(MasterProxyHandler.class);
    private ShardDiscover shardDiscover;

    private static AttributeKey<FullRedisMessage> REDIS_MSG_KEY = AttributeKey.valueOf("FULL_REDIS_MESSAGE");

    public MasterRedisHandler(ShardDiscover shardDiscover) {
        this.shardDiscover = shardDiscover;
    }

    public class CallbackImpl implements Callback{
        public CountDownLatch latch;
        private AtomicInteger fail = new AtomicInteger(0);
        public CallbackImpl(CountDownLatch latch){
            this.latch = latch;
        }

        @Override
        public void onSuccess() {
            latch.countDown();
        }

        public void onFail(){
            onSuccess();
            fail.incrementAndGet();
        }

        public int getFail(){
            return fail.get();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RedisMessage command = (RedisMessage)msg;
        if(command instanceof ArrayHeaderRedisMessage){
            Attribute<FullRedisMessage> keys =  ctx.channel().attr(REDIS_MSG_KEY);

            if(keys.get()!=null&&keys.get().getHead() != null){
                throw new InvalidRedisMessageException("Invalid header redis message found.");
            }
            FullRedisMessage fullRedisMessage = new FullRedisMessage();
            ArrayHeaderRedisMessage headerRedisMessage = (ArrayHeaderRedisMessage)command;
            fullRedisMessage.setBodyLength(headerRedisMessage.length());
            fullRedisMessage.setHead(headerRedisMessage);
            keys.set(fullRedisMessage);
        }else if(command instanceof FullBulkStringRedisMessage){
            Attribute<FullRedisMessage> keys =  ctx.channel().attr(REDIS_MSG_KEY);
            if(keys.get() == null || keys.get().getHead() == null){
                throw new InvalidRedisMessageException("No header redis message found.");
            }
            FullRedisMessage fullRedisMessage = keys.get();
            long len = fullRedisMessage.getBodyLength();
            if(len == fullRedisMessage.getBody().size()){
                throw new InvalidRedisMessageException("Unexpected request line found.");
            }
            fullRedisMessage.getBody().add((FullBulkStringRedisMessage)command);
            //body line collect full.
            if(fullRedisMessage.getBody().size() == fullRedisMessage.getBodyLength()){
                keys.set(new FullRedisMessage());//reset to empty.
                doProcess(fullRedisMessage,ctx.channel(),ctx);
            }
        }
        System.out.println(command.getClass());
    }

    private void doProcess(FullRedisMessage fullRedisMessage,Channel channel,ChannelHandlerContext ctx) throws Exception {
        List<FullBulkStringRedisMessage> body = fullRedisMessage.body;
        FullBulkStringRedisMessage cmd = body.get(0);
        ByteBuf buf = cmd.content();
        String command = buf.toString(Charset.forName("UTF-8"));
        if("get".equalsIgnoreCase(command)){
            doProcessGet(body,channel,ctx);
        }else if("put".equalsIgnoreCase(command)){
            doProcessPut(body,channel,ctx);
        }
    }

    private void doProcessPut(List<FullBulkStringRedisMessage> body, Channel channel, ChannelHandlerContext ctx) throws InvalidRedisMessageException {
        if(body.size()<3){
            throw new InvalidRedisMessageException("Put request requires a key and a value.");
        }
        byte[] key = ObjectUtil.getContent(body.get(1).content());
        byte[] value = ObjectUtil.getContent(body.get(2).content());
        CountDownLatch latch = new CountDownLatch(shardDiscover.get(key).getDataServers().size());
        CallbackImpl callback = new CallbackImpl(latch);
        shardDiscover.get(key).getDataServers().forEach(server->{
            server.asyncPut(key,value,callback);
        });
        try {
            latch.await(20, TimeUnit.MILLISECONDS);
            if(callback.getFail()==shardDiscover.get(key).getDataServers().size()){
                channel.writeAndFlush(FixedRedisMessagePool.INSTANCE.getSimpleString("ERR"));//TODO
            }else{
                channel.writeAndFlush(FixedRedisMessagePool.INSTANCE.getSimpleString("OK"));
            }

        }catch (Exception e){
            log.error("",e);
            //todo 1. add exception handler
            channel.writeAndFlush(FixedRedisMessagePool.INSTANCE.getError("ERR"));
        }
    }

    private void doProcessGet(List<FullBulkStringRedisMessage> body, Channel channel,ChannelHandlerContext ctx) throws InvalidRedisMessageException {
        if(body.size()<2){
            throw new InvalidRedisMessageException("Get request requires a key");
        }
        ByteBuf buf = body.get(1).content();
        byte[] key = ObjectUtil.getContent(buf);
        ServiceDiscover discover = shardDiscover.get(key);
        byte[] result = new byte[0];
        try {
            result = discover.getOne(key).get(key);
        } catch (Exception e) {
            log.error("",e);
        }
        if(result == null||result.length == 0){
            channel.writeAndFlush(FullBulkStringRedisMessage.NULL_INSTANCE);
        }else{
            ByteBuf res = ctx.alloc().buffer(result.length);
            res.writeBytes(result);
            FullBulkStringRedisMessage bulkStringRedisMessage = new FullBulkStringRedisMessage(res);
            channel.writeAndFlush(bulkStringRedisMessage);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("",cause);
        ctx.close();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class FullRedisMessage{
        private ArrayHeaderRedisMessage head;
        private long bodyLength;
        private List<FullBulkStringRedisMessage> body = Lists.newArrayList();

    }
}
