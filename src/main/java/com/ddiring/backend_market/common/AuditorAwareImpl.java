package com.ddiring.backend_market.common;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<String> {

    private static final Logger log = LoggerFactory.getLogger(AuditorAwareImpl.class);
    private final HttpSession httpSession;

    @Override
    public Optional<String> getCurrentAuditor() {
        Object attr;
        try {
            attr = httpSession.getAttribute("user");
        } catch (IllegalStateException e) {
            return Optional.empty();
        }

        if (attr == null) {
            return Optional.empty();
        }

        if (attr instanceof String s) {
            String trimmed = s.trim();
            return trimmed.isEmpty() ? Optional.empty() : Optional.of(trimmed);
        }

        try {
            var m1 = attr.getClass().getMethod("getUserSeq");
            Object v1 = m1.invoke(attr);
            if (v1 instanceof String str && !str.isBlank())
                return Optional.of(str);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception ex) {
            log.debug("AuditorAware getUserSeq() 접근 실패: {}", ex.getMessage());
        }
        try {
            var m2 = attr.getClass().getMethod("getId");
            Object v2 = m2.invoke(attr);
            if (v2 != null)
                return Optional.of(v2.toString());
        } catch (NoSuchMethodException ignored) {
        } catch (Exception ex) {
            log.debug("AuditorAware getId() 접근 실패: {}", ex.getMessage());
        }
        String fallback = attr.toString();
        return fallback.isBlank() ? Optional.empty() : Optional.of(fallback);
    }
}