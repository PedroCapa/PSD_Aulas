package company.resources;

import company.representations.Saying;

import com.google.common.base.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/department")
@Produces(MediaType.APPLICATION_JSON)
public class Department {
    private String name;
    private String description;

    public Department(String defaultName, String description) {
        this.name = defaultName;
        this.description = description;
    }

    public String getDescription(){
        return this.description;
    }

    public String getName(){
        return this.name;
    }
}

