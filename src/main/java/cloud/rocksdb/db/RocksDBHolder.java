package cloud.rocksdb.db;


import cloud.rocksdb.BaseConfig;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.BitSet;

/**
 * RocksDB客户端
 *
 * @Author : Jiahao.dong
 * @Email : jiahao.dong@ele.me
 * @Date : 2017/3/2
 *
 */
public class RocksDBHolder implements Lifecycle{
    private static final Logger LOG = LoggerFactory.getLogger(RocksDBHolder.class);

    private static final String HUSKAR_ROCKSDB_DATA_FILE = "rocksdb_data_file";
    private String dataPath;


    private static final String HUSKAR_ROCKSDB_BLOCK_CACHE_SIZE = "rocksdb_block_cache_size";
    private static final int DEFAULT_ROCKSDB_BLOCK_CACHE_SIZE = 2;
    private static final String HUSKAR_ROCKSDB_BLOCK_CACHE_COMPRESSED_SIZE = "rocksdb_block_cache_compressed_size";
    private static final int DEFAULT_ROCKSDB_BLOCK_CACHE_COMPRESSED_SIZE = 1;

    private static final String ROCKSDB_WAL_DATA_PATH = "/data/wal_rocksdb";
    private static final int MAX_ROCKSDB_BLOCK_CACHE_SIZE = 40;
    private static final long CACHE_SIZE_UNIT = 1024 * 1024 * 1024L; // GB

    private int blockCacheSize;
    private int blockCacheCompressedSize;

    private RocksDB rocksDB;
    private Options options;
    private ReadOptions readOptions;
    private Filter bloomFilter;
    private File lock ;
    private String id;

    private BitSet slot = new BitSet(16383);

    public RocksDBHolder(String id) throws InstanceAlreadyRunningException {
        this.id = id;

    }

    public RocksDB getResource() {
        return rocksDB;
    }

    public ReadOptions getReadOptions() {
        return readOptions;
    }

    /**
     * 清空RocksDB
     * */
    public void flushDB() {
        closeRocksDB();
        openRocksDB();
        return ;
    }

    public void getLock() throws InstanceAlreadyRunningException {
        File lockDir = new File(ROCKSDB_WAL_DATA_PATH+File.separator+id);
        boolean ret = lockDir.mkdirs();
        if(!ret){
            throw new InstanceAlreadyRunningException("Rocksdb instance with id {} is running.");
        }
    }

    public void cleanLock(){
        File lockDir = new File(ROCKSDB_WAL_DATA_PATH+File.separator+id);
        try {
            lockDir.delete();
        }catch (Exception e){
            LOG.error("Failed to clean lock, you may need clean it handy next time.");
        }
    }


    private void setBlockCacheSize() {
        int cacheSize = 10;
        if (cacheSize > 0 && cacheSize <= MAX_ROCKSDB_BLOCK_CACHE_SIZE) {
            blockCacheSize = cacheSize;
        } else {
            LOG.info("--> blockCacheSize is invalid, set default value");
            blockCacheSize = DEFAULT_ROCKSDB_BLOCK_CACHE_SIZE;
        }
        LOG.info("--> blockCacheSize = {} GB", blockCacheSize);
        return ;
    }

    private void setBlockCacheCompressedSize() {
        int cacheCompressedSize = 5 ;
        if (cacheCompressedSize > 0 && cacheCompressedSize <= MAX_ROCKSDB_BLOCK_CACHE_SIZE) {
            blockCacheCompressedSize = cacheCompressedSize;
        } else {
            LOG.info("--> blockCacheCompressedSize is invalid, set default value");
            blockCacheCompressedSize = DEFAULT_ROCKSDB_BLOCK_CACHE_COMPRESSED_SIZE;
        }
        LOG.info("--> blockCacheCompressedSize = {} GB", blockCacheCompressedSize);
        return ;
    }

    private void initConfig() {
        setBlockCacheSize();
        setBlockCacheCompressedSize();
        bloomFilter = new BloomFilter(12);
        BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
        tableConfig.setBlockCacheSize(blockCacheSize * CACHE_SIZE_UNIT)
                   .setBlockCacheCompressedSize(blockCacheCompressedSize * CACHE_SIZE_UNIT)
                   .setIndexType(IndexType.kBinarySearch)
                   .setCacheIndexAndFilterBlocks(true)
                   .setFilter(bloomFilter);

        File walDir = new File(ROCKSDB_WAL_DATA_PATH);
        if (walDir.exists()) {
            LOG.info("--> {} exists", ROCKSDB_WAL_DATA_PATH);
        } else {
            if (walDir.mkdirs()) {
                LOG.info("--> {} not exists, make it success", ROCKSDB_WAL_DATA_PATH);
            } else {
                LOG.info("--> {} not exists, make it failed", ROCKSDB_WAL_DATA_PATH);
            }
        }

        options = new Options();
        options.createStatistics()
               .setCreateIfMissing(true)
               .setAllowOsBuffer(true)
               .setWriteBufferSize(64 * 1024 * 1024)
               .setMaxWriteBufferNumber(8)
               .setMinWriteBufferNumberToMerge(4)
               .setBloomLocality(4)
               .setWalDir(ROCKSDB_WAL_DATA_PATH)
               .setWalTtlSeconds(3 * 60 * 60L)
               .setTableFormatConfig(tableConfig);

        readOptions = new ReadOptions();
        readOptions.setVerifyChecksums(false);

        dataPath = "/data/rocksdb/";
        File dataDir = new File(dataPath);
        if (dataDir.exists()) {
            LOG.info("--> data path is OK : {}", dataPath);
        } else {
            if (dataDir.mkdirs()) {
                LOG.info("--> dataDir make it OK");
            }
        }
        LOG.info("--> Init config OK");
        return ;
    }

    private void openRocksDB() {
        try {
            rocksDB = RocksDB.open(options, dataPath);
            LOG.info("--> RocksDB data path : {}", dataPath);
        } catch (Exception e) {
            LOG.error(BaseConfig.LOG_INFO_EXCEPTION_MSG + e.getMessage(), e);
            throw new RuntimeException("Failed to startup rocksdb instance.");
        }
        LOG.info("--> Open RocksDB OK");
        return ;
    }

    void closeRocksDB() {
        if (rocksDB != null) {
            rocksDB.close();
            rocksDB = null;
        }
        cleanLock();
        LOG.info("--> Close RocksDB OK");
        return ;
    }

    @Override
    public void init() throws InstanceAlreadyRunningException {
        getLock();
        RocksDB.loadLibrary();
        initConfig();
        openRocksDB();
    }

    @Override
    public void destroy() throws Exception {
        closeRocksDB();
    }
}
