package io.github.vcvitaly.eclipsebatchtest.eclipsebatchtest;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BatchService {

    private final Random r = new Random();

    private final ProjectAssetRepo repo;

    @Transactional
    public void save(int numBatches) {
        for (int i = 0; i < numBatches; i++) {
            final List<ProjectAsset> assets = IntStream.rangeClosed(1, 3_000)
                    .mapToObj(j -> ProjectAsset.builder().assetGuid(getGuid("a")).projectGuid("pr1").build())
                    .toList();
            repo.saveAll(assets);
            log.info("Saved {} assets", assets.size());
        }
    }

    private String getGuid(String prefix) {
        return "%s_%d_%d".formatted(prefix, System.nanoTime(), r.nextInt(100));
    }
}
