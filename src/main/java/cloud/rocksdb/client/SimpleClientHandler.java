package cloud.rocksdb.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import cloud.rocksdb.command.Response;

/**
 * Created by fafu on 2017/5/31.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SimpleClientHandler extends ChannelDuplexHandler {
//    @Override
//    public void writeBody(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//        super.writeBody(ctx, msg, promise);
//    }
    public ConnectionPool connectionPool;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Response<?> response = (Response)msg;
        Channel channel = ctx.channel();
        Connection conn = null;
        if(conn != null){
            TaskFuture<Response<?>> taskFuture = conn.get(response.getSeq());
            if(taskFuture != null && taskFuture.getTask() != null && !taskFuture.getTask().isCanceled()){
                Task task = taskFuture.getTask();
                try{
                    task.getLock().lock();
                    task.setResponse(response);
                    task.setDone(true);
                    task.getCondition().signal();
                }finally {
                    task.getLock().unlock();
                }
            }
        }

    }
}
