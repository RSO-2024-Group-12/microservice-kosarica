package si.nakupify.endpoint.v1.REST;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import si.nakupify.service.KosaricaService;
import si.nakupify.service.dto.ElementDTO;
import si.nakupify.service.dto.KosaricaDTO;

@Path("/v1/kosarica")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KosaricaResource {

    @Inject
    KosaricaService kosaricaService;

    @GET
    @Path("/uporabnik/{id}")
    public Response getKosaricaUporabnika(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        KosaricaDTO kosarica = kosaricaService.pridobiKosaricoUporabnika(id);
        return Response.status(Response.Status.OK).entity(kosarica).build();
    }

    @POST
    public Response createKosarica(KosaricaDTO kosaricaDTO) {
        if (kosaricaDTO == null || kosaricaDTO.getId_uporabnik() == null || kosaricaDTO.getKosarica() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        KosaricaDTO kosarica = kosaricaService.dodajVKosarico(kosaricaDTO.getId_uporabnik(), kosaricaDTO.getKosarica().get(0));
        if (kosarica == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.status(Response.Status.CREATED).entity(kosarica).build();
    }

    @PUT
    public Response updateKosarica(ElementDTO elementDTO) {
        if (elementDTO == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        KosaricaDTO kosarica = kosaricaService.posodobiKosarico(elementDTO);
        if (kosarica.getId_uporabnik() == -1) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else if (kosarica.getId_uporabnik() == -2) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(kosarica).build();
    }

    @DELETE
    @Path("/uporabnik/{id}")
    public Response deleteKosarica1(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        KosaricaDTO kosarica = kosaricaService.izbrisiKosaricoUporabnika(id);
        return Response.status(Response.Status.OK).entity(kosarica).build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteKosarica2(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        KosaricaDTO kosarica = kosaricaService.izbrisiKosarico(id);
        if (kosarica == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(kosarica).build();
    }
}
