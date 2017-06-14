package cloud.rocksdb.server.client;

import cloud.rocksdb.server.command.Command;
import cloud.rocksdb.server.command.Response;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by fafu on 2017/6/1.
 */
@AllArgsConstructor
@Data
public class TaskFuture<R extends Command> implements Future<Response<?>> {
    private Task task;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        task.setCanceled(true);
        task.getThread().interrupt();//Interrupt executor thread.
        return true;
    }

    @Override
    public boolean isCancelled() {
        return task.isCanceled();
    }

    @Override
    public boolean isDone() {
        return task.isDone();
    }

    @Override
    public Response<?> get() throws InterruptedException, ExecutionException {
        try{
            task.getLock().lock();
            task.getCondition().await();
            return task.getResponse();
        }finally {
            task.getLock().unlock();
        }
    }

    @Override
    public Response<?> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try{
            task.getLock().lock();
            task.getCondition().await(timeout,unit);
            return task.getResponse();
        }finally {
            task.getLock().unlock();
        }
    }
}
