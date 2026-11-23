
package com.covoiturage.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleRequestDTO {

    @NotBlank
    private String marque;

    @NotBlank
    private String modele;

    @NotBlank
    private String immatriculation;

    private String couleur;

    @NotNull
    @Min(1)
    private Integer capacite;

    @NotNull
    private Long driverId;
}
