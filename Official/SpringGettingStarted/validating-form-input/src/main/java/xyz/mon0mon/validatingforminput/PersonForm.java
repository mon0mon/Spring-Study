package xyz.mon0mon.validatingforminput;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PersonForm {
    @NotNull
    @Size(min = 2, max = 30)
    private String name;

    @NotNull
    @Min(18)
//    @Min(value = 18, message = "Adolescent are not allowed")
    private Integer age;
}
