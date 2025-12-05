package si.nakupify.endpoint.v1.REST;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import si.nakupify.service.KosaricaService;
import si.nakupify.service.dto.ElementDTO;
import si.nakupify.service.dto.KosaricaDTO;

import java.util.logging.Logger;

@Path("/v1/kosarica")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KosaricaREST {

    @Inject
    KosaricaService kosaricaService;

    private Logger log = Logger.getLogger(KosaricaREST.class.getName());

    public boolean validacija(KosaricaDTO kosaricaDTO, int mode) {
        if (kosaricaDTO == null || kosaricaDTO.getId_uporabnik() == null) {
            log.info("Podani manjkajoči ali nepravilni podatki!");
            return false;
        }

        for (ElementDTO element : kosaricaDTO.getKosarica()) {
            if (mode == 1) {
                if (element.getId_kosarica() == null) {
                    log.info("Podani manjkajoči ali nepravilni podatki!");
                    return false;
                }
            }

            if (element.getId_izdelek() == null ||
                    element.getCena() == null || element.getCena() <= 0 ||
                    element.getKolicina() == null || element.getKolicina() < 0) {
                log.info("Podani manjkajoči ali nepravilni podatki!");
                return false;
            }
        }

        return true;
    }

    @GET
    @Path("{id}")
    @Operation(summary="Pridobi košarico", description="Vrne košarico uporabnika s podanim id.")
    @APIResponses({
            @APIResponse(responseCode="200", description="(OK) Uspešno vrne košarico uporabika s podanim id."),
            @APIResponse(responseCode="400", description="(BAD_REQUEST) Podana nepravilna oblika id v url."),
    })
    public Response getKosaricaUporabnika(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        KosaricaDTO kosarica = kosaricaService.pridobiKosarico(id);

        return Response.status(Response.Status.OK).entity(kosarica).build();
    }

    @POST
    @Operation(summary="Ustvari košarico", description="Doda nove izdelke v košarico uporabnika.")
    @APIResponses({
            @APIResponse(responseCode="201", description="(CREATED) Uspešno ustvarjena košarica."),
            @APIResponse(responseCode="400", description="(BAD_REQUEST) Podana nepravilna oblika oz. nepopolna oblika KosaricaDTO."),
    })
    public Response createKosarica(KosaricaDTO kosaricaDTO) {
        if (!validacija(kosaricaDTO, 0)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        KosaricaDTO kosarica = kosaricaService.dodajKosarico(kosaricaDTO);

        return Response.status(Response.Status.CREATED).entity(kosarica).build();
    }

    @PUT
    @Operation(summary="Posodobi košarico", description="Posodobi količine izdelkov v košarici.<br>" +
                "V kolikor ima izdelek podano količino 0, bo odstranjen iz košarice.")
    @APIResponses({
            @APIResponse(responseCode="200", description="(OK) Uspešno posodobljena košarica."),
            @APIResponse(responseCode="400", description="(BAD_REQUEST) Podana nepravilna oblika oz. nepopolna oblika KosaricaDTO."),
            @APIResponse(responseCode="404", description="(NOT_FOUND) Izdelka v košarici ni bilo mogoče najti.")
    })
    public Response updateKosarica(KosaricaDTO kosaricaDTO) {
        if (!validacija(kosaricaDTO, 1)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        KosaricaDTO kosarica = kosaricaService.posodobiKosarico(kosaricaDTO);
        if (kosarica == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(kosarica).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(summary="Izbriši košarico", description="Izbriše košarico uporabnika s podanim id.")
    @APIResponses({
            @APIResponse(responseCode="204", description="(NO_CONTENT) Uspešno izbriše košarico uporabnika s podanim id."),
            @APIResponse(responseCode="400", description="(BAD_REQUEST) Podana nepravilna oblika id v url."),
    })
    public Response deleteKosarica(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        kosaricaService.izbrisiKosarico(id);

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
