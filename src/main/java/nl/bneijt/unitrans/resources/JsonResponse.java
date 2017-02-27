package nl.bneijt.unitrans.resources;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class JsonResponse {


    static public class StatusMessage {
        public final int status;
        public final String statusText;
        public final String message;

        public StatusMessage(int status, String statusText, String message) {
            this.status = status;
            this.statusText = statusText;
            this.message = message;
        }
    }

    public static Response conflict(String message) {
        return statusResponse(Response.Status.CONFLICT, message);
    }

    private static Response statusResponse(Response.Status status, String message) {
        return Response
                .status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new StatusMessage(status.getStatusCode(), status.getReasonPhrase(), message))
                .build();
    }

    public static Response unauthorized(String message) {
        return statusResponse(Response.Status.UNAUTHORIZED, message);
    }
}
