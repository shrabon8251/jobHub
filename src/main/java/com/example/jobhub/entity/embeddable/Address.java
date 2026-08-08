package com.example.jobhub.entity.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Address {

    private String street;
    private String city;
    private String country;
    private String postalCode;
}
