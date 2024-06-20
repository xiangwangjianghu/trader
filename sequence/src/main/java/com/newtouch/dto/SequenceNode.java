package com.newtouch.dto;

import com.alipay.sofa.jraft.rhea.LeaderStateListener;
import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Data
@RequiredArgsConstructor
@Slf4j
public class SequenceNode {

    // 分布式数据库
    private final RheaKVStoreOptions options;

    // 分布式存储节点 要求强一致性
    private RheaKVStore rheaKVStore;

    private final AtomicLong leaderTerm = new AtomicLong(-1);

    // 是否leader节点
    public boolean isLeader() {
        return leaderTerm.get() > 0;
    }

    // 停止Node
    public void stop() {
        rheaKVStore.shutdown();
    }

    // 启动Node
    public void start() {
        // 1.初始化kvstore
        rheaKVStore = new DefaultRheaKVStore();
        rheaKVStore.init(options);

        // 2.监听节点状态
        rheaKVStore.addLeaderStateListener(-1,new LeaderStateListener() {
            // 成为主节点
            @Override
            public void onLeaderStart(long newTerm) {
                log.info("節點財務領導");
                leaderTerm.set(newTerm);
            }

            // 去掉主节点
            @Override
            public void onLeaderStop(long oldTerm) {
                leaderTerm.set(-1);
            }
        });
    }
}
