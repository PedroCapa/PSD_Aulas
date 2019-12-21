package company.resources;

import company.representations.Saying;

import com.google.common.base.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Path("/departments")
@Produces(MediaType.APPLICATION_JSON)
public class Departments {
    private Map<String, Department> departments;

    public Departments() {
        this.departments = new HashMap<String, Department>();
        Department department = new Department("UM", "UMinho department");
        departments.put(department.getName(), department);
        Department d = new Department("FCP", "Departamento do FCP");
        departments.put(d.getName(), d);
    }

    @GET
    public List<Saying> sayHello() {
        List<Saying> res = new ArrayList<Saying>();
        for(Department d: departments.values()){
            res.add(new Saying(d.getName(), d.getDescription()));
        }
        return res;
    }

    @GET
    @Path("/{name}")
    public Response getDepartment(@PathParam("name") String name){
        if(!departments.containsKey(name)){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Department d = departments.get(name);
        Saying s = new Saying(d.getName(), d.getDescription());
        return Response.ok(s).build();
    }

    @POST
    public Response add(@QueryParam("name") String name, @QueryParam("description") String description){
        departments.put(name, new Department(name, description));
        return Response.status(Response.Status.CREATED).build();
    }
    

    @PUT
    public Response set(@QueryParam("name") String name, @QueryParam("description") String description){
        if(!departments.containsKey(name)){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        departments.remove(name);
        departments.put(name, new Department(name, description));

        return Response.status(Response.Status.CREATED).build();
    }
}