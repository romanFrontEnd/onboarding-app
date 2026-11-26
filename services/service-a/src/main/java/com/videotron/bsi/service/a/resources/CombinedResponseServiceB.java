
package com.videotron.bsi.service.a.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;
import java.util.List;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
@SuppressWarnings("unused")
public class CombinedResponseServiceB {


    @JsonProperty("message")
    private String message;
    @JsonProperty("version")
    private String version;
}
