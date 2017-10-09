package redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by fafu on 2017/8/3.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KvException extends Exception {
    public int code;
    public String msg;

}
