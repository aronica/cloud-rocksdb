package cloud.rocksdb.client;


import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import cloud.rocksdb.command.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
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
    private Channel channel;
    private AtomicInteger seq = new AtomicInteger(0);
    private ReentrantLock lock = new ReentrantLock();
    private ConcurrentHashMap<Integer,TaskFuture<Response<?>>> taskMap = new ConcurrentHashMap<>();

    public Connection(Channel channel){
        this.channel = channel;
    }

    public Future<Response<?>> asyncSendTask(Task task){
        assert task != null;
        TaskFuture<Response<?>> future = null;
        try {
            Condition condition = lock.newCondition();
            lock.lock();
            task.setCondition(condition);
            task.setLock(lock);
            int seq = task.getRequest().getSeq();
            channel.writeAndFlush(task.getRequest());
            future = new TaskFuture<Response<?>>(task);
            taskMap.put(seq,future);
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
