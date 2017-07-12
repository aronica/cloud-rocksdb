package cloud.rocksdb.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class BaseConfig {
    private static final Logger LOG = LoggerFactory.getLogger(BaseConfig.class);
    public static final String SEPARATOR_COLON = ":";
    public static final String SEPARATOR_COMMA = ",";

    public static final String SEPARATOR_CTRL_A = "\001";
    public static final String SEPARATOR_CTRL_B = "\002";
    public static final String SEPARATOR_CTRL_C = "\003";

    public static final String CHAR_SET_NAME_UTF8 = "UTF-8";
    public static final int IMPORT_DATA_BATCH = 10000;
    public static final int THREAD_SLEEP_TIME = 50;

    public static final String DEFAULT_CONFIG_BASE_DIR = "/data/pps_config";

    public static final String LOG_INFO_FLAG_MSG = "======================================";
    public static final String OUT_OF_SERVICE_MSG = "--> service is disabled, action quit";
    public static final String PARAM_IS_NULL_OR_EMPTY_MSG = "--> param is null or empty, will return";
    public static final String LOG_INFO_EXCEPTION_MSG = "--> exception : ";

    public static final String NO_KEY_INFO_MSG = "no key info";
    public static final String EMPTY_TAG_RESULT_MSG = "tag result is empty";
    public static final String INTERNAL_ERROR_MSG = "internal error";
    public static final String INVALID_REQUEST_MSG = "Invalid appid, token, parameter or no privillege for tags";

    private BaseConfig() { }

    public static void init() {
        File dir = new File(DEFAULT_CONFIG_BASE_DIR);
        if (dir.exists()) {
            LOG.info("--> {} exists", DEFAULT_CONFIG_BASE_DIR);
        } else {
            if (dir.mkdirs()) {
                LOG.info("--> {} make it success", DEFAULT_CONFIG_BASE_DIR);
            } else {
                LOG.info("--> {} make it failed", DEFAULT_CONFIG_BASE_DIR);
            }
        }
        return ;
    }
}
