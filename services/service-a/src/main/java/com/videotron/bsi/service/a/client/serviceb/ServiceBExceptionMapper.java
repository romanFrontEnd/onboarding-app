package com.videotron.bsi.service.a.client.serviceb;

import static com.videotron.bsi.service.a.constants.Dictionary.SERVICE_B;

import com.videotron.bsi.service.a.client.mapper.AbstractBackendExceptionMapper;

import lombok.extern.slf4j.Slf4j;

/** Registered via @RegisterProvider on ServiceBClient. When a spec pins the
 *  whole backend to one catalog entry regardless of status, override
 *  normalize(...) instead of relying on fromHttpStatus. */
@Slf4j
public class ServiceBExceptionMapper extends AbstractBackendExceptionMapper {
    @Override protected String backendName() { return SERVICE_B; }
    @Override protected Strategy strategy()  { return Strategy.NORMALIZE; }
}
