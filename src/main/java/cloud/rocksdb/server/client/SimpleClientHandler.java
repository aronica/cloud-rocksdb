package cloud.rocksdb.server.client;

import cloud.rocksdb.server.command.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;

/**
 * Created by fafu on 2017/5/31.
 */
@Data
public class SimpleClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Response<?> response = (Response)msg;
        Channel channel = ctx.channel();
        Connection connection = channel.attr(Connection.CONNECTION_KEY).get();
        TaskFuture<Response<?>> taskFuture = connection.get(response.getTransactionId());
        if(taskFuture != null && taskFuture.getTask() != null && !taskFuture.getTask().isCanceled()){
            Task task = taskFuture.getTask();
            try {
                task.getLock().lock();
                task.setResponse(response);
                task.setDone(true);
                task.getCondition().signalAll();
            }catch (Exception e){
                task.cancel();
                taskFuture.cancel(true);
            }finally {
                task.getLock().unlock();
            }
        }
    }
}
