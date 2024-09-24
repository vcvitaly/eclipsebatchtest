package io.github.vcvitaly.eclipsebatchtest.eclipsebatchtest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final BatchService batchService;

    @PostMapping("/run")
    @ResponseStatus(HttpStatus.OK)
    public void runBatches(@RequestBody BatchRequestDto dto) {
        batchService.save(dto.numBatches());
    }
}
