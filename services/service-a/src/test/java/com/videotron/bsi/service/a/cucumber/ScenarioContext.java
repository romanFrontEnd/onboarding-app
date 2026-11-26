package com.videotron.bsi.service.a.cucumber;

import io.cucumber.java.Before;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.Setter;

@ApplicationScoped
@Getter
@Setter
public class ScenarioContext {

    private String xTransactionId;
    private String observedTransactionId;
    private long elapsedMillis;

    @Before
    public void reset() {
        this.xTransactionId = null;
        this.observedTransactionId = null;
        this.elapsedMillis = 0;
    }
}
