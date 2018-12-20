package com.rbkmoney.midgard.base.load.pollers.dominant;

public interface DominantHandler<T> {

    boolean accept(T change);

    void handle(T change, Long versionId);
}
