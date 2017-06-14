package cloud.rocksdb.server.client;


import cloud.rocksdb.server.command.Response;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by fafu on 2017/6/1.
 */
@AllArgsConstructor
@Data
public class Connection{
    private static final Logger log = LoggerFactory.getLogger(Connection.class);
    public static final AttributeKey<Connection> CONNECTION_KEY = AttributeKey.valueOf("channel-connection");
    private Channel channel;
    private AtomicInteger seq = new AtomicInteger(0);
    private ReentrantLock lock = new ReentrantLock();
    private Map<Integer,TaskFuture<Response<?>>> taskMap = new HashMap<>();

    public Connection(Channel channel){
        this.channel = channel;
        Attribute<Connection> attribute = channel.attr(CONNECTION_KEY);
        attribute.set(this);
    }

    public Future<Response<?>> asyncSendTask(Task task){
        assert task != null;
        TaskFuture<Response<?>> future = null;
        try {
            Condition condition = lock.newCondition();
            lock.lock();
            task.setCondition(condition);
            task.setLock(lock);
            int seq = task.getRequest().getTransactionId();
            channel.writeAndFlush(task.getRequest()).sync();
            future = new TaskFuture<Response<?>>(task);
            taskMap.put(seq,future);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return future;
    }

    public Response<?> sendTask(Task task) throws TimeoutException, ExecutionException {
        Future<Response<?>> future = asyncSendTask(task);
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new TimeoutException("Execution is interrupted since a request timeout occurs.");
        }
    }

    public TaskFuture<Response<?>> get(int seq){
        return taskMap.get(seq);
    }
}
