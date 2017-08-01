package cloud.rocksdb.server.master;

import cloud.rocksdb.server.client.command.*;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by fafu on 2017/5/30.
 */
@Data
public class MasterProxyHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(MasterProxyHandler.class);


    private ShardDiscover shardDiscover;

    public MasterProxyHandler(ShardDiscover shardDiscover) {
        this.shardDiscover = shardDiscover;
    }

    public class CallbackImpl implements Callback{
        public CountDownLatch latch;
        public CallbackImpl(CountDownLatch latch){
            this.latch = latch;
        }

        @Override
        public void onSuccess() {
            latch.countDown();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Command command = (Command)msg;
        switch (command.getType()){
            case GET:
                GetCommand getCommand = (GetCommand)command;
                ServiceDiscover discover = shardDiscover.get(getCommand.getKey());
                byte[] getResponse = discover.getOne(getCommand.getKey()).get(getCommand.getKey());
                ctx.writeAndFlush(new GetResponse(getResponse));
                break;
            case PUT:
                //todo 1. Change to parallel call. 2. Add return strategy,for example ,ONE/ALL/QUORUM 3. write wal ?
                PutCommand putCommand = ((PutCommand)command);
                CountDownLatch latch = new CountDownLatch(shardDiscover.get(putCommand.getKey()).getDataServers().size());
                CallbackImpl callback = new CallbackImpl(latch);
                shardDiscover.get(putCommand.getKey()).getDataServers().forEach(server->{
                    server.asyncPut(putCommand.getKey(),putCommand.getValue(),callback);
                });
                try {
                    latch.await(20, TimeUnit.MILLISECONDS);
                }catch (Exception e){
                    log.error("",e);
                     //todo 1. add exception handler
                }
                ctx.writeAndFlush(new PutResponse());
                break;
            case MULTIGET:
                MultiGetCommand multiGetCommand = (MultiGetCommand)command;
                Map<ServiceDiscover,List<byte[]>> serviceDiscoverListMap = shardDiscover.multiGet(Arrays.asList(multiGetCommand.getKeys()));
                Map<? extends byte[], ? extends byte[]> ret = Maps.newHashMap();
                CountDownLatch latch2 = new CountDownLatch(serviceDiscoverListMap.size());
                CallbackImpl callback2 = new CallbackImpl(latch2);
                serviceDiscoverListMap.forEach((dis,bytes)->{
                    dis.getOne().asyncMultiGet(bytes,callback2);
                });
                try {
                    latch2.await(20, TimeUnit.MILLISECONDS);
                }catch (Exception e){
                    log.error("",e);
                }
                ctx.writeAndFlush(new MultiGetResponse((Map<byte[], byte[]>) ret));
                break;
            case DELETE:
                DeleteCommand deleteCommand = (DeleteCommand)command;
                CountDownLatch deleteLatch = new CountDownLatch(shardDiscover.get(deleteCommand.getKey()).getDataServers().size());
                CallbackImpl deleteCall = new CallbackImpl(deleteLatch);
                shardDiscover.get(deleteCommand.getKey()).getDataServers().forEach(server->{
                    server.asyncDelete(deleteCommand.getKey(),deleteCall);
                });
                try {
                    deleteLatch.await(20, TimeUnit.MILLISECONDS);
                }catch (Exception e){
                    log.error("",e);
                }
                ctx.writeAndFlush(new DeleteResponse());
                break;
            case GET_LATEST_SEQ:
                //no implemented yet.
                break;
            case EXIST:
                ExistCommand existCommand = (ExistCommand)command;
                discover = shardDiscover.get(existCommand.getKey());
                boolean exist = discover.getOne(existCommand.getKey()).exist(existCommand.getKey());
                ctx.writeAndFlush(new ExistResponse(exist));
                break;
            default:
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("",cause);
        ctx.close();
    }
}
