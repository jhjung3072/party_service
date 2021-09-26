package com.party.modules.platform;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PlatformService {

    private final PlatformRepository platformRepository;

    //의존성 주입이 이루어진 후 초기화를 수행하는 메서드
    //참고 : https://www.tabnine.com/code/java/methods/java.nio.file.Files/readAllLines
    @PostConstruct
    public void initPlatformData() throws IOException {
        if (platformRepository.count() == 0) {
            Resource resource = new ClassPathResource("OTT_Platform.csv");
            List<Platform> platformList = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).stream()
                    .map(line -> {
                        String[] split = line.split(",");
                        return Platform.builder().koreanNameOfPlatform(split[0]).englishNameOfPlatform(split[1]).build();
                    }).collect(Collectors.toList());
            platformRepository.saveAll(platformList);
        }
    }


}
