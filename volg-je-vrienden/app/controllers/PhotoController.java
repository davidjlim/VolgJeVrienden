package controllers;

import play.mvc.Result;

import controllers.DatabaseController;
import play.mvc.Http.MultipartFormData;

/**
 * Created by s1826328 on 23/05/17.
 */
public class PhotoController extends DatabaseController {
    public Result addPhoto() {
        /*MultipartFormData body = request().body().asMultipartFormData();
        String pid = body.  .getString("pid");
        String password = body.getFile("password");

        if(!checkValidUser(pid, password))
            return ok("");

        MultipartFormData.FilePart imageFile = body.getFile("image");

        System.out.println(file.getFilename());
        //System.out.println(file.getFile());//.getAbsoluteFile());*/
        return ok("");
    }
}