package si.nakupify.endpoint.v1;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import si.nakupify.proto.*;
import si.nakupify.service.KosaricaService;
import si.nakupify.service.TenantService;
import si.nakupify.service.dto.ElementDTO;
import si.nakupify.service.dto.ErrorDTO;
import si.nakupify.service.dto.KosaricaDTO;
import si.nakupify.service.dto.PairDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@GrpcService
public class KosaricaGRPC implements gRPCKosaricaService {

    @Inject
    KosaricaService kosaricaService;

    @Inject
    TenantService tenantService;

    private Logger log = Logger.getLogger(KosaricaGRPC.class.getName());

    public boolean validacija(KosaricaDTO kosaricaDTO, int mode) {
        if (kosaricaDTO == null || kosaricaDTO.getId_uporabnik() == null) {
            log.info("Podani manjkajoči ali nepravilni podatki! 1");
            return false;
        }

        for (ElementDTO element : kosaricaDTO.getKosarica()) {
            if (mode == 1) {
                if (element.getId_kosarica() == null) {
                    log.info("Podani manjkajoči ali nepravilni podatki! 2");
                    return false;
                }
            }

            if (element.getId_izdelek() == null ||
                    element.getCena() == null || element.getCena() <= 0 ||
                    element.getKolicina() == null || element.getKolicina() < 0) {
                log.info("Podani manjkajoči ali nepravilni podatki! 3");
                return false;
            }
        }

        return true;
    }

    public gRPCKosaricaDTO toGrpc(KosaricaDTO kosaricaDTO) {
        gRPCKosaricaDTO.Builder protoKosarica = gRPCKosaricaDTO.newBuilder()
                .setIdUporabnik(kosaricaDTO.getId_uporabnik())
                .setTenant(kosaricaDTO.getTenant());

        for (ElementDTO e : kosaricaDTO.getKosarica()) {
            gRPCElementDTO.Builder elementBuilder = gRPCElementDTO.newBuilder();
            elementBuilder.setIdKosarica(e.getId_kosarica() != null ? e.getId_kosarica() : 0);
            elementBuilder.setIdIzdelek(e.getId_izdelek() != null ? e.getId_izdelek() : 0);
            elementBuilder.setNaziv(e.getNaziv() != null ? e.getNaziv() : "");
            elementBuilder.setCena(e.getCena() != null ? e.getCena() : 0);
            elementBuilder.setKolicina(e.getKolicina() != null ? e.getKolicina() : 0);

            protoKosarica.addKosarica(elementBuilder.build());
        }

        return protoKosarica.build();
    }

    public KosaricaDTO toDto(gRPCKosaricaDTO protoKosarica) {
        if (protoKosarica == null) {
            return null;
        }

        List<ElementDTO> elementi = new ArrayList<>();

        for (gRPCElementDTO e : protoKosarica.getKosaricaList()) {
            ElementDTO element = new ElementDTO();
            element.setId_kosarica(e.getIdKosarica() != 0 ? e.getIdKosarica() : null);
            element.setId_izdelek(e.getIdIzdelek() != 0 ? e.getIdIzdelek() : null);
            element.setNaziv(e.getNaziv() != null && !e.getNaziv().isEmpty() ? e.getNaziv() : null);
            element.setCena(e.getCena() != 0 ? e.getCena() : null);
            element.setKolicina(e.getKolicina());
            elementi.add(element);
        }

        return new KosaricaDTO(protoKosarica.getIdUporabnik(), protoKosarica.getTenant(), elementi);
    }

    @Override
    @Blocking
    public Uni<gRPCKosaricaDTO> getKosarica(GetKosaricaRequest request) {
        if (request.getIdUporabnik() == 0) {
            return Uni.createFrom().failure(Status.INVALID_ARGUMENT.asRuntimeException());
        }

        String tenant = tenantService.getTenant();

        if (tenant == null) {
            return Uni.createFrom().failure(Status.PERMISSION_DENIED.asRuntimeException());
        }

        PairDTO<KosaricaDTO, ErrorDTO> pair = kosaricaService.pridobiKosarico(request.getIdUporabnik(), tenant);
        KosaricaDTO kosarica = pair.getValue();
        ErrorDTO error = pair.getError();

        if (error != null) {
            if (error.getErrorCode() == 404) {
                return Uni.createFrom().failure(Status.NOT_FOUND.asRuntimeException());
            }
            if (error.getErrorCode() == 503) {
                return Uni.createFrom().failure(Status.UNAVAILABLE.asRuntimeException());
            }
        }

        return Uni.createFrom().item(toGrpc(kosarica));
    }

    @Override
    @Blocking
    public Uni<gRPCKosaricaDTO> createKosarica(CreateKosaricaRequest request) {
        KosaricaDTO kosaricaDTO = toDto(request.getKosarica());

        if (!validacija(kosaricaDTO, 0)) {
            return  Uni.createFrom().failure(Status.INVALID_ARGUMENT.asRuntimeException());
        }

        String tenant = tenantService.getTenant();

        if (tenant == null) {
            return Uni.createFrom().failure(Status.PERMISSION_DENIED.asRuntimeException());
        }

        PairDTO<KosaricaDTO, ErrorDTO> pair = kosaricaService.dodajKosarico(kosaricaDTO, tenant);
        KosaricaDTO kosarica = pair.getValue();
        ErrorDTO error = pair.getError();

        if (error != null) {
            if (error.getErrorCode() == 404) {
                return Uni.createFrom().failure(Status.NOT_FOUND.asRuntimeException());
            }
            if (error.getErrorCode() == 409) {
                return Uni.createFrom().failure(Status.ALREADY_EXISTS.asRuntimeException());
            }
            if (error.getErrorCode() == 503) {
                return Uni.createFrom().failure(Status.UNAVAILABLE.asRuntimeException());
            }
        }

        return Uni.createFrom().item(toGrpc(kosarica));
    }

    @Override
    @Blocking
    public Uni<gRPCKosaricaDTO> updateKosarica(UpdateKosaricaRequest request) {
        KosaricaDTO kosaricaDTOInput = toDto(request.getKosarica());

        if (!validacija(kosaricaDTOInput, 1)) {
            return  Uni.createFrom().failure(Status.INVALID_ARGUMENT.asRuntimeException());
        }

        String tenant = tenantService.getTenant();

        if (tenant == null) {
            return Uni.createFrom().failure(Status.PERMISSION_DENIED.asRuntimeException());
        }

        PairDTO<KosaricaDTO, ErrorDTO> pair = kosaricaService.posodobiKosarico(kosaricaDTOInput, tenant);
        KosaricaDTO kosarica = pair.getValue();
        ErrorDTO error = pair.getError();

        if (error != null) {
            if (error.getErrorCode() == 404) {
                return Uni.createFrom().failure(Status.NOT_FOUND.asRuntimeException());
            }
            if (error.getErrorCode() == 409) {
                return Uni.createFrom().failure(Status.ALREADY_EXISTS.asRuntimeException());
            }
            if (error.getErrorCode() == 503) {
                return Uni.createFrom().failure(Status.UNAVAILABLE.asRuntimeException());
            }
        }

        return Uni.createFrom().item(toGrpc(kosarica));
    }

    @Override
    @Blocking
    public Uni<Empty> deleteKosarica(DeleteKosaricaRequest request) {
        if (request.getIdUporabnik() == 0) {
            return Uni.createFrom().failure(Status.INVALID_ARGUMENT.asRuntimeException());
        }

        String tenant = tenantService.getTenant();

        if (tenant == null) {
            return Uni.createFrom().failure(Status.PERMISSION_DENIED.asRuntimeException());
        }

        kosaricaService.izbrisiKosarico(request.getIdUporabnik(), tenant);

        return Uni.createFrom().item(Empty.getDefaultInstance());
    }
}
