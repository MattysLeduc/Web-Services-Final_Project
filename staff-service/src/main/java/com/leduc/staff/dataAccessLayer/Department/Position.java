package com.leduc.staff.dataAccessLayer.Department;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Embeddable
@Getter
@NoArgsConstructor
public class Position {

    @Enumerated(EnumType.STRING)
    private PositionTitle title;

    @Enumerated(EnumType.STRING)
    private PositionCode code;

    public Position(@NotNull PositionTitle title, @NotNull PositionCode code){
        this.title = title;
        this.code = code;
    }
}
