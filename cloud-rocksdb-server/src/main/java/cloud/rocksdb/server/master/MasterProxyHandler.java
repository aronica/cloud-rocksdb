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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fafu on 2017/5/30.
 */
@Data
@AllArgsConstructor
public class MasterProxyHandler extends ChannelInboundHandlerAdapter {

    private Map<String,Map<String,DataServerNode>> serverNodeMap;
    private AtomicInteger inc = new AtomicInteger(0);
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
                ctx.write(new GetResponse(res));
            case PUT:
                //todo 1. Change to parallel call. 2. Add return strategy,for example ,ONE/ALL/QUORUM
                PutCommand putCommand = ((PutCommand)command);
                servers.forEach(server->{
                    try {
                        server.put(putCommand.getKey(),putCommand.getValue());
                        ctx.write(new PutResponse());
                    } catch (Exception e) {
                        e.printStackTrace();
                        //todo

                    }
                });
                break;
            case MULTIGET:
                MultiGetCommand multiGetCommand = (MultiGetCommand)command;
                Map<? extends byte[], ? extends byte[]> ret = servers.get(inc.getAndIncrement()%servers.size()).multiGet(Arrays.asList(multiGetCommand.getKeys()));
                ctx.write(new MultiGetResponse((Map<byte[], byte[]>) ret));
            case DELETE:
                //TODO
                break;
            case GET_LATEST_SEQ:
                //just break
                break;
            case EXIST:
                ExistCommand existCommand = (ExistCommand)command;
                ctx.write(servers.get(inc.getAndIncrement()%servers.size()).exist(existCommand.getKey()));
                break;
            default:
        }
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
