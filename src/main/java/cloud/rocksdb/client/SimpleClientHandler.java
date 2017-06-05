package cloud.rocksdb.client;

import cloud.rocksdb.command.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;

/**
 * Created by fafu on 2017/5/31.
 */
@Data
public class SimpleClientHandler extends ChannelInboundHandlerAdapter {
//    @Override
//    public void writeBody(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//        super.writeBody(ctx, msg, promise);
//    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Response<?> response = (Response)msg;
        Channel channel = ctx.channel();
        Connection conn = null;
//        if(conn != null){
            TaskFuture taskFuture = channel.attr(ConnectionPool.NETTY_CHANNEL_KEY).get();
            if(taskFuture != null && taskFuture.getTask() != null && !taskFuture.getTask().isCanceled()){
                Task task = taskFuture.getTask();
                try {
                    System.out.println("channelRead in client."+response.getType());
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
//        }

    }
}
