package cloud.rocksdb.client;

import cloud.rocksdb.command.Command;
import cloud.rocksdb.command.Response;
import lombok.Data;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

/**
 * Created by fafu on 2017/6/1.
 */
@Data
public class Task {

    public Task(Command request, Connection connection){
        this.request = request;
        this.thread = Thread.currentThread();
    }

    private Command request;
    private Response response;
    private Consumer<?> consumer;
    private Lock lock;
    private Condition condition;

    private volatile boolean canceled;
    private volatile boolean done;
    private Thread thread;


    private int timeout = -1;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    private Connection connection;

    public void cancel(){
        this.canceled = true;
    }
}
