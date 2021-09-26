package com.party.modules.platform;

import lombok.Data;

@Data
public class PlatformForm {

    private String platformName;

    // "%s(%s)", city, koreanNameOfPlatform
    public String getKoreanNameOfPlatform() {
        return platformName.substring(0, platformName.indexOf("("));
    } //넷플릭스

    public String getEnglishNameOfPlatform() {
        return platformName.substring(platformName.indexOf("(") + 1, platformName.indexOf(")"));} // netflix


    public Platform getPlatform() {
        return Platform.builder().koreanNameOfPlatform(this.getKoreanNameOfPlatform())
                .englishNameOfPlatform(this.getEnglishNameOfPlatform()).build();
    }

}
