package si.nakupify.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.logging.Logger;

@ApplicationScoped
public class TenantService {

    @Inject
    JsonWebToken jwt;

    private Logger log = Logger.getLogger(TenantService.class.getName());

    public String getTenant() {
        /*
        if (jwt == null || jwt.getName() == null) {
            log.info("Zahtevana avtorizacija (JWT manjka)");
            return null;
        }

        String tenant = jwt.getClaim("tenant");

        if (tenant == null || tenant.isBlank()) {
            log.info("JWT ne vsebuje claim 'tenant'");
            return null;
        }

        return tenant;
        */

        String tenant = "org1";

        return tenant;
    }
}
