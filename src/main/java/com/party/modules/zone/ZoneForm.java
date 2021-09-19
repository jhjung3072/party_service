package com.party.modules.zone;

import lombok.Data;

@Data
public class ZoneForm {

    private String zoneName;

    // "%s(%s)/%s", city, localNameOfCity, province,                     Seoul(서울특별시)/none
    public String getCityName() {
        return zoneName.substring(0, zoneName.indexOf("("));
    } //Seoul

    public String getLocalNameOfCity() {
        return zoneName.substring(zoneName.indexOf("(") + 1, zoneName.indexOf(")"));} // 서울특별시

    public String getProvinceName() {
        return zoneName.substring(zoneName.indexOf("/") + 1);
    } // none

    public Zone getZone() {
        return Zone.builder().city(this.getCityName())
                .localNameOfCity(this.getLocalNameOfCity())
                .province(this.getProvinceName()).build();
    }

}
