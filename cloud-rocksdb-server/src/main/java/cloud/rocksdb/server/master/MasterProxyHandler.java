package cloud.rocksdb.server.master;

import cloud.rocksdb.server.client.command.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by fafu on 2017/5/30.
 */
@Data
@AllArgsConstructor
public class MasterProxyHandler extends ChannelInboundHandlerAdapter {

    private Map<String,Map<String,DataServerNode>> serverNodeMap;
    private AtomicInteger inc = new AtomicInteger(0);
    private ExecutorService executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors()*2,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1000000),(r)->{
        return new Thread(r,"ProxyWorkerThread");
    },new ThreadPoolExecutor.AbortPolicy());
    //todo
    private List<DataServerNode> servers;
    public MasterProxyHandler(Map<String, Map<String, DataServerNode>> serverNodeMap) {
        this.serverNodeMap = serverNodeMap;
        servers = new ArrayList(serverNodeMap.get("1").values());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Command command = (Command)msg;
        switch (command.getType()){
            case GET:
                byte[] res = servers.get(inc.getAndIncrement()%servers.size()).get(((GetCommand)command).getKey());
                ctx.writeAndFlush(new GetResponse(res));
                break;
            case PUT:
                //todo 1. Change to parallel call. 2. Add return strategy,for example ,ONE/ALL/QUORUM
                PutCommand putCommand = ((PutCommand)command);
                doExecute((server)->{
                    try{
                        server.put(putCommand.getKey(),putCommand.getValue());
                    }catch (Exception e){
                        e.printStackTrace();
                        //todo
                    }
                },putCommand);
                ctx.writeAndFlush(new PutResponse());
                break;
            case MULTIGET:
                MultiGetCommand multiGetCommand = (MultiGetCommand)command;
                Map<? extends byte[], ? extends byte[]> ret = servers.get(inc.getAndIncrement()%servers.size()).multiGet(Arrays.asList(multiGetCommand.getKeys()));
                ctx.writeAndFlush(new MultiGetResponse((Map<byte[], byte[]>) ret));
                break;
            case DELETE:
                DeleteCommand deleteCommand = (DeleteCommand)command;
                doExecute((server)->{
                    try {
                        server.delete(deleteCommand.getKey());
                    } catch (Exception e) {
                        e.printStackTrace();
                        //todo
                    }
                },deleteCommand);
                ctx.writeAndFlush(new DeleteResponse());
                break;
            case GET_LATEST_SEQ:
                //just break
                break;
            case EXIST:
                ExistCommand existCommand = (ExistCommand)command;
                ctx.writeAndFlush(servers.get(inc.getAndIncrement()%servers.size()).exist(existCommand.getKey()));
                break;
            default:
        }
    }

    private void doExecute(Consumer<DataServerNode> commandConsumer,Command command) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(servers.size());
        List<Future<?>> futures = new ArrayList<>();
        servers.forEach(server->{
            futures.add(executor.submit(()->{
                try {
                    commandConsumer.accept(server);
                    latch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        });
        latch.await(10, TimeUnit.MICROSECONDS);
        futures.forEach(future->{
            if(!future.isDone()){
                future.cancel(true);
            }
        });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelReadComplete");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
