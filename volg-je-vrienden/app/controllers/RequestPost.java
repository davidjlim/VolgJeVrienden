package controllers; /**
 * Created by s1511432 on 18/05/17.
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;

public class RequestPost extends Controller {
    @Inject
    WSClient ws;

    public Result send(){
        JsonNode json = Json.newObject()
                .put("pid", "31643572882").put("password","abc");
        assert(ws != null);
        assert(ws.url("http://localhost:9000/insert") != null);
        ws.url("http://localhost:9000/insert").post(json);
        return ok(json);
    }
}
