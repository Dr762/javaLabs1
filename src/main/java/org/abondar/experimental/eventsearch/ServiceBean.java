/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.abondar.experimental.eventsearch;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * @author abondar
 */

public class ServiceBean {

    private static final Log LOG = LogFactory.getLog(ServiceBean.class);

  private final SearchData sd = new SearchData();
  
    @POST
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findPlaces() {
       
                       
        return Response.ok().build();
    }

    @GET
    @Path("/site")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getData() {
        EventFinder ef = new EventFinder();
        ef.getCategorizedEvents("theatre");
        ef.getCategorizedEvents("lectures");
        ef.getCategorizedEvents("club");
        ef.getCategorizedEvents("art");
        ef.getCategorizedEvents("concert");

        return Response.ok().build();

    }

    @GET
    @Path("/index")
    @Produces(MediaType.APPLICATION_JSON)
    public Response indexData() {

       
        sd.IndexFiles();
        
        
        return Response.ok().build();

    }
    
    
    @POST
    @Path("/search")

    @Produces(MediaType.APPLICATION_JSON)
    public Response searchData(String param) throws IOException,  org.apache.lucene.queryparser.classic.ParseException {

  
      String[] filePaths = sd.getEventData(param);
   StringBuilder sb = new StringBuilder();
       sb.append("[");
      for (int i=0;i<filePaths.length;i++){
          
        sb.append(getJSONstr(filePaths[i])).append(";");
      
      }
      sb.deleteCharAt(sb.toString().length()-1);
      sb.append("]");
     String res = sb.toString().replace(";", ",");
        System.out.println(sb.toString());
       return Response.ok(res).build();

    }

   public String  getJSONstr(String path) throws FileNotFoundException, IOException{
    
      StringBuilder res = new StringBuilder();
        List<String> resList = Files.readAllLines(Paths.get(path),Charset.forName("UTF-8"));
        for(String line : resList)
              
        res.append(line);
      
        return res.toString();
    }
 
};
