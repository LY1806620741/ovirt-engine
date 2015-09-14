package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.model.Nic;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface VmNicResource extends NicResource {

    @Path("{action: (activate|deactivate)}/{oid}")
    public ActionResource getActionSubresource(@PathParam("action") String action, @PathParam("oid") String oid);

    // TODO remove on 4.1
    @Deprecated
    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
    @Actionable
    @Path("activate")
    public Response activate(Action action);

    // TODO remove on 4.1
    @Deprecated
    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
    @Actionable
    @Path("deactivate")
    public Response deactivate(Action action);

    @PUT
    @Override
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
    public Nic update(Nic device);

    @GET
    @Override
    public Nic get();

    @DELETE
    Response remove();

    @Path("reporteddevices")
    public VmReportedDevicesResource getVmReportedDevicesResource();
}
