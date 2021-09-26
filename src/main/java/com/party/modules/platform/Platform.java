package com.party.modules.platform;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Platform {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String koreanNameOfPlatform;

    @Column(nullable = false)
    private String englishNameOfPlatform;

    @Override
    public String toString() {
        return String.format("%s(%s)", koreanNameOfPlatform, englishNameOfPlatform);
    }

}
