package com.rbkmoney.midgard.service.load.services;

import com.rbkmoney.damsel.domain_config.Commit;
import com.rbkmoney.damsel.domain_config.Operation;
import com.rbkmoney.midgard.service.load.dao.dominant.iface.DominantDao;
import com.rbkmoney.midgard.service.load.pollers.dominant.DominantHandler;
import com.rbkmoney.midgard.service.load.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class DominantService {

    private final DominantDao dominantDao;

    private final List<DominantHandler> handlers;

    @Transactional
    public void processCommit(long versionId, Map.Entry<Long, Commit> e) {
        List<Operation> operations = e.getValue().getOps();
        operations.forEach(op -> handlers.forEach(h -> {
            if (h.accept(op)) {
                log.info("Start to process commit with versionId={} operation={} ", versionId, JsonUtil.tBaseToJsonString(op));
                h.handle(op, versionId);
                log.info("End to process commit with versionId={}", versionId);
            }
        }));
    }

    public Optional<Long> getLastVersionId() {
        Optional<Long> lastVersionId = Optional.ofNullable(dominantDao.getLastVersionId());
        log.info("Last dominant versionId={}", lastVersionId);
        return lastVersionId;
    }
}
