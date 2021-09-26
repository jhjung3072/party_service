package com.party.modules.platform;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformRepository extends JpaRepository<Platform, Long> {
    Platform findByKoreanNameOfPlatformAndEnglishNameOfPlatform(String koreanNameOfPlatform, String englishNameOfPlatform);
}
