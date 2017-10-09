package redis;

import com.google.common.collect.Lists;
import io.netty.handler.codec.redis.ArrayHeaderRedisMessage;
import io.netty.handler.codec.redis.FullBulkStringRedisMessage;
import io.netty.handler.codec.redis.RedisMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by fafu on 2017/8/4.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullBulkRedisMessage implements RedisMessage {

    private ArrayHeaderRedisMessage head;
    private long bodyLength;
    private List<FullBulkStringRedisMessage> body = Lists.newArrayList();
}
