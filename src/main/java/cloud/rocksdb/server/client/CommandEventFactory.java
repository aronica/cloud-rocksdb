package cloud.rocksdb.server.client;

import cloud.rocksdb.server.command.CommandEvent;
import com.lmax.disruptor.EventFactory;

/**
 * Created by fafu on 2017/6/1.
 */
public class CommandEventFactory implements EventFactory<CommandEvent> {

    public CommandEvent newInstance() {
        return new CommandEvent();
    }
}
