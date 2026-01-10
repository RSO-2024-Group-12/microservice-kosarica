package si.nakupify.endpoint.v1;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import si.nakupify.service.KosaricaService;
import si.nakupify.service.TenantService;
import si.nakupify.service.dto.*;

import java.util.logging.Logger;

@Path("/v1/kosarica")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KosaricaREST {

    @Inject
    KosaricaService kosaricaService;

    @Inject
    TenantService tenantService;

    private Logger log = Logger.getLogger(KosaricaREST.class.getName());

    public ErrorDTO validacija(KosaricaDTO kosaricaDTO, int mode) {
        if (kosaricaDTO == null) {
            log.info("Validation fail: KosaricaDTO ne sme biti null");
            String msg = "Mora biti podan KosaricaDTO!";
            return new ErrorDTO(400, msg);
        }

        if (kosaricaDTO.getId_uporabnik() == null || kosaricaDTO.getKosarica() == null) {
            log.info("Validation fail: KosaricaDTO mora imeti podana polja: id_uporabnik, kosarica");
            String msg = "Polji id_uporabnik in kosarica morata biti podana!";
            return new ErrorDTO(400, msg);
        }

        if (kosaricaDTO.getKosarica().size() != 1) {
            log.info("Validation fail: KosaricaDTO kosarica lahko vsebuje le en element");
            String msg = "Polje kosarica lahko vsebuje le en element!";
            return new ErrorDTO(400, msg);
        }

        ElementDTO element = kosaricaDTO.getKosarica().get(0);

        if (mode == 1) {
            if (element.getId_kosarica() == null || element.getId_izdelek() == null ||
                    element.getCena() == null || element.getCena() <= 0 ||
                    element.getKolicina() == null || element.getKolicina() < 0) {
                log.info("Validation fail: ElementDTO mora imeti podana polja: id_kosarica, id_izdelek, cena, kolicina");
                String msg = "Pri elementu košarice polja id_kosarica, id_izdelek, cena, kolicina ne smejo biti prazna!";
                return new ErrorDTO(400, msg);
            }
        } else {
            if (element.getId_izdelek() == null ||
                    element.getCena() == null || element.getCena() <= 0 ||
                    element.getKolicina() == null || element.getKolicina() < 0) {
                log.info("Validation fail: ElementDTO mora imeti podana polja: id_izdelek, cena, kolicina");
                String msg = "Pri elementu košarice polja id_izdelek, cena, kolicina ne smejo biti prazna!";
                return new ErrorDTO(400, msg);
            }
        }

        return null;
    }

    @GET
    @Path("{id}")
    @Operation(
            summary="Pridobi košarico",
            description="Vrne košarico uporabnika s podanim id.<br>" +
                    "V primeru napake vrne objekt ErrorDTO z opisom napake."
    )
    @APIResponses({
            @APIResponse(
                    responseCode="200",
                    description="(OK) Uspešno vrne košarico uporabika s podanim id.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KosaricaDTO.class)
                    )),
            @APIResponse(
                    responseCode="400",
                    description="(BAD_REQUEST) Podana nepravilna oblika URL.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(
                    responseCode="404",
                    description="(NOT_FOUND) Ni bilo mogoče najti vseh potrebnih podatkov.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(responseCode="503",
                    description="(SERVICE UNAVALIABLE) Težava pri komunikaciji z drugo mikrostoritvijo.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    ))
    })
    public Response getKosaricaUporabnika(@PathParam("id") Long id) {
        if (id == null) {
            ErrorDTO parameterError = new ErrorDTO(400, "V URL mora biti podan parameter id.");
            log.info("Path parameter error: V URL ni podanega id");
            return Response.status(parameterError.getErrorCode()).entity(parameterError).build();
        }

        String tenant = tenantService.getTenant();

        if (tenant == null) {
            ErrorDTO authError = new ErrorDTO(400, "Mora biti podan JWT.");
            log.info("Auth error: JWT mora biti podan");
            return Response.status(authError.getErrorCode()).entity(authError).build();
        }

        PairDTO<KosaricaDTO, ErrorDTO> pair = kosaricaService.pridobiKosarico(id, tenant);
        KosaricaDTO kosarica = pair.getValue();
        ErrorDTO error = pair.getError();

        if (error != null) {
            return Response.status(error.getErrorCode()).entity(error).build();
        }

        return Response.status(200).entity(kosarica).build();
    }

    @POST
    @Operation(
            summary="Dodaj nov izdelek v košarico",
            description="Doda nov izdelek v košarico uporabnika.<br>" +
                    "V primeru napake vrne objekt ErrorDTO z opisom napake."
    )
    @APIResponses({
            @APIResponse(
                    responseCode="201",
                    description="(CREATED) Uspešno dodan izdelek v košarico.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KosaricaDTO.class)
                    )),
            @APIResponse(
                    responseCode="400",
                    description="(BAD_REQUEST) Podana nepravilna oblika vhodnega objekta KosaricaDTO.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(
                    responseCode="404",
                    description="(NOT_FOUND) Ni bilo mogoče najti vseh potrebnih podatkov.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(
                    responseCode="409",
                    description="(CONFLICT) Ni bilo mogoče dodati izdelka v košarico - premalo zaloge.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(
                    responseCode="503",
                    description="(SERVICE UNAVALIABLE) Težava pri komunikaciji z drugo mikrostoritvijo.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    ))
    })
    public Response createKosarica(KosaricaDTO kosaricaDTO) {
        ErrorDTO validationError = validacija(kosaricaDTO, 0);
        if (validationError != null) {
            return Response.status(validationError.getErrorCode()).entity(validationError).build();
        }

        String tenant = tenantService.getTenant();

        if (tenant == null) {
            ErrorDTO authError = new ErrorDTO(400, "Mora biti podan JWT.");
            log.info("Auth error: JWT mora biti podan");
            return Response.status(authError.getErrorCode()).entity(authError).build();
        }

        PairDTO<KosaricaDTO, ErrorDTO> pair = kosaricaService.dodajKosarico(kosaricaDTO, tenant);
        KosaricaDTO kosarica = pair.getValue();
        ErrorDTO error = pair.getError();

        if (error != null) {
            return Response.status(error.getErrorCode()).entity(error).build();
        }

        return Response.status(201).entity(kosarica).build();
    }

    @PUT
    @Operation(
            summary="Posodobi količino izdelka v košarici",
            description="Posodobi količino izdelka v košarici.<br>" +
                    "V primeru, da ima nastavljeno količino na 0, bo odstranjen iz košarice.<br>" +
                    "V primeru napake vrne objekt ErrorDTO z opisom napake."
    )
    @APIResponses({
            @APIResponse(
                    responseCode="200",
                    description="(OK) Uspešno posodobljen košarica.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KosaricaDTO.class)
                    )),
            @APIResponse(
                    responseCode="400",
                    description="(BAD_REQUEST) Podana nepravilna oblika vhodnega objekta KosaricaDTO.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(
                    responseCode="404",
                    description="(NOT_FOUND) Ni bilo mogoče najti vseh potrebnih podatkov.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(
                    responseCode="409",
                    description="(CONFLICT) Ni bilo mogoče posodobiti količine izdelka v košarici - premalo zaloge.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(
                    responseCode="503",
                    description="(SERVICE UNAVALIABLE) Težava pri komunikaciji z drugo mikrostoritvijo.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    ))
    })
    public Response updateKosarica(KosaricaDTO kosaricaDTO) {
        ErrorDTO validationError = validacija(kosaricaDTO, 1);
        if (validationError != null) {
            return Response.status(validationError.getErrorCode()).entity(validationError).build();
        }

        String tenant = tenantService.getTenant();

        if (tenant == null) {
            ErrorDTO authError = new ErrorDTO(400, "Mora biti podan JWT.");
            log.info("Auth error: JWT mora biti podan");
            return Response.status(authError.getErrorCode()).entity(authError).build();
        }

        PairDTO<KosaricaDTO, ErrorDTO> pair = kosaricaService.posodobiKosarico(kosaricaDTO, tenant);
        KosaricaDTO kosarica = pair.getValue();
        ErrorDTO error = pair.getError();

        if (error != null) {
            return Response.status(error.getErrorCode()).entity(error).build();
        }

        return Response.status(200).entity(kosarica).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary="Izbriši košarico",
            description="Izbriše košarico uporabnika s podanim id.<br>" +
                    "V primeru napake vrne objekt ErrorDTO z opisom napake."
    )
    @APIResponses({
            @APIResponse(
                    responseCode="204",
                    description="(NO_CONTENT) Uspešno izbriše košarico uporabnika s podanim id.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KosaricaDTO.class)
                    )),
            @APIResponse(
                    responseCode="400",
                    description="(BAD_REQUEST) Podana nepravilna oblika URL.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(
                    responseCode="404",
                    description="(NOT_FOUND) Ni bilo mogoče najti vseh potrebnih podatkov.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(
                    responseCode="503",
                    description="(SERVICE UNAVALIABLE) Težava pri komunikaciji z drugo mikrostoritvijo.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    ))
    })
    public Response deleteKosarica(@PathParam("id") Long id) {
        if (id == null) {
            ErrorDTO parameterError = new ErrorDTO(400, "V URL mora biti podan parameter id.");
            log.info("Path parameter error: V URL ni podanega id");
            return Response.status(parameterError.getErrorCode()).entity(parameterError).build();
        }

        String tenant = tenantService.getTenant();

        if (tenant == null) {
            ErrorDTO authError = new ErrorDTO(400, "Mora biti podan JWT.");
            log.info("Auth error: JWT mora biti podan");
            return Response.status(authError.getErrorCode()).entity(authError).build();
        }

        PairDTO<KosaricaDTO, ErrorDTO> pair = kosaricaService.izbrisiKosarico(id, tenant);
        KosaricaDTO kosarica = pair.getValue();
        ErrorDTO error = pair.getError();

        if (error != null) {
            return Response.status(error.getErrorCode()).entity(error).build();
        }

        return Response.status(204).entity(kosarica).build();
    }
}
