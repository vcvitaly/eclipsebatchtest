package io.github.vcvitaly.eclipsebatchtest.eclipsebatchtest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final BatchService batchService;

    @PostMapping("/run")
    @ResponseStatus(HttpStatus.OK)
    public void runBatches(@RequestBody BatchRequestDto dto) {
        final long start = System.currentTimeMillis();
        batchService.save(dto.numBatches());
        final long end = System.currentTimeMillis();
        log.info("Batch execution time: {} ms", end - start);
    }
}
