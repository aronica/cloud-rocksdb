package cloud.rocksdb.client;

import cloud.rocksdb.command.Command;
import cloud.rocksdb.command.Response;
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
        task.getCondition().await();
        return task.getResponse();
    }

    @Override
    public Response<?> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        task.getCondition().await(timeout,unit);
        return task.getResponse();
    }
}
