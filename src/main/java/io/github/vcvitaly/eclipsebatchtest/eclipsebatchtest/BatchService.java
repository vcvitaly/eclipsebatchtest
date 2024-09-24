package io.github.vcvitaly.eclipsebatchtest.eclipsebatchtest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final ProjectAssetRepo repo;

    public void save(int numBatches) {

    }
}
