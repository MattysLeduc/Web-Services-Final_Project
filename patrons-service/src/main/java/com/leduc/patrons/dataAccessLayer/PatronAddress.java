package com.leduc.patrons.dataAccessLayer;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Embeddable
@NoArgsConstructor
@Getter
@Builder
public class PatronAddress {

    private String streetAddress;
    private String city;
    private String province;
    private String country;
    private String postalCode;

    public PatronAddress(@NotNull String streetAddress, @NotNull String city, @NotNull String province, @NotNull String country, @NotNull String postalCode) {
        this.streetAddress = streetAddress;
        this.city = city;
        this.province = province;
        this.country = country;
        this.postalCode = postalCode;
    }
}
