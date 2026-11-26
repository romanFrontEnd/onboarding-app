package com.videotron.bsi.service.a.mapping;

// Third-party libraries
import org.mapstruct.Mapper;

// Internal project packages
import com.videotron.bsi.service.a.resources.CombinedResponseServiceB;
import com.videotron.bsi.service.a.resources.HelloResponse;

// Jakarta/JEE
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Mapper(componentModel = "jakarta")
public interface CombinedMapper {

    // TODO Not Implemented — signatures only; Cody implements the @Mapping
    // annotations driven by Teddy's failing tests and the spec's mapping rules
    CombinedResponseServiceB toServiceB(HelloResponse source);
}
