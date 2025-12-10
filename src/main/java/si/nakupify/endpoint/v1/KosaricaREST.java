package si.nakupify.endpoint.v1;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import si.nakupify.service.KosaricaService;
import si.nakupify.service.dto.ElementDTO;
import si.nakupify.service.dto.ErrorDTO;
import si.nakupify.service.dto.KosaricaDTO;

import java.util.logging.Logger;

@Path("/v1/kosarica")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KosaricaREST {

    @Inject
    KosaricaService kosaricaService;

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
    @Operation(summary="Pridobi košarico", description="Vrne košarico uporabnika s podanim id.")
    @APIResponses({
            @APIResponse(responseCode="200", description="(OK) Uspešno vrne košarico uporabika s podanim id."),
            @APIResponse(responseCode="400", description="(BAD_REQUEST) Podana nepravilna oblika id v url."),
    })
    public Response getKosaricaUporabnika(@PathParam("id") Long id) {
        if (id == null) {
            ErrorDTO parameterError = new ErrorDTO(400, "V URL mora biti podan parameter id.");
            log.info("Path parameter error: V URL ni podanega id");
            return Response.status(Response.Status.BAD_REQUEST).entity(parameterError).build();
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
        ErrorDTO validationError = validacija(kosaricaDTO, 0);
        if (validationError != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(validationError).build();
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
        ErrorDTO validationError = validacija(kosaricaDTO, 1);
        if (validationError != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(validationError).build();
        }

        KosaricaDTO kosarica = kosaricaService.posodobiKosarico(kosaricaDTO);
        if (kosarica == null) {
            ErrorDTO notFoundError = new ErrorDTO(404, "Elementa kosarice s podanim id_kosarica ni bilo mogoče najti!");
            return Response.status(Response.Status.NOT_FOUND).entity(notFoundError).build();
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
            ErrorDTO parameterError = new ErrorDTO(400, "V URL mora biti podan parameter id.");
            log.info("Path parameter error: V URL ni podanega id");
            return Response.status(Response.Status.BAD_REQUEST).entity(parameterError).build();
        }

        KosaricaDTO kosarica = kosaricaService.izbrisiKosarico(id);

        return Response.status(Response.Status.NO_CONTENT).entity(kosarica).build();
    }
}
